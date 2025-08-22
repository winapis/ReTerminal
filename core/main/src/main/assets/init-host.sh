DISTRIBUTION_DIR=$PREFIX/local/distribution

mkdir -p $DISTRIBUTION_DIR

# Determine which distribution to use based on user selection or available rootfs files
ROOTFS_FILE=""
DISTRIBUTION_NAME=""

# Helper function to find distribution file with multiple extensions
find_distribution_file() {
    local dist_name="$1"
    if [ -f "$PREFIX/files/${dist_name}.tar.gz" ]; then
        echo "${dist_name}.tar.gz"
        return 0
    elif [ -f "$PREFIX/files/${dist_name}.tar.xz" ]; then
        echo "${dist_name}.tar.xz"
        return 0
    fi
    return 1
}

# Check if user has selected a specific distribution
if [ -n "$SELECTED_DISTRIBUTION" ]; then
    # Use the user's selected distribution if the rootfs file exists
    FOUND_FILE=$(find_distribution_file "$SELECTED_DISTRIBUTION")
    if [ -n "$FOUND_FILE" ]; then
        ROOTFS_FILE="$FOUND_FILE"
        DISTRIBUTION_NAME="$SELECTED_DISTRIBUTION"
    else
        echo "Warning: Selected distribution '$SELECTED_DISTRIBUTION' not found, falling back to available distributions"
    fi
fi

# If no valid selection found, fall back to checking available files
if [ -z "$DISTRIBUTION_NAME" ]; then
    # Try to find any available distribution in priority order
    for dist in alpine ubuntu debian arch kali; do
        FOUND_FILE=$(find_distribution_file "$dist")
        if [ -n "$FOUND_FILE" ]; then
            ROOTFS_FILE="$FOUND_FILE"
            DISTRIBUTION_NAME="$dist"
            break
        fi
    done
    
    # If still no distribution found, exit with error
    if [ -z "$DISTRIBUTION_NAME" ]; then
        echo "No distribution rootfs found!"
        echo "Expected one of: alpine.tar.gz, ubuntu.tar.gz, debian.tar.gz, arch.tar.gz, kali.tar.gz"
        echo "             or: alpine.tar.xz, ubuntu.tar.xz, debian.tar.xz, arch.tar.xz, kali.tar.xz"
        echo "Available files in $PREFIX/files/:"
        ls -la "$PREFIX/files/" 2>/dev/null || echo "  - Directory not found"
        exit 1
    fi
fi

