#!/usr/bin/env python3
"""
Test script to debug schedule checking logic
"""

from datetime import datetime

# Sample schedules (copy from your actual state)
auto_schedules = {
    'motor1': {
        'enabled': False,  # Change to True if enabled in your app
        'start_time': '21:35',
        'end_time': '21:40',
        'days': []  # Add days like: ['Monday', 'Tuesday']
    },
    'motor2': {
        'enabled': False,
        'start_time': '08:00',
        'end_time': '18:00',
        'days': []
    },
    'motor3': {
        'enabled': False,
        'start_time': '08:00',
        'end_time': '18:00',
        'days': []
    }
}

def check_schedule(motor_id):
    """Check if motor should be on based on schedule"""
    schedule = auto_schedules[motor_id]

    print(f"\n{'='*60}")
    print(f"Checking schedule for {motor_id}")
    print(f"{'='*60}")

    if not schedule['enabled']:
        print(f"❌ Schedule is DISABLED")
        return False

    print(f"✅ Schedule is ENABLED")

    now = datetime.now()
    current_time = now.strftime('%H:%M')
    current_day = now.strftime('%A')

    print(f"⏰ Current time: {current_time}")
    print(f"📅 Current day: {current_day}")
    print(f"📋 Schedule:")
    print(f"   - Start: {schedule['start_time']}")
    print(f"   - End: {schedule['end_time']}")
    print(f"   - Days: {schedule['days']}")

    # Check if current day is in schedule
    if schedule['days'] and current_day not in schedule['days']:
        print(f"❌ Current day '{current_day}' NOT in schedule days {schedule['days']}")
        return False

    if not schedule['days']:
        print(f"⚠️  No days configured! Schedule will never run!")
        return False

    print(f"✅ Current day '{current_day}' IS in schedule days")

    # Check if current time is in range
    start_time = schedule['start_time']
    end_time = schedule['end_time']

    in_time_range = start_time <= current_time <= end_time

    print(f"\n🕐 Time check:")
    print(f"   {start_time} <= {current_time} <= {end_time}")
    print(f"   Result: {in_time_range}")

    if in_time_range:
        print(f"✅ Motor {motor_id} should be ON")
    else:
        print(f"❌ Motor {motor_id} should be OFF (time not in range)")

    return in_time_range

def main():
    print("\n" + "="*60)
    print("🔍 SCHEDULE CHECK DEBUG TOOL")
    print("="*60)

    now = datetime.now()
    print(f"\n🕐 Server Time: {now.strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"📅 Day of Week: {now.strftime('%A')}")

    print("\n" + "="*60)
    print("📋 Current Schedules Configuration:")
    print("="*60)

    import json
    print(json.dumps(auto_schedules, indent=2))

    print("\n" + "="*60)
    print("🔍 Checking each motor schedule:")
    print("="*60)

    for motor_id in auto_schedules.keys():
        result = check_schedule(motor_id)
        print(f"\n{'='*60}")

    print("\n" + "="*60)
    print("📊 SUMMARY")
    print("="*60)

    enabled_count = sum(1 for s in auto_schedules.values() if s['enabled'])
    active_count = sum(1 for mid in auto_schedules.keys() if check_schedule(mid))

    print(f"✅ Enabled schedules: {enabled_count}/3")
    print(f"🟢 Currently active (should be ON): {active_count}/3")

    if enabled_count == 0:
        print("\n⚠️  WARNING: No schedules are enabled!")
        print("   → Enable at least one schedule in the app")

    if enabled_count > 0 and active_count == 0:
        print("\n⚠️  WARNING: Schedules enabled but none are active!")
        print("   Possible reasons:")
        print("   1. Current day not in schedule days")
        print("   2. Current time not in schedule time range")
        print("   3. No days configured in schedule")

    if active_count > 0:
        print(f"\n✅ SUCCESS: {active_count} motor(s) should be running now!")

if __name__ == "__main__":
    print("\n" + "="*60)
    print("⚠️  INSTRUCTIONS:")
    print("="*60)
    print("1. Update the 'auto_schedules' dictionary at the top of this file")
    print("   with your actual schedule configuration")
    print("2. Set 'enabled' to True for motors you want to test")
    print("3. Add days to the 'days' list (e.g., ['Monday', 'Tuesday'])")
    print("4. Run this script: python3 test_schedule_logic.py")
    print("="*60)

    input("\nPress Enter to run the test...")

    main()

