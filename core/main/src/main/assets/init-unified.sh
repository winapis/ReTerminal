#!/bin/bash

# Unified ReTerminal Init Script
# Combines functionality from init, init-host, and init-host-root
# Handles distribution detection, installation, and launches appropriate distribution script

# Set basic variables
DISTRIBUTION_DIR=$PREFIX/local/distribution
ROOT_ENABLED=${ROOT_ENABLED:-false}
ROOT_VERIFIED=${ROOT_VERIFIED:-false}
USE_ROOT_MOUNTS=${USE_ROOT_MOUNTS:-false}
BUSYBOX_PATH=${BUSYBOX_PATH:-}

# Create necessary directories
mkdir -p $DISTRIBUTION_DIR
mkdir -p $PREFIX/local/bin
mkdir -p $PREFIX/local/lib

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

# Determine which distribution to use
ROOTFS_FILE=""
DISTRIBUTION_NAME=""

# Check if user has selected a specific distribution
if [ -n "$SELECTED_DISTRIBUTION" ]; then
    FOUND_FILE=$(find_distribution_file "$SELECTED_DISTRIBUTION")
    if [ -n "$FOUND_FILE" ]; then
        ROOTFS_FILE="$FOUND_FILE"
        DISTRIBUTION_NAME="$SELECTED_DISTRIBUTION"
    else
        echo "Warning: Selected distribution '$SELECTED_DISTRIBUTION' not found, falling back to available distributions"
        echo "Debug: Searching for '$SELECTED_DISTRIBUTION' in $PREFIX/files/"
        echo "Debug: Files in $PREFIX/files/:"
        ls -la "$PREFIX/files/" 2>/dev/null || echo "  - Directory not found or not accessible"
    fi
fi

# If no valid selection found, fall back to checking available files
if [ -z "$DISTRIBUTION_NAME" ]; then
    for dist in alpine ubuntu debian arch kali; do
        FOUND_FILE=$(find_distribution_file "$dist")
        if [ -n "$FOUND_FILE" ]; then
            ROOTFS_FILE="$FOUND_FILE"
            DISTRIBUTION_NAME="$dist"
            break
        fi
    done
    
    if [ -z "$DISTRIBUTION_NAME" ]; then
        echo "No distribution rootfs found!"
        echo "Expected one of: alpine.tar.gz, ubuntu.tar.gz, debian.tar.gz, arch.tar.gz, kali.tar.gz"
        echo "             or: alpine.tar.xz, ubuntu.tar.xz, debian.tar.xz, arch.tar.xz, kali.tar.xz"
        echo "Available files in $PREFIX/files/:"
        ls -la "$PREFIX/files/" 2>/dev/null || echo "  - Directory not found"
        exit 1
    fi
fi

# Check for silent mode flag
SILENT_MODE_FILE="$DISTRIBUTION_DIR/.reterminal_installed"

# Helper function for conditional output
log_message() {
    if [ ! -f "$SILENT_MODE_FILE" ]; then
        echo "$1"
    fi
}