# Function to extract and verify distribution
extract_distribution() {
    log_message "Extracting $DISTRIBUTION_NAME rootfs from $ROOTFS_FILE..."
    
    # Use tar options that are more compatible with Android filesystem limitations
    # --no-same-owner: Don't try to preserve owner (may fail on Android)
    # --no-same-permissions: Don't try to preserve exact permissions (may fail on Android)
    # --dereference: Follow symbolic links instead of preserving them
    TAR_OPTS="--no-same-owner --no-same-permissions"
    
    # Try extraction with different strategies if hard links fail
    if ! tar -xf "$PREFIX/files/$ROOTFS_FILE" -C "$DISTRIBUTION_DIR" $TAR_OPTS 2>/dev/null; then
        log_message "Standard extraction failed, trying with hard link conversion..."
        # Try again with hard links converted to copies (more Android-compatible)
        if ! tar -xf "$PREFIX/files/$ROOTFS_FILE" -C "$DISTRIBUTION_DIR" $TAR_OPTS --hard-dereference 2>/dev/null; then
            log_message "Hard link conversion failed, trying without link preservation..."
            # Final attempt: ignore link creation errors
            if ! tar -xf "$PREFIX/files/$ROOTFS_FILE" -C "$DISTRIBUTION_DIR" $TAR_OPTS --warning=no-file-ignored 2>/tmp/tar_errors.log; then
                echo "Error: Failed to extract rootfs from $PREFIX/files/$ROOTFS_FILE"
                echo "File details:"
                ls -la "$PREFIX/files/$ROOTFS_FILE" 2>/dev/null || echo "  - File not found"
                echo "Available files in $PREFIX/files/:"
                ls -la "$PREFIX/files/" 2>/dev/null | head -10
                echo "Target directory status:"
                ls -la "$DISTRIBUTION_DIR/" 2>/dev/null || echo "  - Directory not accessible"
                if [ -f /tmp/tar_errors.log ]; then
                    echo "Tar extraction errors:"
                    cat /tmp/tar_errors.log
                    rm -f /tmp/tar_errors.log
                fi
                exit 1
            else
                log_message "Warning: Extraction completed with some link creation errors (non-critical)"
                if [ -f /tmp/tar_errors.log ]; then
                    log_message "Note: Some hard links were converted to file copies for Android compatibility"
                    rm -f /tmp/tar_errors.log
                fi
            fi
        else
            log_message "Successfully extracted with hard link conversion"
        fi
    fi
    log_message "Successfully extracted $DISTRIBUTION_NAME rootfs"
    
    # Verify critical directories exist
    for dir in bin usr/bin sbin usr/sbin; do
        if [ ! -d "$DISTRIBUTION_DIR/$dir" ]; then
            log_message "Warning: Expected directory $dir not found in extracted rootfs"
        fi
    done
    
    # For Ubuntu/Debian, verify apt is available
    if [ "$DISTRIBUTION_NAME" = "ubuntu" ] || [ "$DISTRIBUTION_NAME" = "debian" ]; then
        if [ ! -f "$DISTRIBUTION_DIR/usr/bin/apt" ] && [ ! -f "$DISTRIBUTION_DIR/usr/bin/apt-get" ]; then
            log_message "Warning: Neither apt nor apt-get found in extracted $DISTRIBUTION_NAME rootfs"
            if [ ! -f "$SILENT_MODE_FILE" ]; then
                echo "Available binaries in usr/bin:"
                ls "$DISTRIBUTION_DIR/usr/bin/" 2>/dev/null | grep -E "(apt|dpkg)" || echo "  - No apt/dpkg binaries found"
            fi
        fi
    fi
}

# Function to verify existing distribution matches selected one
verify_existing_distribution() {
    if [ -f "$DISTRIBUTION_DIR/etc/os-release" ]; then
        # Check if the existing distribution matches the selected one
        if [ "$DISTRIBUTION_NAME" = "ubuntu" ]; then
            if grep -q "ID=ubuntu" "$DISTRIBUTION_DIR/etc/os-release" 2>/dev/null; then
                return 0  # Match found
            fi
        elif [ "$DISTRIBUTION_NAME" = "debian" ]; then
            if grep -q "ID=debian" "$DISTRIBUTION_DIR/etc/os-release" 2>/dev/null; then
                return 0  # Match found
            fi
        elif [ "$DISTRIBUTION_NAME" = "alpine" ]; then
            if grep -q "ID=alpine" "$DISTRIBUTION_DIR/etc/os-release" 2>/dev/null; then
                return 0  # Match found
            fi
        elif [ "$DISTRIBUTION_NAME" = "arch" ]; then
            if grep -q "ID=arch" "$DISTRIBUTION_DIR/etc/os-release" 2>/dev/null; then
                return 0  # Match found
            fi
        elif [ "$DISTRIBUTION_NAME" = "kali" ]; then
            if grep -q "ID=kali" "$DISTRIBUTION_DIR/etc/os-release" 2>/dev/null; then
                return 0  # Match found
            fi
        fi
    fi
    return 1  # No match or no os-release file
}

# Check for silent mode flag
SILENT_MODE_FILE="$DISTRIBUTION_DIR/.reterminal_installed"

# Helper function for conditional output
log_message() {
    if [ ! -f "$SILENT_MODE_FILE" ]; then
        echo "$1"
    fi
}

if [ -z "$(ls -A "$DISTRIBUTION_DIR" | grep -vE '^(root|tmp)$')" ]; then
    # Directory is empty, extract the distribution
    log_message "Setting up $DISTRIBUTION_NAME environment..."
    extract_distribution
    # Mark installation as complete for silent mode
    touch "$SILENT_MODE_FILE"
    log_message "$DISTRIBUTION_NAME installation completed"
