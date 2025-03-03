set -e

WORKING_DIR=/data/local/tmp
DOWNLOAD_DIR=/sdcard/Android/data/$PKG/files/Download
ALPINE_DIR=$WORKING_DIR/alpine

if [ "$1" = "0" ]; then
    if [ ! -f "$WORKING_DIR/libtalloc.so.2" ]; then
        sh "$PREFIX/local/bin/rish" -c "mv \"$DOWNLOAD_DIR/libtalloc.so.2\" \"$WORKING_DIR\" && chmod +x \"$WORKING_DIR/libtalloc.so.2\""
    fi

    if [ ! -f "$WORKING_DIR/proot" ]; then
        sh "$PREFIX/local/bin/rish" -c "mv \"$DOWNLOAD_DIR/proot\" \"$WORKING_DIR\" && chmod +x \"$WORKING_DIR/proot\""
    fi

    if [ ! -d "$ALPINE_DIR" ]; then
        sh "$PREFIX/local/bin/rish" -c "tar -xvf \"$DOWNLOAD_DIR/alpine.tar.gz\" -C \"$WORKING_DIR\" && chmod +x \"$WORKING_DIR\""
    fi

    # Prepare arguments for proot
    ARGS="--kill-on-exit"
    ARGS="$ARGS -w $PWD"

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

    ARGS="$ARGS -b /sys"

    if [ ! -d "$ALPINE_DIR" ]; then
        mkdir -p "$ALPINE_DIR/tmp"
        chmod 1777 "$ALPINE_DIR/tmp"
    fi
    sh "$PREFIX/local/bin/rish" -c "mkdir -p $ALPINE_DIR/tmp"
    ARGS="$ARGS -b $ALPINE_DIR/tmp:/dev/shm"

    ARGS="$ARGS -r $ALPINE_DIR"
    ARGS="$ARGS -0"
    ARGS="$ARGS --link2symlink"
    ARGS="$ARGS --sysvipc"
    ARGS="$ARGS -L"

    sh "$PREFIX/local/bin/rish" -c "LD_LIBRARY_PATH=$WORKING_DIR PROOT_TMP_DIR=$WORKING_DIR $WORKING_DIR/proot $(echo $ARGS)"

elif [ "$1" = "1" ]; then
    sh "$PREFIX/local/bin/rish"
elif [ "$1" = "2" ]; then
    sh
else
    echo "Unknown working mode"
fi