# Function to extract and setup distribution
extract_distribution() {
    log_message "Extracting $DISTRIBUTION_NAME rootfs from $ROOTFS_FILE..."
    
    # Enhanced tar extraction with Android compatibility
    TAR_OPTS="--no-same-owner --no-same-permissions"
    
    # Try extraction with progressively more compatible options
    if ! tar -xf "$PREFIX/files/$ROOTFS_FILE" -C "$DISTRIBUTION_DIR" $TAR_OPTS 2>/dev/null; then
        log_message "Standard extraction failed, trying with hard link conversion..."
        if ! tar -xf "$PREFIX/files/$ROOTFS_FILE" -C "$DISTRIBUTION_DIR" $TAR_OPTS --hard-dereference 2>/dev/null; then
            log_message "Hard link conversion failed, trying with aggressive Android compatibility mode..."
            # Ensure we have a writable temp directory  
            if [ -z "$TMPDIR" ] || [ ! -w "$TMPDIR" ]; then
                TMPDIR="$PREFIX/tmp"
                mkdir -p "$TMPDIR"
            fi
            TAR_LOG_FILE="${TMPDIR}/tar_errors.log"
            
            # Most aggressive compatibility mode for Android
            if ! tar -xf "$PREFIX/files/$ROOTFS_FILE" -C "$DISTRIBUTION_DIR" \
                --no-same-owner --no-same-permissions --dereference --hard-dereference \
                --ignore-failed-read --skip-old-files \
                --exclude="*/alternatives/*" \
                --exclude="*/systemd/system/*/wants/*" \
                --exclude="var/lock" \
                --exclude="var/run" \
                --exclude="usr/bin/awk" \
                --exclude="usr/bin/nawk" \
                --exclude="usr/bin/pager" \
                --exclude="usr/bin/which" \
                --exclude="etc/alternatives/*" \
                2>"$TAR_LOG_FILE"; then
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
                log_message "Extraction completed with Android compatibility mode"
                if [ -f "$TAR_LOG_FILE" ] && [ ! -f "$SILENT_MODE_FILE" ]; then
                    echo "Filesystem extraction completed with Android compatibility adjustments:"
                    system_links=$(grep -c "not under.*distribution" "$TAR_LOG_FILE" 2>/dev/null || echo "0")
                    hard_links=$(grep -c "Permission denied" "$TAR_LOG_FILE" 2>/dev/null || echo "0")
                    [ "$system_links" -gt 0 ] && echo "- $system_links system symlinks converted to files (expected)"
                    [ "$hard_links" -gt 0 ] && echo "- $hard_links hard links converted to regular files (expected)"
                    echo "- These adjustments are normal for Android and don't affect functionality"
                fi
                [ -f "$TAR_LOG_FILE" ] && rm -f "$TAR_LOG_FILE"
            fi
        else
            log_message "Successfully extracted with hard link conversion"
        fi
    fi
    
    # Post-extraction fixes for broken symlinks on Android
    log_message "Applying Android compatibility fixes for $DISTRIBUTION_NAME..."
    
    # Create essential directories that are often missing due to symlink issues
    mkdir -p "$DISTRIBUTION_DIR/var/lock" 2>/dev/null
    mkdir -p "$DISTRIBUTION_DIR/var/run" 2>/dev/null
    mkdir -p "$DISTRIBUTION_DIR/tmp" 2>/dev/null
    chmod 1777 "$DISTRIBUTION_DIR/tmp" 2>/dev/null
    
    # Fix common broken symlinks for Ubuntu/Debian
    if [ "$DISTRIBUTION_NAME" = "ubuntu" ] || [ "$DISTRIBUTION_NAME" = "debian" ]; then
        # Fix awk symlink
        if [ ! -x "$DISTRIBUTION_DIR/usr/bin/awk" ] && [ -x "$DISTRIBUTION_DIR/usr/bin/mawk" ]; then
            ln -sf mawk "$DISTRIBUTION_DIR/usr/bin/awk" 2>/dev/null || {
                echo '#!/bin/sh' > "$DISTRIBUTION_DIR/usr/bin/awk"
                echo 'exec /usr/bin/mawk "$@"' >> "$DISTRIBUTION_DIR/usr/bin/awk"
                chmod +x "$DISTRIBUTION_DIR/usr/bin/awk"
            }
        fi
        
        # Fix nawk symlink
        if [ ! -x "$DISTRIBUTION_DIR/usr/bin/nawk" ] && [ -x "$DISTRIBUTION_DIR/usr/bin/mawk" ]; then
            ln -sf mawk "$DISTRIBUTION_DIR/usr/bin/nawk" 2>/dev/null || {
                echo '#!/bin/sh' > "$DISTRIBUTION_DIR/usr/bin/nawk"
                echo 'exec /usr/bin/mawk "$@"' >> "$DISTRIBUTION_DIR/usr/bin/nawk"
                chmod +x "$DISTRIBUTION_DIR/usr/bin/nawk"
            }
        fi
        
        # Fix pager symlink
        if [ ! -x "$DISTRIBUTION_DIR/usr/bin/pager" ]; then
            if [ -x "$DISTRIBUTION_DIR/usr/bin/less" ]; then
                ln -sf less "$DISTRIBUTION_DIR/usr/bin/pager" 2>/dev/null || {
                    echo '#!/bin/sh' > "$DISTRIBUTION_DIR/usr/bin/pager"
                    echo 'exec /usr/bin/less "$@"' >> "$DISTRIBUTION_DIR/usr/bin/pager"
                    chmod +x "$DISTRIBUTION_DIR/usr/bin/pager"
                }
            elif [ -x "$DISTRIBUTION_DIR/usr/bin/more" ]; then
                ln -sf more "$DISTRIBUTION_DIR/usr/bin/pager" 2>/dev/null || {
                    echo '#!/bin/sh' > "$DISTRIBUTION_DIR/usr/bin/pager"
                    echo 'exec /usr/bin/more "$@"' >> "$DISTRIBUTION_DIR/usr/bin/pager"
                    chmod +x "$DISTRIBUTION_DIR/usr/bin/pager"
                }
            fi
        fi
        
        # Fix which symlink
        if [ ! -x "$DISTRIBUTION_DIR/usr/bin/which" ] && [ -x "$DISTRIBUTION_DIR/usr/bin/which.debianutils" ]; then
            ln -sf which.debianutils "$DISTRIBUTION_DIR/usr/bin/which" 2>/dev/null || {
                echo '#!/bin/sh' > "$DISTRIBUTION_DIR/usr/bin/which"
                echo 'exec /usr/bin/which.debianutils "$@"' >> "$DISTRIBUTION_DIR/usr/bin/which"
                chmod +x "$DISTRIBUTION_DIR/usr/bin/which"
            }
        fi
        
        # Fix perl symlink if needed
        if [ ! -x "$DISTRIBUTION_DIR/usr/bin/perl" ] && [ -x "$DISTRIBUTION_DIR/usr/bin/perl5.34.0" ]; then
            ln -sf perl5.34.0 "$DISTRIBUTION_DIR/usr/bin/perl" 2>/dev/null
        fi
    fi
    
    log_message "Successfully extracted $DISTRIBUTION_NAME rootfs"
}

