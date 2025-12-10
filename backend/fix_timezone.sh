#!/bin/bash

echo "=================================="
echo "🕐 Fixing Raspberry Pi Timezone"
echo "=================================="

# Check current timezone
echo -e "\n📍 Current timezone:"
timedatectl show -p Timezone --value 2>/dev/null || echo "Unknown"

echo -e "\n⏰ Current time:"
date '+%Y-%m-%d %H:%M:%S %Z'

# Set timezone to Vietnam (Asia/Ho_Chi_Minh - UTC+7)
echo -e "\n🔧 Setting timezone to Asia/Ho_Chi_Minh (UTC+7)..."

if sudo timedatectl set-timezone Asia/Ho_Chi_Minh; then
    echo "✅ Timezone set successfully!"
else
    echo "❌ Failed to set timezone (check sudo permissions)"
    exit 1
fi

# Verify
echo -e "\n✅ New timezone:"
timedatectl show -p Timezone --value

echo -e "\n✅ New time:"
date '+%Y-%m-%d %H:%M:%S %Z'

echo -e "\n=================================="
echo "✅ Timezone updated successfully!"
echo "=================================="
echo -e "\n⚠️  IMPORTANT: Restart your Flask server for changes to take effect!"
echo ""
echo "Recommended: Use ./start_server.sh which auto-sets timezone"
echo "Or restart manually:"
echo "  1. Stop current server (Ctrl+C or kill process)"
echo "  2. Start again: python3 app_complete.py"
echo "  3. Or use systemctl if running as service"


