#!/bin/bash

# Enhanced init-host script with root support
# This script provides additional mounts, DNS configuration, and Android groups when root is available

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
        echo "Debug: Searching for '$SELECTED_DISTRIBUTION' in $PREFIX/files/"
        echo "Debug: Files in $PREFIX/files/:"
        ls -la "$PREFIX/files/" 2>/dev/null || echo "  - Directory not found or not accessible"
        echo "Debug: Looking for: ${SELECTED_DISTRIBUTION}.tar.gz or ${SELECTED_DISTRIBUTION}.tar.xz"
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
    TAR_OPTS="--no-same-owner --no-same-permissions"
    
    # Try extraction with different strategies if hard links fail
    if ! tar -xf "$PREFIX/files/$ROOTFS_FILE" -C "$DISTRIBUTION_DIR" $TAR_OPTS 2>/dev/null; then
        log_message "Standard extraction failed, trying with hard link conversion..."
        if ! tar -xf "$PREFIX/files/$ROOTFS_FILE" -C "$DISTRIBUTION_DIR" $TAR_OPTS --hard-dereference 2>/dev/null; then
            log_message "Hard link conversion failed, trying without link preservation..."
            # Final attempt: ignore link creation errors and use app-writable temp directory
            # Ensure we have a writable temp directory
            if [ -z "$TMPDIR" ] || [ ! -w "$TMPDIR" ]; then
                TMPDIR="$PREFIX/tmp"
                mkdir -p "$TMPDIR"
            fi
            TAR_LOG_FILE="${TMPDIR}/tar_errors.log"
            if ! tar -xf "$PREFIX/files/$ROOTFS_FILE" -C "$DISTRIBUTION_DIR" $TAR_OPTS --warning=no-file-ignored 2>"$TAR_LOG_FILE"; then
                echo "Error: Failed to extract rootfs from $PREFIX/files/$ROOTFS_FILE"
                echo "File details:"
                ls -la "$PREFIX/files/$ROOTFS_FILE" 2>/dev/null || echo "  - File not found"
                echo "Available files in $PREFIX/files/:"
                ls -la "$PREFIX/files/" 2>/dev/null | head -10
                echo "Target directory status:"
                ls -la "$DISTRIBUTION_DIR/" 2>/dev/null || echo "  - Directory not accessible"
                if [ -f "$TAR_LOG_FILE" ]; then
                    echo "Tar extraction errors:"
                    cat "$TAR_LOG_FILE"
                    rm -f "$TAR_LOG_FILE"
                fi
                exit 1
            else
                log_message "Warning: Extraction completed with some link creation errors (non-critical)"
                if [ -f "$TAR_LOG_FILE" ]; then
                    log_message "Note: Some hard links were converted to file copies for Android compatibility"
                    rm -f "$TAR_LOG_FILE"
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
}

