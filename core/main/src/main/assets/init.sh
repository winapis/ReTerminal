if [ ! -f "$PREFIX/local/.busybox_installed" ]; then
    $EXEC busybox --install -s $PREFIX/local/bin && touch "$PREFIX/local/.busybox_installed"
fi

#Start bash
$LINKER $PREFIX/local/bin/bash --login
