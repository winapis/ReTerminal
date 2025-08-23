#!/bin/bash

# Unified ReTerminal Init Script
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

# Function to extract distribution using simple tar command
extract_distribution() {
    log_message "Extracting $DISTRIBUTION_NAME rootfs from $ROOTFS_FILE..."
    
    # Simple tar extraction as requested by user - no complex exclusions
    if ! tar xpvf "$PREFIX/files/$ROOTFS_FILE" --numeric-owner -C "$DISTRIBUTION_DIR" 2>/dev/null; then
        echo "Error: Failed to extract rootfs from $PREFIX/files/$ROOTFS_FILE"
        echo "File details:"
        ls -la "$PREFIX/files/$ROOTFS_FILE" 2>/dev/null || echo "  - File not found"
        echo "Available files in $PREFIX/files/:"
        ls -la "$PREFIX/files/" 2>/dev/null
        echo "Target directory status:"
        ls -la "$DISTRIBUTION_DIR/" 2>/dev/null || echo "  - Directory not accessible"
        exit 1
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
if [ -z "$(ls -A "$DISTRIBUTION_DIR" 2>/dev/null)" ]; then
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
        rm -rf "$DISTRIBUTION_DIR"/*
        mkdir -p "$DISTRIBUTION_DIR"
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

# Configure DNS and hosts
echo "nameserver 8.8.8.8" > "$DISTRIBUTION_DIR/etc/resolv.conf"
echo "127.0.0.1 localhost" > "$DISTRIBUTION_DIR/etc/hosts"

## --- Root-specific preparation ---
if [ "$ROOT_ENABLED" = "true" ]; then
    log_message "Configuring root environment..."
    
    su -c "mkdir -p $DISTRIBUTION_DIR/dev/shm"
    su -c "busybox mount -o remount,dev,suid /data"
    su -c "busybox mount --bind /dev $DISTRIBUTION_DIR/dev"
    su -c "busybox mount --bind /sys $DISTRIBUTION_DIR/sys"
    su -c "busybox mount --bind /proc $DISTRIBUTION_DIR/proc"
    su -c "busybox mount -t devpts devpts $DISTRIBUTION_DIR/dev/pts"

    # /dev/shm for Electron apps
    su -c "busybox mount -t tmpfs -o size=256M tmpfs $DISTRIBUTION_DIR/dev/shm"

    # Create necessary groups
    su -c "chroot $DISTRIBUTION_DIR groupadd -g 3003 aid_inet" 2>/dev/null || true
    su -c "chroot $DISTRIBUTION_DIR groupadd -g 3004 aid_net_raw" 2>/dev/null || true
    su -c "chroot $DISTRIBUTION_DIR groupadd -g 1003 aid_graphics" 2>/dev/null || true

    # Adjust user permissions
    su -c "chroot $DISTRIBUTION_DIR usermod -g 3003 -G 3003,3004 -a _apt" 2>/dev/null || true
    su -c "chroot $DISTRIBUTION_DIR usermod -G 3003 -a root" 2>/dev/null || true
fi

## --- Boot logic ---
if [ "$ROOT_ENABLED" = "true" ]; then
    # Start distro with chroot and root
    log_message "Starting $DISTRIBUTION_NAME environment with chroot (root mode)..."
    su -c "busybox chroot $DISTRIBUTION_DIR /bin/su - root"
else
    # Setup proot arguments for non-root mode
    ARGS="--kill-on-exit -w /"

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

    # Execute proot with the appropriate distribution init script
    exec $LINKER $PREFIX/local/bin/proot $ARGS sh $PREFIX/local/bin/$INIT_SCRIPT "$@"
fi