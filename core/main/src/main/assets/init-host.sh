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
        exit 1
    fi
fi

if [ -z "$(ls -A "$DISTRIBUTION_DIR" | grep -vE '^(root|tmp)$')" ]; then
    tar -xf "$PREFIX/files/$ROOTFS_FILE" -C "$DISTRIBUTION_DIR"
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

$LINKER $PREFIX/local/bin/proot $ARGS sh $PREFIX/local/bin/$INIT_SCRIPT "$@"
