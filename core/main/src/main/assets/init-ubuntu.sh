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

required_packages="bash nano"
optional_packages="curl"
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

# Try to install optional packages but don't fail if they can't be installed
optional_missing=""
for pkg in $optional_packages; do
    if ! dpkg -l | grep -q "^ii  $pkg "; then
        optional_missing="$optional_missing $pkg"
    fi
done

if [ -n "$missing_packages" ]; then
    echo -e "\e[34;1m[*] \e[0mInstalling required packages: $missing_packages\e[0m"
    if $APT_CMD install -y $missing_packages; then
        echo -e "\e[32;1m[+] \e[0mSuccessfully installed required packages\e[0m"
    else
        echo -e "\e[33;1m[!] \e[0mSome required packages failed to install. Continuing anyway...\e[0m"
        echo "You may need to install packages manually later."
    fi
fi

if [ -n "$optional_missing" ]; then
    echo -e "\e[34;1m[*] \e[0mTrying to install optional packages: $optional_missing\e[0m"
    if $APT_CMD install -y $optional_missing 2>/dev/null; then
        echo -e "\e[32;1m[+] \e[0mSuccessfully installed optional packages\e[0m"
    else
        echo -e "\e[33;1m[!] \e[0mOptional packages not available or failed to install. Skipping...\e[0m"
    fi
fi

if [ -n "$missing_packages" ] || [ -n "$optional_missing" ]; then
    echo -e "\e[34m[*] \e[0mUse \e[32m${APT_CMD}\e[0m to install new packages\e[0m"
fi

# Fix linker warning
if [[ ! -f /linkerconfig/ld.config.txt ]]; then
    mkdir -p /linkerconfig
    touch /linkerconfig/ld.config.txt
fi

# Suppress group ID warnings by redirecting groups command errors
# These warnings are expected in Android environment and can be safely ignored
export GROUPS_SUPPRESS_WARNINGS=1 2>/dev/null || true

# Create symlink to Android sdcard for easy access
if [ -d "/sdcard" ] && [ ! -e "$HOME/sdcard" ]; then
    ln -sf /sdcard "$HOME/sdcard" 2>/dev/null || true
    echo "Created symlink: $HOME/sdcard -> /sdcard"
fi

if [ "$#" -eq 0 ]; then
    # Suppress group warnings that occur during profile loading
    # Redirect stderr to null for common commands that trigger group warnings
    alias groups='groups 2>/dev/null' 2>/dev/null || true
    source /etc/profile 2>/dev/null || true
    export PS1="\[\e[38;5;46m\]\u\[\033[39m\]@reterm \[\033[39m\]\w \[\033[0m\]\\$ "
    cd $HOME
    /bin/bash
else
    exec "$@"
fi