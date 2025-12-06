#!/bin/bash
# Post-installation script for Java WebView App

set -e

echo "Running post-installation script for Java WebView App..."

# Update desktop database
if command -v update-desktop-database >/dev/null 2>&1; then
    echo "Updating desktop database..."
    update-desktop-database
fi

# Update icon cache
if command -v gtk-update-icon-cache >/dev/null 2>&1; then
    echo "Updating icon cache..."
    gtk-update-icon-cache -f -t /usr/share/icons/hicolor >/dev/null 2>&1 || true
fi

echo "Post-installation completed successfully."