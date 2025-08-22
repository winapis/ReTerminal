# Ubuntu Environment Initialization Script
# Note: Removed 'set -e' to allow graceful error handling

export PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/share/bin:/usr/share/sbin:/usr/local/bin:/usr/local/sbin:/system/bin:/system/xbin
export HOME=/root

# Debug information
echo "=== Ubuntu Environment Initialization ==="
echo "Current working directory: $(pwd)"
echo "PATH: $PATH"
echo "User: $(whoami 2>/dev/null || echo 'unknown')"
echo "Root filesystem check:"
ls -la / 2>/dev/null | head -5

if [ ! -s /etc/resolv.conf ]; then
    echo "nameserver 8.8.8.8" > /etc/resolv.conf
fi

export PS1="\[\e[38;5;46m\]\u\[\033[39m\]@reterm \[\033[39m\]\w \[\033[0m\]\\$ "
export DEBIAN_FRONTEND=noninteractive

required_packages="bash nano curl"
missing_packages=""

# Check if we're in a proper Ubuntu environment
if [ -f /etc/os-release ]; then
    echo "OS Release information:"
    cat /etc/os-release
else
    echo "Warning: /etc/os-release not found - may not be in proper Ubuntu chroot"
fi

# Check if apt-get or apt is available
APT_CMD=""
if command -v apt-get >/dev/null 2>&1; then
    APT_CMD="apt-get"
elif command -v apt >/dev/null 2>&1; then
    APT_CMD="apt"
else
    echo "Error: Neither apt-get nor apt found. Ubuntu environment may not be properly initialized."
    echo "Available package managers:"
    command -v dpkg 2>/dev/null && echo "  - dpkg found"
    command -v snap 2>/dev/null && echo "  - snap found"
    echo "PATH: $PATH"
    echo "Current directory: $(pwd)"
    echo "Root filesystem contents:"
    ls -la / 2>/dev/null | head -10
    echo "Available binaries in /usr/bin:"
    ls -la /usr/bin/ 2>/dev/null | grep -E "(apt|dpkg)" || echo "  - No apt/dpkg binaries found"
    echo "Checking if this looks like Android filesystem:"
    if [ -d "/apex" ] || [ -d "/system/bin" ]; then
        echo "  - WARNING: Detected Android filesystem structure. Ubuntu chroot may have failed."
        echo "  - This suggests proot is not working correctly or Ubuntu rootfs was not extracted properly."
    fi
    exit 1
fi

# Update package lists
echo "Using $APT_CMD for package management"
if ! $APT_CMD update 2>/dev/null; then
    echo "Warning: Package list update failed. Continuing without update..."
    echo "This may be due to network issues or repository problems."
fi

for pkg in $required_packages; do
    if ! dpkg -l | grep -q "^ii  $pkg "; then
        missing_packages="$missing_packages $pkg"
    fi
done

if [ -n "$missing_packages" ]; then
    echo -e "\e[34;1m[*] \e[0mInstalling Important packages\e[0m"
    if $APT_CMD install -y $missing_packages; then
        echo -e "\e[32;1m[+] \e[0mSuccessfully Installed\e[0m"
    else
        echo -e "\e[33;1m[!] \e[0mSome packages failed to install. Continuing anyway...\e[0m"
        echo "You may need to install packages manually later."
    fi
    echo -e "\e[34m[*] \e[0mUse \e[32m${APT_CMD}\e[0m to install new packages\e[0m"
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