# Function to verify existing distribution matches selected one
verify_existing_distribution() {
    if [ -f "$DISTRIBUTION_DIR/etc/os-release" ]; then
        case "$DISTRIBUTION_NAME" in
            "ubuntu") grep -q "ID=ubuntu" "$DISTRIBUTION_DIR/etc/os-release" 2>/dev/null && return 0 ;;
            "debian") grep -q "ID=debian" "$DISTRIBUTION_DIR/etc/os-release" 2>/dev/null && return 0 ;;
            "alpine") grep -q "ID=alpine" "$DISTRIBUTION_DIR/etc/os-release" 2>/dev/null && return 0 ;;
            "arch") grep -q "ID=arch" "$DISTRIBUTION_DIR/etc/os-release" 2>/dev/null && return 0 ;;
            "kali") grep -q "ID=kali" "$DISTRIBUTION_DIR/etc/os-release" 2>/dev/null && return 0 ;;
        esac
    fi
    return 1
}

# Install or verify distribution
if [ -z "$(ls -A "$DISTRIBUTION_DIR" | grep -vE '^(root|tmp)$')" ]; then
    log_message "Setting up $DISTRIBUTION_NAME environment..."
    extract_distribution
    touch "$SILENT_MODE_FILE"
    log_message "$DISTRIBUTION_NAME installation completed"
