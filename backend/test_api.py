#!/usr/bin/env python3
"""
Test script for Device Binding APIs
Run this to verify backend is working correctly
"""

import requests
import json
import sys

# Configuration
BASE_URL = "http://localhost:8000"  # Change to your server URL
FAKE_TOKEN = "test_token_for_testing"  # Replace with real Firebase token

def print_result(name, response):
    """Print test result"""
    status = "✅ PASS" if response.status_code in [200, 201] else "❌ FAIL"
    print(f"\n{status} {name}")
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    print("-" * 50)

def test_health():
    """Test health check endpoint"""
    response = requests.get(f"{BASE_URL}/health")
    print_result("Health Check", response)
    return response.status_code == 200

def test_bind_device():
    """Test device binding"""
    data = {
        "device_id": "raspberrypi-001"
    }
    headers = {
        "Authorization": FAKE_TOKEN,
        "Content-Type": "application/json"
    }
    response = requests.post(f"{BASE_URL}/api/devices/bind",
                            json=data,
                            headers=headers)
    print_result("Bind Device", response)
    return response.status_code in [200, 201]

def test_get_my_device():
    """Test get my device endpoint"""
    headers = {
        "Authorization": FAKE_TOKEN
    }
    response = requests.get(f"{BASE_URL}/api/devices/my-device",
                           headers=headers)
    print_result("Get My Device", response)
    return response.status_code == 200

def test_check_permission():
    """Test check device permission"""
    data = {
        "device_id": "raspberrypi-001"
    }
    headers = {
        "Authorization": FAKE_TOKEN,
        "Content-Type": "application/json"
    }
    response = requests.post(f"{BASE_URL}/api/devices/check",
                            json=data,
                            headers=headers)
    print_result("Check Permission", response)
    return response.status_code == 200

def test_unbind_device():
    """Test device unbinding"""
    data = {
        "device_id": "raspberrypi-001"
    }
    headers = {
        "Authorization": FAKE_TOKEN,
        "Content-Type": "application/json"
    }
    response = requests.post(f"{BASE_URL}/api/devices/unbind",
                            json=data,
                            headers=headers)
    print_result("Unbind Device", response)
    return response.status_code == 200

def test_udp_discovery():
    """Test UDP device discovery"""
    import socket

    print("\n🔍 Testing UDP Discovery...")
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        sock.settimeout(3)
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

        message = "DISCOVER_RASP".encode('utf-8')
        sock.sendto(message, ('255.255.255.255', 50000))

        data, addr = sock.recvfrom(1024)
        device_id = data.decode('utf-8')

        print(f"✅ PASS UDP Discovery")
        print(f"Found device: {device_id} at {addr[0]}")
        print("-" * 50)
        sock.close()
        return True
    except socket.timeout:
        print("❌ FAIL UDP Discovery - Timeout (no response)")
        print("-" * 50)
        return False
    except Exception as e:
        print(f"❌ FAIL UDP Discovery - Error: {e}")
        print("-" * 50)
        return False

def main():
    """Run all tests"""
    print("=" * 50)
    print("🧪 Device Binding API Tests")
    print("=" * 50)

    tests = [
        ("Health Check", test_health),
        ("UDP Discovery", test_udp_discovery),
        # Note: Following tests require valid Firebase token
        # Uncomment when you have a real token
        # ("Bind Device", test_bind_device),
        # ("Get My Device", test_get_my_device),
        # ("Check Permission", test_check_permission),
        # ("Unbind Device", test_unbind_device),
    ]

    results = []
    for name, test_func in tests:
        try:
            result = test_func()
            results.append((name, result))
        except Exception as e:
            print(f"❌ {name} - Exception: {e}")
            results.append((name, False))

    # Summary
    print("\n" + "=" * 50)
    print("📊 Test Summary")
    print("=" * 50)
    passed = sum(1 for _, result in results if result)
    total = len(results)

    for name, result in results:
        status = "✅" if result else "❌"
        print(f"{status} {name}")

    print(f"\nPassed: {passed}/{total}")
    print("=" * 50)

    return passed == total

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)

