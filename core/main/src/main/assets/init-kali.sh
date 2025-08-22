set -e  # Exit immediately on Failure

export PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/share/bin:/usr/share/sbin:/usr/local/bin:/usr/local/sbin:/system/bin:/system/xbin
export HOME=/root

if [ ! -s /etc/resolv.conf ]; then
    echo "nameserver 8.8.8.8" > /etc/resolv.conf
fi

# Check for silent mode flag
SILENT_MODE_FILE="/.reterminal_installed"

export PS1="\[\e[38;5;46m\]\u\[\033[39m\]@reterm \[\033[39m\]\w \[\033[0m\]\\$ "
export DEBIAN_FRONTEND=noninteractive

required_packages="bash nano curl"
missing_packages=""

# Update package lists
if [ ! -f "$SILENT_MODE_FILE" ]; then
    apt-get update
else
    apt-get update >/dev/null 2>&1
fi

for pkg in $required_packages; do
    if ! dpkg -l | grep -q "^ii  $pkg "; then
        missing_packages="$missing_packages $pkg"
    fi
done

if [ -n "$missing_packages" ]; then
    if [ ! -f "$SILENT_MODE_FILE" ]; then
        echo -e "\e[34;1m[*] \e[0mInstalling Important packages\e[0m"
    fi
    apt-get install -y $missing_packages
    if [ $? -eq 0 ]; then
        if [ ! -f "$SILENT_MODE_FILE" ]; then
            echo -e "\e[32;1m[+] \e[0mSuccessfully Installed\e[0m"
        fi
    fi
    if [ ! -f "$SILENT_MODE_FILE" ]; then
        echo -e "\e[34m[*] \e[0mUse \e[32mapt\e[0m to install new packages\e[0m"
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