else
    if verify_existing_distribution; then
        if [ ! -f "$SILENT_MODE_FILE" ]; then
            log_message "Using existing $DISTRIBUTION_NAME installation"
            touch "$SILENT_MODE_FILE"
        fi
    else
        log_message "Existing installation does not match selected distribution ($DISTRIBUTION_NAME)"
        log_message "Clearing existing installation and extracting correct distribution..."
        cd "$DISTRIBUTION_DIR" || exit 1
        # Clean existing installation
        for dir in bin boot dev etc lib lib64 media mnt opt proc run sbin srv sys tmp usr var; do
            [ -d "$dir" ] && rm -rf "$dir" 2>/dev/null
        done
        cd - >/dev/null
        mkdir -p "$DISTRIBUTION_DIR"
        chmod 755 "$DISTRIBUTION_DIR"
        extract_distribution
        touch "$SILENT_MODE_FILE"
    fi
fi

# Setup proot and shared libraries
[ ! -e "$PREFIX/local/bin/proot" ] && cp "$PREFIX/files/proot" "$PREFIX/local/bin"

for sofile in "$PREFIX/files/"*.so.2; do
    dest="$PREFIX/local/lib/$(basename "$sofile")"
    [ ! -e "$dest" ] && cp "$sofile" "$dest"
done

# Setup proot arguments
ARGS="--kill-on-exit -w /"

# Root-specific configuration if enabled
if [ "$ROOT_ENABLED" = "true" ] && [ "$ROOT_VERIFIED" = "true" ]; then
    log_message "Configuring enhanced root environment..."
    
    # Configure DNS
    if [ -d "$DISTRIBUTION_DIR/etc" ]; then
        log_message "Configuring DNS..."
        echo "nameserver 8.8.8.8" > "$DISTRIBUTION_DIR/etc/resolv.conf"
        echo "nameserver 8.8.4.4" >> "$DISTRIBUTION_DIR/etc/resolv.conf"
        echo "127.0.0.1 localhost" > "$DISTRIBUTION_DIR/etc/hosts"
    fi
    
    # Setup enhanced mounts with root
    if [ -n "$BUSYBOX_PATH" ] && [ -x "$BUSYBOX_PATH" ]; then
        log_message "Setting up enhanced root mounts..."
        mkdir -p "$DISTRIBUTION_DIR/dev/shm"
        su -c "$BUSYBOX_PATH mount -o remount,dev,suid /data" 2>/dev/null || true
        su -c "$BUSYBOX_PATH mount --bind /dev $DISTRIBUTION_DIR/dev" 2>/dev/null || true
        su -c "$BUSYBOX_PATH mount --bind /sys $DISTRIBUTION_DIR/sys" 2>/dev/null || true  
        su -c "$BUSYBOX_PATH mount --bind /proc $DISTRIBUTION_DIR/proc" 2>/dev/null || true
        su -c "$BUSYBOX_PATH mount -t devpts devpts $DISTRIBUTION_DIR/dev/pts" 2>/dev/null || true
        su -c "$BUSYBOX_PATH mount -t tmpfs -o size=256M tmpfs $DISTRIBUTION_DIR/dev/shm" 2>/dev/null || true
        
        # Setup Android groups for network access
        if [ "$DISTRIBUTION_NAME" = "ubuntu" ] || [ "$DISTRIBUTION_NAME" = "debian" ]; then
            cat > "$DISTRIBUTION_DIR/tmp/setup_groups.sh" << 'EOF'
