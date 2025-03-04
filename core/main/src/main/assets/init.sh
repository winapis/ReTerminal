if [ "$1" = "0" ]; then
    sh "$PREFIX/local/bin/rish" -c "/system/bin/app_process -Djava.class.path=\"$PKG_PATH\" /system/bin com.rk.shell.Installer" "$2"
elif [ "$1" = "1" ]; then
    sh "$PREFIX/local/bin/rish"
elif [ "$1" = "2" ]; then
    sh
else
    echo "Unknown working mode $1"
fi


