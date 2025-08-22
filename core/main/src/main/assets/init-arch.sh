set -e  # Exit immediately on Failure

export PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/share/bin:/usr/share/sbin:/usr/local/bin:/usr/local/sbin:/system/bin:/system/xbin
export HOME=/root

if [ ! -s /etc/resolv.conf ]; then
    echo "nameserver 8.8.8.8" > /etc/resolv.conf
fi

export PS1="\[\e[38;5;46m\]\u\[\033[39m\]@reterm \[\033[39m\]\w \[\033[0m\]\\$ "

required_packages="bash nano curl"
missing_packages=""

# Initialize pacman keyring if needed
if [ ! -d /etc/pacman.d/gnupg ]; then
    pacman-key --init
    pacman-key --populate archlinux
fi

# Update package lists
pacman -Sy

for pkg in $required_packages; do
    if ! pacman -Q $pkg >/dev/null 2>&1; then
        missing_packages="$missing_packages $pkg"
    fi
done

if [ -n "$missing_packages" ]; then
    echo -e "\e[34;1m[*] \e[0mInstalling Important packages\e[0m"
    pacman -S --noconfirm $missing_packages
    if [ $? -eq 0 ]; then
        echo -e "\e[32;1m[+] \e[0mSuccessfully Installed\e[0m"
    fi
    echo -e "\e[34m[*] \e[0mUse \e[32mpacman\e[0m to install new packages\e[0m"
fi

# Fix linker warning
if [[ ! -f /linkerconfig/ld.config.txt ]]; then
    mkdir -p /linkerconfig
    touch /linkerconfig/ld.config.txt
fi

if [ "$#" -eq 0 ]; then
    source /etc/profile 2>/dev/null || true
    export PS1="\[\e[38;5;46m\]\u\[\033[39m\]@reterm \[\033[39m\]\w \[\033[0m\]\\$ "
    cd $HOME
    /bin/bash
else
    exec "$@"
fi