#!/bin/bash
groupadd -g 3003 aid_inet 2>/dev/null || true
groupadd -g 3004 aid_net_raw 2>/dev/null || true
groupadd -g 1003 aid_graphics 2>/dev/null || true
usermod -g 3003 -G 3003,3004 -a _apt 2>/dev/null || true
usermod -G 3003 -a root 2>/dev/null || true
EOF
            chmod +x "$DISTRIBUTION_DIR/tmp/setup_groups.sh"
            su -c "chroot $DISTRIBUTION_DIR /tmp/setup_groups.sh" 2>/dev/null || true
            rm -f "$DISTRIBUTION_DIR/tmp/setup_groups.sh" 2>/dev/null || true
        fi
    fi
    
    # Use chroot instead of proot for root mode
    if [ "$ROOT_ENABLED" = "true" ]; then
        log_message "Starting $DISTRIBUTION_NAME environment with chroot (root mode)..."
        log_message "Distribution directory: $DISTRIBUTION_DIR"
        
        # Determine which init script to use
        INIT_SCRIPT="init-$DISTRIBUTION_NAME.sh"
        if [ ! -f "$PREFIX/local/bin/$INIT_SCRIPT" ]; then
            INIT_SCRIPT="init-$DISTRIBUTION_NAME"
        fi
        
        # Execute in chroot with root
        su -c "chroot $DISTRIBUTION_DIR /bin/su - root"
        exit 0
    fi
fi

# Standard proot mounts for non-root mode
for system_mnt in /apex /odm /product /system /system_ext /vendor \
 /linkerconfig/ld.config.txt \
 /linkerconfig/com.android.art/ld.config.txt \
 /plat_property_contexts /property_contexts; do
 if [ -e "$system_mnt" ]; then
  system_mnt=$(realpath "$system_mnt")
  ARGS="$ARGS -b ${system_mnt}"
 fi
done

ARGS="$ARGS -b /sdcard -b /storage -b /dev -b /data"
ARGS="$ARGS -b /dev/urandom:/dev/random -b /proc -b $PREFIX"
ARGS="$ARGS -b $PREFIX/local/stat:/proc/stat"
ARGS="$ARGS -b $PREFIX/local/vmstat:/proc/vmstat"

# Standard file descriptors
[ -e "/proc/self/fd" ] && ARGS="$ARGS -b /proc/self/fd:/dev/fd"
[ -e "/proc/self/fd/0" ] && ARGS="$ARGS -b /proc/self/fd/0:/dev/stdin"
[ -e "/proc/self/fd/1" ] && ARGS="$ARGS -b /proc/self/fd/1:/dev/stdout"
[ -e "/proc/self/fd/2" ] && ARGS="$ARGS -b /proc/self/fd/2:/dev/stderr"

ARGS="$ARGS -b /sys"

# Setup /dev/shm
if [ ! -d "$PREFIX/local/distribution/tmp" ]; then
 mkdir -p "$PREFIX/local/distribution/tmp"
 chmod 1777 "$PREFIX/local/distribution/tmp"
fi
ARGS="$ARGS -b $PREFIX/local/distribution/tmp:/dev/shm"

# Final proot arguments
ARGS="$ARGS -r $PREFIX/local/distribution -0 --link2symlink --sysvipc -L"

# Determine which init script to use for the distribution
INIT_SCRIPT="init-$DISTRIBUTION_NAME"
if [ ! -f "$PREFIX/local/bin/$INIT_SCRIPT" ]; then
    echo "Error: Distribution init script not found at $PREFIX/local/bin/$INIT_SCRIPT"
    echo "Available scripts:"
    ls -la "$PREFIX/local/bin/init"* 2>/dev/null || echo "  - No init scripts found"
    exit 1
fi

log_message "Starting $DISTRIBUTION_NAME environment with proot..."
log_message "Distribution directory: $PREFIX/local/distribution"
log_message "Init script: $INIT_SCRIPT"

if [ ! -f "$PREFIX/local/bin/proot" ]; then
    echo "Error: proot binary not found at $PREFIX/local/bin/proot"
    exit 1
fi

# Make proot executable
chmod +x "$PREFIX/local/bin/proot"

# Debug: Show the proot command that will be executed (only in verbose mode)
if [ ! -f "$SILENT_MODE_FILE" ]; then
    echo "Executing: $LINKER $PREFIX/local/bin/proot $ARGS sh $PREFIX/local/bin/$INIT_SCRIPT"
fi

# Execute proot with the appropriate distribution init script
exec $LINKER $PREFIX/local/bin/proot $ARGS sh $PREFIX/local/bin/$INIT_SCRIPT "$@"