"""
Test script để kiểm tra logic device binding độc lập giữa các user
Mỗi user có thể bind/unbind device của riêng mình mà không ảnh hưởng tới user khác
"""

import json
import os

# Simulate the new format
def test_independent_devices():
    print("="*50)
    print("Testing Independent Device Bindings")
    print("="*50)

    # Simulate device bindings
    devices = {}

    # Admin binds device A
    admin_email = "hodung15032003@gmail.com"
    devices[admin_email] = {
        'device_id': 'raspberrypi-001',
        'ip': '192.168.1.100',
        'last_updated': 1234567890
    }
    print(f"\n✅ Admin ({admin_email}) bound device: raspberrypi-001")

    # User 1 binds the SAME device A (their own binding)
    user1_email = "hongocdung15032003@gmail.com"
    devices[user1_email] = {
        'device_id': 'raspberrypi-001',
        'ip': '192.168.1.100',
        'last_updated': 1234567891
    }
    print(f"✅ User1 ({user1_email}) bound device: raspberrypi-001")

    # User 2 binds a different device B
    user2_email = "user2@gmail.com"
    devices[user2_email] = {
        'device_id': 'raspberrypi-002',
        'ip': '192.168.1.101',
        'last_updated': 1234567892
    }
    print(f"✅ User2 ({user2_email}) bound device: raspberrypi-002")

    print("\n" + "="*50)
    print("Current Device Bindings:")
    print("="*50)
    for email, device_info in devices.items():
        print(f"  {email} -> {device_info['device_id']} ({device_info['ip']})")

    # Test: User1 unbinds their device
    print("\n" + "="*50)
    print(f"User1 ({user1_email}) unbinds their device...")
    print("="*50)
    del devices[user1_email]

    print("\n✅ After User1 unbind:")
    for email, device_info in devices.items():
        print(f"  {email} -> {device_info['device_id']} ({device_info['ip']})")

    # Verify admin's device is still there
    if admin_email in devices:
        print(f"\n✅ SUCCESS: Admin's device still connected!")
        print(f"   Admin device: {devices[admin_email]['device_id']}")
    else:
        print(f"\n❌ FAILED: Admin's device was removed!")

    # Verify user2's device is still there
    if user2_email in devices:
        print(f"✅ SUCCESS: User2's device still connected!")
        print(f"   User2 device: {devices[user2_email]['device_id']}")
    else:
        print(f"❌ FAILED: User2's device was removed!")

    print("\n" + "="*50)
    print("Test Migration from Old Format")
    print("="*50)

    # Test migration from old format
    old_format = {
        'raspberrypi-001': 'hodung15032003@gmail.com',  # Old format: device_id -> email
        'raspberrypi-002': {  # Old owner format: device_id -> {email, ip}
            'email': 'hongocdung15032003@gmail.com',
            'ip': '192.168.1.100'
        }
    }

    print("\nOld format:")
    print(json.dumps(old_format, indent=2))

    # Migrate
    new_format = {}
    for key, value in old_format.items():
        if isinstance(value, str):
            email = value
            device_id = key
            new_format[email] = {'device_id': device_id, 'ip': None, 'last_updated': 0}
        elif isinstance(value, dict) and 'email' in value:
            email = value['email']
            device_id = key
            ip = value.get('ip')
            new_format[email] = {'device_id': device_id, 'ip': ip, 'last_updated': 0}

    print("\n✅ Migrated to new format:")
    print(json.dumps(new_format, indent=2))

    print("\n" + "="*50)
    print("All tests completed!")
    print("="*50)

if __name__ == '__main__':
    test_independent_devices()