else
    # Directory exists, verify it contains the correct distribution
    if verify_existing_distribution; then
        if [ ! -f "$SILENT_MODE_FILE" ]; then
            log_message "Using existing $DISTRIBUTION_NAME installation"
            # Mark installation as complete for future silent mode
            touch "$SILENT_MODE_FILE"
        fi
    else
        log_message "Existing installation does not match selected distribution ($DISTRIBUTION_NAME)"
        if [ -f "$DISTRIBUTION_DIR/etc/os-release" ]; then
            existing_id=$(grep "^ID=" "$DISTRIBUTION_DIR/etc/os-release" 2>/dev/null | cut -d'=' -f2 | tr -d '"' 2>/dev/null || echo "unknown")
            if [ -n "$existing_id" ] && [ "$existing_id" != "unknown" ]; then
                log_message "Found existing distribution: $existing_id (expected: $DISTRIBUTION_NAME)"
            else
                log_message "Existing distribution could not be determined from /etc/os-release"
            fi
        else
            log_message "No /etc/os-release found in existing installation"
        fi
        log_message "Clearing existing installation and extracting correct distribution..."
        # Clear existing installation more thoroughly and safely
        log_message "Backing up important files before cleanup..."
        
        # Create temporary backup directory for any user data
        BACKUP_DIR="/tmp/reterminal_backup_$$"
        mkdir -p "$BACKUP_DIR"
        
        # Backup user home directory if it exists
        if [ -d "$DISTRIBUTION_DIR/root" ] && [ "$(ls -A "$DISTRIBUTION_DIR/root" 2>/dev/null | wc -l)" -gt 0 ]; then
            log_message "Backing up user data from /root..."
            cp -r "$DISTRIBUTION_DIR/root" "$BACKUP_DIR/" 2>/dev/null || true
        fi
        
        # More robust directory cleanup
        log_message "Cleaning existing distribution files..."
        cd "$DISTRIBUTION_DIR" || exit 1
        
        # Remove directories that are safe to remove completely
        for dir in bin boot dev etc lib lib64 media mnt opt proc run sbin srv sys tmp usr var; do
            if [ -d "$dir" ]; then
                log_message "Removing $dir..."
                rm -rf "$dir" 2>/dev/null || {
                    log_message "Warning: Could not remove $dir completely, trying to clean contents..."
                    find "$dir" -mindepth 1 -exec rm -rf {} + 2>/dev/null || true
                }
            fi
        done
        
        # Remove any remaining files except home directory
        find . -maxdepth 1 -type f -exec rm -f {} + 2>/dev/null || true
        find . -maxdepth 1 -type l -exec rm -f {} + 2>/dev/null || true
        
        # Remove any remaining directories except home
        for item in *; do
            if [ -d "$item" ] && [ "$item" != "home" ] && [ "$item" != "root" ]; then
                rm -rf "$item" 2>/dev/null || true
            fi
        done
        
        # Verify the directory is cleaned
        remaining_items=$(find . -mindepth 1 -maxdepth 1 ! -name "home" ! -name "root" 2>/dev/null | wc -l)
        if [ "$remaining_items" -gt 0 ]; then
            log_message "Warning: Some items could not be removed:"
            if [ ! -f "$SILENT_MODE_FILE" ]; then
                ls -la . 2>/dev/null | grep -v -E "^total|^d.*\s\.\s*$|^d.*\s\.\.\s*$|home|root" || true
            fi
        fi
        
        cd - >/dev/null
        
        # Ensure directory exists and has proper permissions
        mkdir -p "$DISTRIBUTION_DIR"
        chmod 755 "$DISTRIBUTION_DIR"
        
        extract_distribution
        
        # Restore user data if backup exists
        if [ -d "$BACKUP_DIR/root" ]; then
            log_message "Restoring user data to new distribution..."
            if [ -d "$DISTRIBUTION_DIR/root" ]; then
                cp -r "$BACKUP_DIR/root/." "$DISTRIBUTION_DIR/root/" 2>/dev/null || true
                log_message "User data restored from previous installation"
            fi
        fi
        
        # Clean up backup
        rm -rf "$BACKUP_DIR" 2>/dev/null || true
    fi
fi