# Function to setup root-specific configuration
setup_root_configuration() {
    if [ "$ROOT_ENABLED" = "true" ] && [ "$ROOT_VERIFIED" = "true" ]; then
        log_message "Configuring enhanced root environment..."
        
        # Remount /data with dev and suid options if BusyBox is available
        if [ -n "$BUSYBOX_PATH" ] && [ -x "$BUSYBOX_PATH" ]; then
            log_message "Using BusyBox at $BUSYBOX_PATH for enhanced mounts..."
            
            # Enhanced mount options for root
            su -c "$BUSYBOX_PATH mount -o remount,dev,suid /data" 2>/dev/null || {
                log_message "Warning: Could not remount /data with enhanced options"
            }
        fi
        
        # Setup DNS in the chroot environment
        if [ -d "$DISTRIBUTION_DIR/etc" ]; then
            log_message "Configuring DNS..."
            su -c "echo 'nameserver 8.8.8.8' > $DISTRIBUTION_DIR/etc/resolv.conf" 2>/dev/null || {
                log_message "Warning: Could not configure DNS"
            }
            su -c "echo 'nameserver 8.8.4.4' >> $DISTRIBUTION_DIR/etc/resolv.conf" 2>/dev/null || true
            su -c "echo 'nameserver 1.1.1.1' >> $DISTRIBUTION_DIR/etc/resolv.conf" 2>/dev/null || true
        fi
        
        # Add Android root groups if supported
        if [ -d "$DISTRIBUTION_DIR/etc" ]; then
            log_message "Adding Android groups..."
            
            # Create Android-specific groups for network and device access
            ANDROID_GROUPS="
groupadd -g 3001 aid_bt 2>/dev/null || true
groupadd -g 3002 aid_bt_net 2>/dev/null || true  
groupadd -g 3003 aid_inet 2>/dev/null || true
groupadd -g 3004 aid_net_raw 2>/dev/null || true
groupadd -g 3005 aid_admin 2>/dev/null || true
groupadd -g 3006 aid_radio 2>/dev/null || true
groupadd -g 3007 aid_nfc 2>/dev/null || true
groupadd -g 3008 aid_drmrpc 2>/dev/null || true
groupadd -g 3009 aid_vpn 2>/dev/null || true
groupadd -g 3010 aid_media_rw 2>/dev/null || true
"
            
            # Add groups to root user
            ADD_USER_TO_GROUPS="
usermod -a -G aid_bt,aid_bt_net,aid_inet,aid_net_raw,aid_admin,aid_radio,aid_nfc,aid_drmrpc,aid_vpn,aid_media_rw root 2>/dev/null || true
"
            
            # For Debian-based distros, fix _apt user group
            if [ "$DISTRIBUTION_NAME" = "ubuntu" ] || [ "$DISTRIBUTION_NAME" = "debian" ]; then
                DEBIAN_APT_FIX="usermod -g aid_inet _apt 2>/dev/null || true"
            else
                DEBIAN_APT_FIX=""
            fi
            
            # Execute group setup commands in chroot
            ROOT_SETUP_SCRIPT="$DISTRIBUTION_DIR/tmp/root_setup.sh"
            cat > "$ROOT_SETUP_SCRIPT" << EOF
#!/bin/bash
$ANDROID_GROUPS
$ADD_USER_TO_GROUPS
$DEBIAN_APT_FIX
EOF
            chmod +x "$ROOT_SETUP_SCRIPT"
            
            # Execute setup script in chroot environment
            su -c "chroot $DISTRIBUTION_DIR /tmp/root_setup.sh" 2>/dev/null || {
                log_message "Warning: Could not setup Android groups (will continue without them)"
            }
            
            # Clean up setup script
            rm -f "$ROOT_SETUP_SCRIPT" 2>/dev/null || true
        fi
        
        log_message "Root configuration completed"
    else
        log_message "Root not available - using standard proot mode"
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
    setup_root_configuration
    # Mark installation as complete for silent mode
    touch "$SILENT_MODE_FILE"
    log_message "$DISTRIBUTION_NAME installation completed"
else
    # Directory exists, verify it contains the correct distribution
    if verify_existing_distribution; then
        if [ ! -f "$SILENT_MODE_FILE" ]; then
            log_message "Using existing $DISTRIBUTION_NAME installation"
            # Setup root configuration for existing installation
            setup_root_configuration
            # Mark installation as complete for future silent mode
            touch "$SILENT_MODE_FILE"
        fi
    else
        log_message "Existing installation does not match selected distribution ($DISTRIBUTION_NAME)"
        # Clear and reinstall with proper distribution
        log_message "Clearing existing installation and extracting correct distribution..."
        cd "$DISTRIBUTION_DIR" || exit 1
        
        # Clean existing installation
        for dir in bin boot dev etc lib lib64 media mnt opt proc run sbin srv sys tmp usr var; do
            if [ -d "$dir" ]; then
                rm -rf "$dir" 2>/dev/null || true
            fi
        done
        
        cd - >/dev/null
        mkdir -p "$DISTRIBUTION_DIR"
        chmod 755 "$DISTRIBUTION_DIR"
        
        extract_distribution
        setup_root_configuration
    fi
fi

[ ! -e "$PREFIX/local/bin/proot" ] && cp "$PREFIX/files/proot" "$PREFIX/local/bin"

for sofile in "$PREFIX/files/"*.so.2; do
    dest="$PREFIX/local/lib/$(basename "$sofile")"
    [ ! -e "$dest" ] && cp "$sofile" "$dest"
done

ARGS="--kill-on-exit"
ARGS="$ARGS -w /"

# Enhanced mounts for root mode
if [ "$ROOT_ENABLED" = "true" ] && [ "$USE_ROOT_MOUNTS" = "true" ] && [ "$ROOT_VERIFIED" = "true" ]; then
    log_message "Using enhanced root mounts..."
    
    # Additional bind mounts with root privileges
    for system_mnt in /apex /odm /product /system /system_ext /vendor \
     /linkerconfig/ld.config.txt \
     /linkerconfig/com.android.art/ld.config.txt \
     /plat_property_contexts /property_contexts; do
    
     if [ -e "$system_mnt" ]; then
      system_mnt=$(realpath "$system_mnt")
      ARGS="$ARGS -b ${system_mnt}"
     fi
    done
    
    # Enhanced device mounts
    ARGS="$ARGS -b /dev"
    ARGS="$ARGS -b /dev/pts"
    ARGS="$ARGS -b /sys"
    ARGS="$ARGS -b /proc"
    
else
    # Standard mounts for rootless mode
    for system_mnt in /apex /odm /product /system /system_ext /vendor \
     /linkerconfig/ld.config.txt \
     /linkerconfig/com.android.art/ld.config.txt \
     /plat_property_contexts /property_contexts; do
    
     if [ -e "$system_mnt" ]; then
      system_mnt=$(realpath "$system_mnt")
      ARGS="$ARGS -b ${system_mnt}"
     fi
    done
    
    ARGS="$ARGS -b /dev"
    ARGS="$ARGS -b /proc"
    ARGS="$ARGS -b /sys"
fi

unset system_mnt

ARGS="$ARGS -b /sdcard"
ARGS="$ARGS -b /storage"
ARGS="$ARGS -b /data"
ARGS="$ARGS -b /dev/urandom:/dev/random"
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
if [ "$ROOT_ENABLED" = "true" ]; then
    log_message "Root mode: ENABLED with enhanced mounts"
else
    log_message "Root mode: DISABLED (rootless mode)"
fi

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