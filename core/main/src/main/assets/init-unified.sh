#!/bin/bash

# Unified ReTerminal Init Script
# Handles distribution detection, installation, and launches appropriate distribution script

# Set basic variables
DISTRIBUTION_DIR=$PREFIX/local/distribution
ROOT_ENABLED=${ROOT_ENABLED:-false}

# Export standard environment variables
export PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/share/bin:/usr/share/sbin:/usr/local/bin:/usr/local/sbin:/system/bin:/system/xbin
export HOME=/root

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
    
    # Verify distribution extraction created expected directory structure
    log_message "Verifying distribution structure in $DISTRIBUTION_DIR..."
    expected_dirs="bin etc usr var root home dev sys proc tmp"
    missing_dirs=""
    
    for dir in $expected_dirs; do
        if [ ! -d "$DISTRIBUTION_DIR/$dir" ]; then
            missing_dirs="$missing_dirs $dir"
        fi
    done
    
    if [ -n "$missing_dirs" ]; then
        log_message "Warning: Some expected directories are missing:$missing_dirs"
        log_message "Creating missing critical directories..."
        for dir in $missing_dirs; do
            mkdir -p "$DISTRIBUTION_DIR/$dir" 2>/dev/null || true
        done
    fi
    
    log_message "Successfully extracted $DISTRIBUTION_NAME rootfs to $DISTRIBUTION_DIR"
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
    log_message "Distribution content in $DISTRIBUTION_DIR:"
    ls -la "$DISTRIBUTION_DIR" 2>/dev/null || log_message "Could not list distribution directory"
else
    if verify_existing_distribution; then
        if [ ! -f "$SILENT_MODE_FILE" ]; then
            log_message "Using existing $DISTRIBUTION_NAME installation"
            log_message "Distribution content in $DISTRIBUTION_DIR:"
            ls -la "$DISTRIBUTION_DIR" 2>/dev/null || log_message "Could not list distribution directory"
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
    
    # Ensure critical directories exist before mounting
    su -c "mkdir -p $DISTRIBUTION_DIR/dev/shm"
    su -c "mkdir -p $DISTRIBUTION_DIR/dev/pts"
    su -c "mkdir -p $DISTRIBUTION_DIR/tmp"
    
    su -c "busybox mount -o remount,dev,suid /data"
    su -c "busybox mount --bind /dev $DISTRIBUTION_DIR/dev"
    su -c "busybox mount --bind /sys $DISTRIBUTION_DIR/sys"
    su -c "busybox mount --bind /proc $DISTRIBUTION_DIR/proc"
    su -c "busybox mount --bind /dev/pts $DISTRIBUTION_DIR/dev/pts"

    # /dev/shm for Electron apps - ensure directory exists and has correct permissions
    if [ -d "$DISTRIBUTION_DIR/dev/shm" ]; then
        su -c "chmod 1777 $DISTRIBUTION_DIR/dev/shm"
        su -c "busybox mount -t tmpfs -o size=256M tmpfs $DISTRIBUTION_DIR/dev/shm" || log_message "Warning: Could not mount tmpfs on /dev/shm"
    else
        log_message "Warning: Could not create /dev/shm directory"
    fi

    # Create necessary groups
    su -c "chroot $DISTRIBUTION_DIR groupadd -g 3003 aid_inet" 2>/dev/null || true
    su -c "chroot $DISTRIBUTION_DIR groupadd -g 3004 aid_net_raw" 2>/dev/null || true
    su -c "chroot $DISTRIBUTION_DIR groupadd -g 1003 aid_graphics" 2>/dev/null || true

    # Adjust user permissions
    su -c "chroot $DISTRIBUTION_DIR usermod -g 3003 -G 3003,3004 -a _apt" 2>/dev/null || true
    su -c "chroot $DISTRIBUTION_DIR usermod -G 3003 -a root" 2>/dev/null || true
    
    # Fix APT permissions for _apt user to resolve package management issues
    if su -c "chroot $DISTRIBUTION_DIR id _apt" >/dev/null 2>&1; then
        log_message "Configuring APT permissions for _apt user..."
        su -c "chroot $DISTRIBUTION_DIR mkdir -p /var/lib/apt/lists/partial" 2>/dev/null || true
        su -c "chroot $DISTRIBUTION_DIR mkdir -p /var/cache/apt/archives/partial" 2>/dev/null || true
        su -c "chroot $DISTRIBUTION_DIR mkdir -p /var/log/apt" 2>/dev/null || true
        su -c "chroot $DISTRIBUTION_DIR chown -R _apt:root /var/lib/apt /var/cache/apt /var/log/apt" 2>/dev/null || true
        su -c "chroot $DISTRIBUTION_DIR chmod -R 755 /var/lib/apt /var/cache/apt /var/log/apt" 2>/dev/null || true
    fi
fi

## --- Boot logic ---
if [ "$ROOT_ENABLED" = "true" ]; then
    # Ensure the distribution-specific init script is copied into the chroot environment
    INIT_SCRIPT="init-$DISTRIBUTION_NAME"
    if [ ! -f "$PREFIX/local/bin/$INIT_SCRIPT" ]; then
        echo "Error: Distribution init script not found at $PREFIX/local/bin/$INIT_SCRIPT"
        echo "Available scripts:"
        ls -la "$PREFIX/local/bin/init"* 2>/dev/null || echo "  - No init scripts found"
        exit 1
    fi
    
    # Copy the distribution script into the chroot environment
    cp "$PREFIX/local/bin/$INIT_SCRIPT" "$DISTRIBUTION_DIR/tmp/$INIT_SCRIPT"
    chmod +x "$DISTRIBUTION_DIR/tmp/$INIT_SCRIPT"
    
    # Mount SDCard for root users
    if [ -d "/sdcard" ]; then
        su -c "mkdir -p $DISTRIBUTION_DIR/root/sdcard"
        su -c "busybox mount --bind /sdcard $DISTRIBUTION_DIR/root/sdcard" 2>/dev/null || log_message "Warning: Could not mount SDCard"
    fi
    
    # Start distro with chroot and root, then execute distribution script
    log_message "Starting $DISTRIBUTION_NAME environment with chroot (root mode)..."
    if [ "$#" -eq 0 ]; then
        # No arguments passed, run the distribution script for configuration then start shell
        su -c "busybox chroot $DISTRIBUTION_DIR /tmp/$INIT_SCRIPT && /bin/su - root"
    else
        # Arguments passed, run the distribution script then execute the command
        su -c "busybox chroot $DISTRIBUTION_DIR /tmp/$INIT_SCRIPT '$@'"
    fi
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
    if [ ! -d "$DISTRIBUTION_DIR/dev/shm" ]; then
     mkdir -p "$DISTRIBUTION_DIR/dev/shm"
     chmod 1777 "$DISTRIBUTION_DIR/dev/shm"
    fi
    ARGS="$ARGS -b $DISTRIBUTION_DIR/dev/shm:/dev/shm"

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