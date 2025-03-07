set -e

if [ "$1" = "0" ]; then
   result=$(sh "$PREFIX/local/bin/rish" -c "/system/bin/app_process -Djava.class.path=\"$PKG_PATH\" /system/bin com.rk.shell.Installer" "$2")

   sh "$PREFIX/local/bin/rish" -c "
       mkdir -p /data/local/tmp/ReTerminal/$2
       export LD_LIBRARY_PATH=/data/local/tmp/ReTerminal
       export PROOT_TMP_DIR=/data/local/tmp/ReTerminal/$2
       /data/local/tmp/ReTerminal/proot $result
   "

elif [ "$1" = "1" ]; then
    sh "$PREFIX/local/bin/rish"
elif [ "$1" = "2" ]; then
    sh
else
    echo "Unknown working mode $1"
fi


