DISTRIBUTION_DIR=$PREFIX/local/distribution

mkdir -p $DISTRIBUTION_DIR

# Determine which distribution to use based on user selection or available rootfs files
ROOTFS_FILE=""
DISTRIBUTION_NAME=""

# Check if user has selected a specific distribution
if [ -n "$SELECTED_DISTRIBUTION" ]; then
    # Use the user's selected distribution if the rootfs file exists
    if [ -f "$PREFIX/files/${SELECTED_DISTRIBUTION}.tar.gz" ]; then
        ROOTFS_FILE="${SELECTED_DISTRIBUTION}.tar.gz"
        DISTRIBUTION_NAME="$SELECTED_DISTRIBUTION"
    else
        echo "Warning: Selected distribution '$SELECTED_DISTRIBUTION' not found, falling back to available distributions"
    fi
fi

# If no valid selection found, fall back to checking available files
if [ -z "$DISTRIBUTION_NAME" ]; then
    if [ -f "$PREFIX/files/alpine.tar.gz" ]; then
        ROOTFS_FILE="alpine.tar.gz"
        DISTRIBUTION_NAME="alpine"
    elif [ -f "$PREFIX/files/ubuntu.tar.gz" ]; then
        ROOTFS_FILE="ubuntu.tar.gz"
        DISTRIBUTION_NAME="ubuntu"
    elif [ -f "$PREFIX/files/debian.tar.gz" ]; then
        ROOTFS_FILE="debian.tar.gz"
        DISTRIBUTION_NAME="debian"
    elif [ -f "$PREFIX/files/arch.tar.gz" ]; then
        ROOTFS_FILE="arch.tar.gz"
        DISTRIBUTION_NAME="arch"
    elif [ -f "$PREFIX/files/kali.tar.gz" ]; then
        ROOTFS_FILE="kali.tar.gz"
        DISTRIBUTION_NAME="kali"
    else
        echo "No distribution rootfs found!"
        echo "Expected one of: alpine.tar.gz, ubuntu.tar.gz, debian.tar.gz, arch.tar.gz, kali.tar.gz"
        echo "Available files in $PREFIX/files/:"
        ls -la "$PREFIX/files/" 2>/dev/null || echo "  - Directory not found"
        exit 1
    fi
fi

# Function to extract and verify distribution
extract_distribution() {
    echo "Extracting $DISTRIBUTION_NAME rootfs from $ROOTFS_FILE..."
    if ! tar -xf "$PREFIX/files/$ROOTFS_FILE" -C "$DISTRIBUTION_DIR" 2>/dev/null; then
        echo "Error: Failed to extract rootfs from $PREFIX/files/$ROOTFS_FILE"
        echo "File details:"
        ls -la "$PREFIX/files/$ROOTFS_FILE" 2>/dev/null || echo "  - File not found"
        echo "Available files in $PREFIX/files/:"
        ls -la "$PREFIX/files/" 2>/dev/null | head -10
        exit 1
    fi
    echo "Successfully extracted $DISTRIBUTION_NAME rootfs"
    
    # Verify critical directories exist
    for dir in bin usr/bin sbin usr/sbin; do
        if [ ! -d "$DISTRIBUTION_DIR/$dir" ]; then
            echo "Warning: Expected directory $dir not found in extracted rootfs"
        fi
    done
    
    # For Ubuntu/Debian, verify apt is available
    if [ "$DISTRIBUTION_NAME" = "ubuntu" ] || [ "$DISTRIBUTION_NAME" = "debian" ]; then
        if [ ! -f "$DISTRIBUTION_DIR/usr/bin/apt" ] && [ ! -f "$DISTRIBUTION_DIR/usr/bin/apt-get" ]; then
            echo "Warning: Neither apt nor apt-get found in extracted $DISTRIBUTION_NAME rootfs"
            echo "Available binaries in usr/bin:"
            ls "$DISTRIBUTION_DIR/usr/bin/" 2>/dev/null | grep -E "(apt|dpkg)" || echo "  - No apt/dpkg binaries found"
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

if [ -z "$(ls -A "$DISTRIBUTION_DIR" | grep -vE '^(root|tmp)$')" ]; then
    # Directory is empty, extract the distribution
    extract_distribution
else
    # Directory exists, verify it contains the correct distribution
    if verify_existing_distribution; then
        echo "Using existing $DISTRIBUTION_NAME installation"
    else
        echo "Existing installation does not match selected distribution ($DISTRIBUTION_NAME)"
        if [ -f "$DISTRIBUTION_DIR/etc/os-release" ]; then
            existing_id=$(grep "^ID=" "$DISTRIBUTION_DIR/etc/os-release" 2>/dev/null | cut -d'=' -f2 | tr -d '"' 2>/dev/null || echo "unknown")
            if [ -n "$existing_id" ] && [ "$existing_id" != "unknown" ]; then
                echo "Found existing distribution: $existing_id (expected: $DISTRIBUTION_NAME)"
            else
                echo "Existing distribution could not be determined from /etc/os-release"
            fi
        else
            echo "No /etc/os-release found in existing installation"
        fi
        echo "Clearing existing installation and extracting correct distribution..."
        # Clear existing installation (but preserve tmp directory)
        find "$DISTRIBUTION_DIR" -mindepth 1 -maxdepth 1 ! -name 'tmp' -exec rm -rf {} + 2>/dev/null || true
        extract_distribution
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

echo "Starting $DISTRIBUTION_NAME environment with proot..."
echo "Distribution directory: $PREFIX/local/distribution"
echo "Init script: $INIT_SCRIPT"

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

# Debug: Show the proot command that will be executed
echo "Executing: $LINKER $PREFIX/local/bin/proot $ARGS sh $PREFIX/local/bin/$INIT_SCRIPT"

# Execute proot with the appropriate init script
$LINKER $PREFIX/local/bin/proot $ARGS sh $PREFIX/local/bin/$INIT_SCRIPT "$@"
