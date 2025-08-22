#!/bin/bash

# Graphics acceleration setup script for ReTerminal
# Detects available graphics capabilities and installs appropriate packages

# Check for graphics acceleration setting
GRAPHICS_ENABLED_FILE="/.reterminal_graphics_enabled"

if [ ! -f "$GRAPHICS_ENABLED_FILE" ]; then
    echo "Graphics acceleration is disabled. Enable it in ReTerminal settings."
    exit 0
fi

echo "Setting up graphics acceleration..."

# Detect GPU vendor from Android system
GPU_VENDOR="unknown"
if [ -r /proc/cpuinfo ]; then
    if grep -qi "adreno" /proc/cpuinfo 2>/dev/null || [ -d "/vendor/lib/egl" ]; then
        GPU_VENDOR="qualcomm"
    elif grep -qi "mali" /proc/cpuinfo 2>/dev/null; then
        GPU_VENDOR="arm"
    elif grep -qi "powervr" /proc/cpuinfo 2>/dev/null; then
        GPU_VENDOR="powervr"
    fi
fi

# Alternative detection method using Android graphics libraries
if [ "$GPU_VENDOR" = "unknown" ] && [ -d "/vendor/lib" ]; then
    if ls /vendor/lib/*adreno* >/dev/null 2>&1 || ls /vendor/lib/egl/*adreno* >/dev/null 2>&1; then
        GPU_VENDOR="qualcomm"
    elif ls /vendor/lib/*mali* >/dev/null 2>&1 || ls /vendor/lib/egl/*mali* >/dev/null 2>&1; then
        GPU_VENDOR="arm"
    elif ls /vendor/lib/*powervr* >/dev/null 2>&1 || ls /vendor/lib/egl/*powervr* >/dev/null 2>&1; then
        GPU_VENDOR="powervr"
    fi
fi

echo "Detected GPU vendor: $GPU_VENDOR"

# Function to install packages based on distribution
install_graphics_packages() {
    local distro="$1"
    
    case "$distro" in
        "ubuntu"|"debian")
            APT_CMD=""
            if command -v apt-get >/dev/null 2>&1; then
                APT_CMD="apt-get"
            elif command -v apt >/dev/null 2>&1; then
                APT_CMD="apt"
            else
                echo "Error: Neither apt-get nor apt found"
                return 1
            fi
            
            echo "Installing graphics packages for $distro..."
            
            # Basic OpenGL packages
            $APT_CMD update >/dev/null 2>&1
            $APT_CMD install -y mesa-utils libgl1-mesa-dev libglu1-mesa-dev libglew-dev
            
            # Additional packages based on GPU vendor
            case "$GPU_VENDOR" in
                "qualcomm")
                    echo "Installing Adreno/Qualcomm graphics support..."
                    $APT_CMD install -y libegl1-mesa-dev libgles2-mesa-dev
                    ;;
                "arm")
                    echo "Installing Mali/ARM graphics support..."
                    $APT_CMD install -y libegl1-mesa-dev libgles2-mesa-dev
                    ;;
                "powervr")
                    echo "Installing PowerVR graphics support..."
                    $APT_CMD install -y libegl1-mesa-dev libgles2-mesa-dev
                    ;;
                *)
                    echo "Installing generic graphics support..."
                    $APT_CMD install -y libegl1-mesa-dev libgles2-mesa-dev
                    ;;
            esac
            
            # Vulkan support (available in newer versions)
            if $APT_CMD list vulkan-tools 2>/dev/null | grep -q vulkan-tools; then
                echo "Installing Vulkan support..."
                $APT_CMD install -y vulkan-tools libvulkan-dev
            fi
            ;;
            
        "alpine")
            echo "Installing graphics packages for Alpine Linux..."
            apk update >/dev/null 2>&1
            apk add mesa-dev mesa-gl mesa-gles mesa-utils
            
            # Vulkan support if available
            if apk search vulkan-tools 2>/dev/null | grep -q vulkan-tools; then
                echo "Installing Vulkan support..."
                apk add vulkan-tools vulkan-loader-dev
            fi
            ;;
            
        "arch")
            echo "Installing graphics packages for Arch Linux..."
            pacman -Sy >/dev/null 2>&1
            pacman -S --noconfirm mesa mesa-utils
            
            # Vulkan support
            if pacman -Ss vulkan-tools 2>/dev/null | grep -q vulkan-tools; then
                echo "Installing Vulkan support..."
                pacman -S --noconfirm vulkan-tools vulkan-headers
            fi
            ;;
            
        "kali")
            echo "Installing graphics packages for Kali Linux..."
            apt-get update >/dev/null 2>&1
            apt-get install -y mesa-utils libgl1-mesa-dev libglu1-mesa-dev libglew-dev
            apt-get install -y libegl1-mesa-dev libgles2-mesa-dev
            
            # Vulkan support
            if apt-get list vulkan-tools 2>/dev/null | grep -q vulkan-tools; then
                echo "Installing Vulkan support..."
                apt-get install -y vulkan-tools libvulkan-dev
            fi
            ;;
            
        *)
            echo "Unknown distribution: $distro"
            return 1
            ;;
    esac
    
    echo "Graphics acceleration setup completed for $distro"
    return 0
}

# Detect current distribution
CURRENT_DISTRO="unknown"
if [ -f /etc/os-release ]; then
    . /etc/os-release
    case "$ID" in
        "ubuntu") CURRENT_DISTRO="ubuntu" ;;
        "debian") CURRENT_DISTRO="debian" ;;
        "alpine") CURRENT_DISTRO="alpine" ;;
        "arch") CURRENT_DISTRO="arch" ;;
        "kali") CURRENT_DISTRO="kali" ;;
        *) CURRENT_DISTRO="$ID" ;;
    esac
fi

if [ "$CURRENT_DISTRO" = "unknown" ]; then
    echo "Could not detect Linux distribution"
    exit 1
fi

echo "Detected distribution: $CURRENT_DISTRO"

# Install graphics packages
if install_graphics_packages "$CURRENT_DISTRO"; then
    echo "Graphics acceleration setup successful!"
    
    # Create a completion marker
    touch /.reterminal_graphics_setup_complete
    
    # Display helpful information
    echo ""
    echo "Graphics acceleration has been set up. You can now:"
    echo "- Test OpenGL: glxinfo (if available)"
    echo "- Test OpenGL ES: es2_info (if available)" 
    echo "- Test Vulkan: vulkaninfo (if available)"
    echo ""
    echo "Note: Actual hardware acceleration depends on your device's"
    echo "Android graphics drivers and may still use software rendering."
else
    echo "Graphics acceleration setup failed for $CURRENT_DISTRO"
    exit 1
fi