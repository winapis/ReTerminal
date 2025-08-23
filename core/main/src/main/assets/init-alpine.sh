#!/bin/bash

# Alpine Distribution Configuration Script
# This script handles Alpine-specific configuration after installation is complete  
# Installation and extraction is handled by the unified init script

set -e  # Exit immediately on failure

export PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/share/bin:/usr/share/sbin:/usr/local/bin:/usr/local/sbin:/system/bin:/system/xbin
export HOME=/root

# Check for silent mode flag
SILENT_MODE_FILE="/.reterminal_installed"

# Helper function for conditional output
log_message() {
    if [ ! -f "$SILENT_MODE_FILE" ]; then
        echo "$1"
    fi
}

# Configure basic network settings if not already set
if [ ! -s /etc/resolv.conf ]; then
    echo "nameserver 8.8.8.8" > /etc/resolv.conf
fi

# Set Alpine-specific environment
export PS1="\[\e[38;5;46m\]\u\[\033[39m\]@reterm \[\033[39m\]\w \[\033[0m\]\\$ "
export PIP_BREAK_SYSTEM_PACKAGES=1

# Install essential packages if not present
required_packages="bash gcompat glib nano"
missing_packages=""

for pkg in $required_packages; do
    if ! apk info -e $pkg >/dev/null 2>&1; then
        missing_packages="$missing_packages $pkg"
    fi
done

if [ -n "$missing_packages" ]; then
    log_message "Installing important packages: $missing_packages"
    if [ ! -f "$SILENT_MODE_FILE" ]; then
        apk update && apk upgrade
    else
        apk update >/dev/null 2>&1 && apk upgrade >/dev/null 2>&1
    fi
    
    if apk add $missing_packages; then
        log_message "Successfully installed packages"
    else
        log_message "Warning: Some packages failed to install"
    fi
    
    log_message "Use 'apk' to install new packages"
fi

# Fix linker warning
if [[ ! -f /linkerconfig/ld.config.txt ]]; then
    mkdir -p /linkerconfig
    touch /linkerconfig/ld.config.txt
fi

# Create convenient symlinks
if [ -d "/sdcard" ] && [ ! -e "$HOME/sdcard" ]; then
    ln -sf /sdcard "$HOME/sdcard" 2>/dev/null || true
fi

if [ -d "/storage" ] && [ ! -e "$HOME/storage" ]; then
    ln -sf /storage "$HOME/storage" 2>/dev/null || true
fi

# Check and setup graphics acceleration if enabled
GRAPHICS_ENABLED_FILE="/.reterminal_graphics_enabled"
GRAPHICS_SETUP_COMPLETE="/.reterminal_graphics_setup_complete"

if [ -f "$GRAPHICS_ENABLED_FILE" ] && [ ! -f "$GRAPHICS_SETUP_COMPLETE" ]; then
    log_message "Setting up graphics acceleration..."
    
    # Run graphics setup script if it exists
    GRAPHICS_SCRIPT_PATH="$PREFIX/local/bin/setup-graphics.sh"
    if [ -f "$GRAPHICS_SCRIPT_PATH" ]; then
        chmod +x "$GRAPHICS_SCRIPT_PATH"
        "$GRAPHICS_SCRIPT_PATH"
    fi
fi

# Execute user command or start shell
if [ "$#" -eq 0 ]; then
    source /etc/profile
    export PS1="\[\e[38;5;46m\]\u\[\033[39m\]@reterm \[\033[39m\]\w \[\033[0m\]\\$ "
    cd $HOME
    /bin/ash
else
    exec "$@"
fi