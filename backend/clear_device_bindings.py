#!/usr/bin/env python3
"""
Script to clear device bindings
Run this to reset all device bindings and start fresh
"""
import json
import os

PERMITTED_DEVICES_PATH = 'permitted_devices.json'

def clear_device_bindings():
    """Clear all device bindings"""
    if os.path.exists(PERMITTED_DEVICES_PATH):
        print(f"Found {PERMITTED_DEVICES_PATH}")

        # Backup old file
        backup_path = f"{PERMITTED_DEVICES_PATH}.backup"
        with open(PERMITTED_DEVICES_PATH, 'r') as f:
            old_data = json.load(f)

        with open(backup_path, 'w') as f:
            json.dump(old_data, f, indent=2)

        print(f"✅ Backed up to {backup_path}")
        print(f"   Old bindings: {old_data}")

        # Clear the file
        with open(PERMITTED_DEVICES_PATH, 'w') as f:
            json.dump({}, f, indent=2)

        print(f"✅ Cleared {PERMITTED_DEVICES_PATH}")
        print("   All device bindings have been removed")
    else:
        print(f"File {PERMITTED_DEVICES_PATH} not found")
        print("Creating empty file...")
        with open(PERMITTED_DEVICES_PATH, 'w') as f:
            json.dump({}, f, indent=2)
        print(f"✅ Created empty {PERMITTED_DEVICES_PATH}")

if __name__ == '__main__':
    print("="*50)
    print("🗑️  Clear Device Bindings")
    print("="*50)
    clear_device_bindings()
    print("\n✅ Done! You can now scan and bind devices again.")
    print("="*50)

