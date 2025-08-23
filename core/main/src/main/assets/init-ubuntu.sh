#!/bin/bash

# Ubuntu Distribution Configuration Script  
# This script handles Ubuntu-specific configuration after installation is complete
# Installation and extraction is handled by the unified init script

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
    echo "nameserver 8.8.4.4" >> /etc/resolv.conf
fi

# Set Ubuntu-specific environment
export PS1="\[\e[38;5;46m\]\u\[\033[39m\]@reterm \[\033[39m\]\w \[\033[0m\]\\$ "
export DEBIAN_FRONTEND=noninteractive

# Verify we're in Ubuntu environment
if [ ! -f "$SILENT_MODE_FILE" ] && [ -f /etc/os-release ]; then
    log_message "=== Ubuntu Environment Configuration ==="
    log_message "OS: $(grep PRETTY_NAME /etc/os-release | cut -d'"' -f2)"
fi

# Install essential packages if not present
required_packages="bash nano"
optional_packages="curl wget"
missing_packages=""

# Find available package manager
APT_CMD=""
if command -v apt-get >/dev/null 2>&1; then
    APT_CMD="apt-get"
elif command -v apt >/dev/null 2>&1; then
    APT_CMD="apt"
else
    log_message "Warning: Neither apt-get nor apt found. Package management may be limited."
fi

# Check for missing required packages
if [ -n "$APT_CMD" ]; then
    for pkg in $required_packages; do
        if ! dpkg -l | grep -q "^ii  $pkg "; then
            missing_packages="$missing_packages $pkg"
        fi
    done

    # Install missing packages
    if [ -n "$missing_packages" ]; then
        log_message "Installing required packages: $missing_packages"
        # Update package lists only if we have missing packages
        $APT_CMD update >/dev/null 2>&1 || log_message "Warning: Package list update failed"
        
        if $APT_CMD install -y $missing_packages >/dev/null 2>&1; then
            log_message "Successfully installed required packages"
        else
            log_message "Warning: Some packages failed to install. Continuing anyway..."
        fi
    fi

    # Try to install optional packages
    optional_missing=""
    for pkg in $optional_packages; do
        if ! dpkg -l | grep -q "^ii  $pkg "; then
            optional_missing="$optional_missing $pkg"
        fi
    done

    if [ -n "$optional_missing" ]; then
        log_message "Installing optional packages: $optional_missing"
        if $APT_CMD install -y $optional_missing >/dev/null 2>&1; then
            log_message "Successfully installed optional packages"
        else
            log_message "Optional packages not available or failed to install. Skipping..."
        fi
    fi

    if [ -n "$missing_packages" ] || [ -n "$optional_missing" ]; then
        log_message "Use '$APT_CMD' to install additional packages"
    fi
fi

# Fix common Ubuntu/Android compatibility issues
if [[ ! -f /linkerconfig/ld.config.txt ]]; then
    mkdir -p /linkerconfig
    touch /linkerconfig/ld.config.txt
fi

# Suppress group ID warnings that are expected in Android environment
export GROUPS_SUPPRESS_WARNINGS=1 2>/dev/null || true

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
    # Suppress group warnings during profile loading
    alias groups='groups 2>/dev/null' 2>/dev/null || true
    source /etc/profile 2>/dev/null || true
    export PS1="\[\e[38;5;46m\]\u\[\033[39m\]@reterm \[\033[39m\]\w \[\033[0m\]\\$ "
    cd $HOME
    /bin/bash
else
    exec "$@"
fi