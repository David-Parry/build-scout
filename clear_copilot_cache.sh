#!/bin/bash

# Clear VS Code Copilot and MCP Server Tools Cache on macOS
# Run with: bash clear_copilot_cache.sh

echo "🧹 VS Code Copilot Cache Cleaner for macOS"
echo "=========================================="
echo ""

# Function to check if VS Code is running
check_vscode_running() {
    # Check for various VS Code process names
    if pgrep -x "Code" > /dev/null || \
       pgrep -x "Visual Studio Code" > /dev/null || \
       pgrep -x "Code - OSS" > /dev/null || \
       ps aux | grep -v grep | grep -q "/Applications/Visual Studio Code.app" || \
       ps aux | grep -v grep | grep -q "[V]isual Studio Code"; then
        return 0
    else
        # Additional check for open VS Code application
        if osascript -e 'tell application "System Events" to count processes whose name contains "Code"' > /dev/null 2>&1; then
            if [ "$(osascript -e 'tell application "System Events" to count processes whose name contains "Code"')" -gt "0" ]; then
                return 0
            fi
        fi
        return 1
    fi
}

# Check if VS Code is running
if check_vscode_running; then
    echo "⚠️  VS Code is currently running!"
    echo "Please close VS Code before running this script."
    read -p "Press Enter after closing VS Code to continue, or Ctrl+C to cancel..."

    # Check again
    if check_vscode_running; then
        echo "❌ VS Code is still running. Exiting..."
        exit 1
    fi
fi

echo "✅ VS Code is not running. Proceeding with cleanup..."
echo ""

# Define paths
VSCODE_PATH="$HOME/Library/Application Support/Code"
CACHE_PATH="$VSCODE_PATH/Cache"
CACHED_DATA_PATH="$VSCODE_PATH/CachedData"
USER_PATH="$VSCODE_PATH/User"
GLOBAL_STORAGE_PATH="$USER_PATH/globalStorage"
WORKSPACE_STORAGE_PATH="$USER_PATH/workspaceStorage"

# Counter for deleted items
deleted_count=0

# Function to safely delete directory contents
safe_delete() {
    local path="$1"
    local description="$2"

    if [ -d "$path" ]; then
        echo "🗑️  Cleaning $description..."
        echo "   Path: $path"

        # Count items before deletion
        local item_count=$(find "$path" -type f 2>/dev/null | wc -l | tr -d ' ')

        # Delete contents but keep the directory
        find "$path" -mindepth 1 -delete 2>/dev/null

        if [ $? -eq 0 ]; then
            echo "   ✓ Deleted $item_count items"
            ((deleted_count += item_count))
        else
            echo "   ⚠️  Some items could not be deleted"
        fi
    else
        echo "📁 $description not found (skipping)"
        echo "   Path: $path"
    fi
    echo ""
}

# Function to delete specific directories
delete_directory() {
    local path="$1"
    local description="$2"

    if [ -d "$path" ]; then
        echo "🗑️  Removing $description..."
        echo "   Path: $path"
        rm -rf "$path"
        if [ $? -eq 0 ]; then
            echo "   ✓ Removed successfully"
            ((deleted_count++))
        else
            echo "   ⚠️  Could not remove directory"
        fi
    else
        echo "📁 $description not found (skipping)"
    fi
    echo ""
}

# Start cleanup
echo "Starting cleanup process..."
echo "=========================="
echo ""

# 1. Clear VS Code Cache
safe_delete "$CACHE_PATH" "VS Code Cache"
safe_delete "$CACHED_DATA_PATH" "VS Code Cached Data"

# 2. Clear Copilot-specific storage
delete_directory "$GLOBAL_STORAGE_PATH/github.copilot" "GitHub Copilot storage"
delete_directory "$GLOBAL_STORAGE_PATH/github.copilot-chat" "GitHub Copilot Chat storage"
delete_directory "$GLOBAL_STORAGE_PATH/github.copilot-nightly" "GitHub Copilot Nightly storage"

# 3. Clear any MCP-related folders in globalStorage
echo "🔍 Searching for MCP-related folders..."
mcp_folders=$(find "$GLOBAL_STORAGE_PATH" -type d -name "*mcp*" -o -name "*MCP*" 2>/dev/null)
if [ -n "$mcp_folders" ]; then
    echo "$mcp_folders" | while read -r folder; do
        delete_directory "$folder" "MCP-related folder: $(basename "$folder")"
    done
else
    echo "   No MCP-related folders found"
    echo ""
fi

# 4. Optional: Clear workspace storage (commented out by default)
echo "💡 Workspace storage cleanup (optional)"
echo "   Path: $WORKSPACE_STORAGE_PATH"
read -p "   Do you want to clear workspace storage? This may affect project-specific settings (y/N): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    safe_delete "$WORKSPACE_STORAGE_PATH" "Workspace Storage"
else
    echo "   Skipped workspace storage cleanup"
    echo ""
fi

# 5. Check for Copilot settings in settings.json
SETTINGS_FILE="$USER_PATH/settings.json"
if [ -f "$SETTINGS_FILE" ]; then
    echo "🔍 Checking settings.json for Copilot/MCP entries..."
    if grep -q -E "(copilot|mcp|MCP)" "$SETTINGS_FILE"; then
        echo "   ⚠️  Found Copilot/MCP related settings in settings.json"
        echo "   You may want to manually review: $SETTINGS_FILE"
    else
        echo "   ✓ No Copilot/MCP settings found"
    fi
else
    echo "📄 No settings.json file found"
fi
echo ""

# Summary
echo "🎉 Cleanup Complete!"
echo "==================="
echo "Total items cleaned: $deleted_count"
echo ""
echo "Next steps:"
echo "1. Open VS Code"
echo "2. Go to Extensions and re-enable GitHub Copilot if needed"
echo "3. Sign in to GitHub Copilot again"
echo ""
echo "✨ Your VS Code Copilot cache has been cleared!"

