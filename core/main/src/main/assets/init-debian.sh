# Debian Environment Initialization Script  
# Note: Removed 'set -e' to allow graceful error handling

export PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/share/bin:/usr/share/sbin:/usr/local/bin:/usr/local/sbin:/system/bin:/system/xbin
export HOME=/root

# Check for silent mode flag
SILENT_MODE_FILE="/.reterminal_installed"

# Only show verbose messages if not in silent mode
if [ ! -f "$SILENT_MODE_FILE" ]; then
    # Debug information
    echo "=== Debian Environment Initialization ==="
    echo "Current working directory: $(pwd)"
    echo "PATH: $PATH"
    echo "User: $(whoami 2>/dev/null || echo 'unknown')"
    echo "Root filesystem check:"
    ls -la / 2>/dev/null | head -5
fi

if [ ! -s /etc/resolv.conf ]; then
    echo "nameserver 8.8.8.8" > /etc/resolv.conf
fi

export PS1="\[\e[38;5;46m\]\u\[\033[39m\]@reterm \[\033[39m\]\w \[\033[0m\]\\$ "
export DEBIAN_FRONTEND=noninteractive

required_packages="bash nano"
optional_packages="curl"
missing_packages=""

# Check if we're in a proper Debian environment
if [ ! -f "$SILENT_MODE_FILE" ]; then
    if [ -f /etc/os-release ]; then
        echo "OS Release information:"
        cat /etc/os-release
    else
        echo "Warning: /etc/os-release not found - may not be in proper Debian chroot"
    fi
fi

# Check if apt-get or apt is available
APT_CMD=""
if command -v apt-get >/dev/null 2>&1; then
    APT_CMD="apt-get"
elif command -v apt >/dev/null 2>&1; then
    APT_CMD="apt"
else
    if [ ! -f "$SILENT_MODE_FILE" ]; then
        echo "Error: Neither apt-get nor apt found. Debian environment may not be properly initialized."
        echo "Available package managers:"
        command -v dpkg 2>/dev/null && echo "  - dpkg found"
        echo "PATH: $PATH"
        echo "Current directory: $(pwd)"
        echo "Root filesystem contents:"
        ls -la / 2>/dev/null | head -10
        echo "Available binaries in /usr/bin:"
        ls -la /usr/bin/ 2>/dev/null | grep -E "(apt|dpkg)" || echo "  - No apt/dpkg binaries found"
        echo "Checking if this looks like Android filesystem:"
        if [ -d "/apex" ] || [ -d "/system/bin" ]; then
            echo "  - WARNING: Detected Android filesystem structure. Debian chroot may have failed."
            echo "  - This suggests proot is not working correctly or Debian rootfs was not extracted properly."
        fi
    fi
    exit 1
fi

# Update package lists
if [ ! -f "$SILENT_MODE_FILE" ]; then
    echo "Using $APT_CMD for package management"
fi
if ! $APT_CMD update 2>/dev/null; then
    if [ ! -f "$SILENT_MODE_FILE" ]; then
        echo "Warning: Package list update failed. Continuing without update..."
        echo "This may be due to network issues or repository problems."
    fi
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
    if [ ! -f "$SILENT_MODE_FILE" ]; then
        echo -e "\e[34;1m[*] \e[0mInstalling required packages: $missing_packages\e[0m"
    fi
    if $APT_CMD install -y $missing_packages; then
        if [ ! -f "$SILENT_MODE_FILE" ]; then
            echo -e "\e[32;1m[+] \e[0mSuccessfully installed required packages\e[0m"
        fi
    else
        if [ ! -f "$SILENT_MODE_FILE" ]; then
            echo -e "\e[33;1m[!] \e[0mSome required packages failed to install. Continuing anyway...\e[0m"
            echo "You may need to install packages manually later."
        fi
    fi
fi

if [ -n "$optional_missing" ]; then
    if [ ! -f "$SILENT_MODE_FILE" ]; then
        echo -e "\e[34;1m[*] \e[0mTrying to install optional packages: $optional_missing\e[0m"
    fi
    if $APT_CMD install -y $optional_missing 2>/dev/null; then
        if [ ! -f "$SILENT_MODE_FILE" ]; then
            echo -e "\e[32;1m[+] \e[0mSuccessfully installed optional packages\e[0m"
        fi
    else
        if [ ! -f "$SILENT_MODE_FILE" ]; then
            echo -e "\e[33;1m[!] \e[0mOptional packages not available or failed to install. Skipping...\e[0m"
        fi
    fi
fi

if [ -n "$missing_packages" ] || [ -n "$optional_missing" ]; then
    if [ ! -f "$SILENT_MODE_FILE" ]; then
        echo -e "\e[34m[*] \e[0mUse \e[32m${APT_CMD}\e[0m to install new packages\e[0m"
    fi
fi

# Fix linker warning
if [[ ! -f /linkerconfig/ld.config.txt ]]; then
    mkdir -p /linkerconfig
    touch /linkerconfig/ld.config.txt
fi

# Mark installation as complete for silent mode
if [ ! -f "$SILENT_MODE_FILE" ]; then
    touch "$SILENT_MODE_FILE"
fi

# Check and setup graphics acceleration if enabled
GRAPHICS_ENABLED_FILE="/.reterminal_graphics_enabled"
GRAPHICS_SETUP_COMPLETE="/.reterminal_graphics_setup_complete"

if [ -f "$GRAPHICS_ENABLED_FILE" ] && [ ! -f "$GRAPHICS_SETUP_COMPLETE" ]; then
    if [ ! -f "$SILENT_MODE_FILE" ]; then
        echo "Setting up graphics acceleration..."
    fi
    
    # Run graphics setup script if it exists
    GRAPHICS_SCRIPT_PATH="$PREFIX/local/bin/setup-graphics.sh"
    if [ -f "$GRAPHICS_SCRIPT_PATH" ]; then
        chmod +x "$GRAPHICS_SCRIPT_PATH"
        "$GRAPHICS_SCRIPT_PATH"
    fi
fi

if [ "$#" -eq 0 ]; then
    source /etc/profile 2>/dev/null || true
    export PS1="\[\e[38;5;46m\]\u\[\033[39m\]@reterm \[\033[39m\]\w \[\033[0m\]\\$ "
    cd $HOME
    /bin/bash
else
    exec "$@"
fi