[ ! -e "$PREFIX/local/bin/proot" ] && cp "$PREFIX/files/proot" "$PREFIX/local/bin"

for sofile in "$PREFIX/files/"*.so.2; do
    dest="$PREFIX/local/lib/$(basename "$sofile")"
    [ ! -e "$dest" ] && cp "$sofile" "$dest"
done


ARGS="--kill-on-exit"
ARGS="$ARGS -w /"

for system_mnt in /apex /odm /product /system /system_ext /vendor \
 /linkerconfig/ld.config.txt \
 /linkerconfig/com.android.art/ld.config.txt \
 /plat_property_contexts /property_contexts; do

 if [ -e "$system_mnt" ]; then
  system_mnt=$(realpath "$system_mnt")
  ARGS="$ARGS -b ${system_mnt}"
 fi
done
unset system_mnt

ARGS="$ARGS -b /sdcard"
ARGS="$ARGS -b /storage"
ARGS="$ARGS -b /dev"
ARGS="$ARGS -b /data"
ARGS="$ARGS -b /dev/urandom:/dev/random"
ARGS="$ARGS -b /proc"
ARGS="$ARGS -b $PREFIX"
ARGS="$ARGS -b $PREFIX/local/stat:/proc/stat"
ARGS="$ARGS -b $PREFIX/local/vmstat:/proc/vmstat"

if [ -e "/proc/self/fd" ]; then
  ARGS="$ARGS -b /proc/self/fd:/dev/fd"
fi

if [ -e "/proc/self/fd/0" ]; then
  ARGS="$ARGS -b /proc/self/fd/0:/dev/stdin"
fi

if [ -e "/proc/self/fd/1" ]; then
  ARGS="$ARGS -b /proc/self/fd/1:/dev/stdout"
fi

if [ -e "/proc/self/fd/2" ]; then
  ARGS="$ARGS -b /proc/self/fd/2:/dev/stderr"
fi


ARGS="$ARGS -b $PREFIX"
ARGS="$ARGS -b /sys"

if [ ! -d "$PREFIX/local/distribution/tmp" ]; then
 mkdir -p "$PREFIX/local/distribution/tmp"
 chmod 1777 "$PREFIX/local/distribution/tmp"
fi
ARGS="$ARGS -b $PREFIX/local/distribution/tmp:/dev/shm"

ARGS="$ARGS -r $PREFIX/local/distribution"
ARGS="$ARGS -0"
ARGS="$ARGS --link2symlink"
ARGS="$ARGS --sysvipc"
ARGS="$ARGS -L"

# Determine which init script to use
INIT_SCRIPT="init"
case "$DISTRIBUTION_NAME" in
    "alpine")
        INIT_SCRIPT="init-alpine"
        ;;
    "ubuntu")
        INIT_SCRIPT="init-ubuntu"
        ;;
    "debian")
        INIT_SCRIPT="init-debian"
        ;;
    "arch")
        INIT_SCRIPT="init-arch"
        ;;
    "kali")
        INIT_SCRIPT="init-kali"
        ;;
esac

log_message "Starting $DISTRIBUTION_NAME environment with proot..."
log_message "Distribution directory: $PREFIX/local/distribution"
log_message "Init script: $INIT_SCRIPT"

if [ ! -f "$PREFIX/local/bin/proot" ]; then
    echo "Error: proot binary not found at $PREFIX/local/bin/proot"
    exit 1
fi

if [ ! -f "$PREFIX/local/bin/$INIT_SCRIPT" ]; then
    echo "Error: Init script not found at $PREFIX/local/bin/$INIT_SCRIPT"
    exit 1
fi

# Make proot executable
chmod +x "$PREFIX/local/bin/proot"

# Debug: Show the proot command that will be executed (only in verbose mode)
if [ ! -f "$SILENT_MODE_FILE" ]; then
    echo "Executing: $LINKER $PREFIX/local/bin/proot $ARGS sh $PREFIX/local/bin/$INIT_SCRIPT"
fi

# Execute proot with the appropriate init script
$LINKER $PREFIX/local/bin/proot $ARGS sh $PREFIX/local/bin/$INIT_SCRIPT "$@"
