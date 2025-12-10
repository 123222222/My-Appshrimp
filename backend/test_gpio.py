#!/usr/bin/env python3
"""
Test script for GPIO motor control
Run this to test GPIO functionality without the full Flask app
"""

import sys
import time

try:
    import RPi.GPIO as GPIO
    GPIO_AVAILABLE = True
except ImportError:
    print("⚠️  RPi.GPIO not available. Install with: pip install RPi.GPIO")
    GPIO_AVAILABLE = False
    sys.exit(1)

# GPIO Pin Configuration
GPIO_PINS = {
    'motor1': 17,
    'motor2': 27,
    'motor3': 22
}

def setup_gpio():
    """Initialize GPIO pins"""
    print("Setting up GPIO...")
    GPIO.setmode(GPIO.BCM)
    GPIO.setwarnings(False)

    for name, pin in GPIO_PINS.items():
        GPIO.setup(pin, GPIO.OUT)
        GPIO.output(pin, GPIO.LOW)
        print(f"✓ {name} (GPIO {pin}) initialized")

def cleanup_gpio():
    """Clean up GPIO pins"""
    print("\nCleaning up GPIO...")
    for pin in GPIO_PINS.values():
        GPIO.output(pin, GPIO.LOW)
    GPIO.cleanup()
    print("✓ GPIO cleaned up")

def test_motor(motor_name, duration=2):
    """Test a single motor"""
    pin = GPIO_PINS[motor_name]
    print(f"\n🔧 Testing {motor_name} (GPIO {pin})...")

    # Turn ON
    GPIO.output(pin, GPIO.HIGH)
    print(f"   ✓ {motor_name} ON")
    time.sleep(duration)

    # Turn OFF
    GPIO.output(pin, GPIO.LOW)
    print(f"   ✓ {motor_name} OFF")
    time.sleep(0.5)

def test_all_motors():
    """Test all motors sequentially"""
    print("\n" + "="*50)
    print("Testing all motors sequentially...")
    print("="*50)

    for motor_name in GPIO_PINS.keys():
        test_motor(motor_name, duration=2)

def test_simultaneous():
    """Test all motors simultaneously"""
    print("\n" + "="*50)
    print("Testing all motors simultaneously...")
    print("="*50)

    # Turn all ON
    print("\n🔧 Turning all motors ON...")
    for name, pin in GPIO_PINS.items():
        GPIO.output(pin, GPIO.HIGH)
        print(f"   ✓ {name} ON")

    time.sleep(3)

    # Turn all OFF
    print("\n🔧 Turning all motors OFF...")
    for name, pin in GPIO_PINS.items():
        GPIO.output(pin, GPIO.LOW)
        print(f"   ✓ {name} OFF")

    time.sleep(0.5)

def interactive_mode():
    """Interactive control mode"""
    print("\n" + "="*50)
    print("Interactive Control Mode")
    print("="*50)
    print("\nCommands:")
    print("  1, 2, 3 - Toggle motor 1, 2, 3")
    print("  a - Turn all ON")
    print("  s - Turn all OFF")
    print("  q - Quit")
    print()

    motor_states = {name: False for name in GPIO_PINS.keys()}

    while True:
        try:
            cmd = input("Enter command: ").strip().lower()

            if cmd == 'q':
                break
            elif cmd == '1':
                toggle_motor('motor1', motor_states)
            elif cmd == '2':
                toggle_motor('motor2', motor_states)
            elif cmd == '3':
                toggle_motor('motor3', motor_states)
            elif cmd == 'a':
                for name in GPIO_PINS.keys():
                    set_motor(name, True, motor_states)
            elif cmd == 's':
                for name in GPIO_PINS.keys():
                    set_motor(name, False, motor_states)
            else:
                print("Invalid command")

        except KeyboardInterrupt:
            break

def toggle_motor(motor_name, states):
    """Toggle a motor on/off"""
    new_state = not states[motor_name]
    set_motor(motor_name, new_state, states)

def set_motor(motor_name, state, states):
    """Set motor to specific state"""
    pin = GPIO_PINS[motor_name]
    GPIO.output(pin, GPIO.HIGH if state else GPIO.LOW)
    states[motor_name] = state
    status = "ON" if state else "OFF"
    print(f"✓ {motor_name} (GPIO {pin}): {status}")

def main():
    """Main test function"""
    print("="*50)
    print("GPIO Motor Control Test")
    print("="*50)

    if not GPIO_AVAILABLE:
        return

    try:
        setup_gpio()

        # Run tests
        test_all_motors()
        test_simultaneous()

        # Interactive mode
        interactive_mode()

    except KeyboardInterrupt:
        print("\n\n⚠️  Interrupted by user")
    except Exception as e:
        print(f"\n❌ Error: {e}")
    finally:
        cleanup_gpio()
        print("\n✓ Test completed")

if __name__ == "__main__":
    main()

