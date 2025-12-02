#!/usr/bin/env python3
"""
Script to manage permitted emails
Usage:
    python manage_emails.py list                    # List all permitted emails
    python manage_emails.py add <email>             # Add email to permitted list
    python manage_emails.py remove <email>          # Remove email from permitted list
"""
import json
import sys
import os

PERMITTED_EMAILS_PATH = 'permitted_emails.json'

def load_permitted_emails():
    if os.path.exists(PERMITTED_EMAILS_PATH):
        with open(PERMITTED_EMAILS_PATH, 'r') as f:
            return json.load(f)
    return []

def save_permitted_emails(emails):
    with open(PERMITTED_EMAILS_PATH, 'w') as f:
        json.dump(emails, f, indent=2)
    print(f"✅ Saved to {PERMITTED_EMAILS_PATH}")

def list_emails():
    emails = load_permitted_emails()
    if not emails:
        print("📭 No permitted emails found")
        return

    print(f"📧 Permitted Emails ({len(emails)}):")
    for i, email in enumerate(emails, 1):
        print(f"  {i}. {email}")

def add_email(email):
    emails = load_permitted_emails()
    if email in emails:
        print(f"⚠️  Email already exists: {email}")
        return

    emails.append(email)
    save_permitted_emails(emails)
    print(f"✅ Added email: {email}")

def remove_email(email):
    emails = load_permitted_emails()
    if email not in emails:
        print(f"⚠️  Email not found: {email}")
        return

    emails.remove(email)
    save_permitted_emails(emails)
    print(f"✅ Removed email: {email}")

def main():
    if len(sys.argv) < 2:
        print("Usage:")
        print("  python manage_emails.py list")
        print("  python manage_emails.py add <email>")
        print("  python manage_emails.py remove <email>")
        sys.exit(1)

    command = sys.argv[1].lower()

    if command == "list":
        list_emails()
    elif command == "add":
        if len(sys.argv) < 3:
            print("❌ Error: Missing email address")
            sys.exit(1)
        add_email(sys.argv[2])
    elif command == "remove":
        if len(sys.argv) < 3:
            print("❌ Error: Missing email address")
            sys.exit(1)
        remove_email(sys.argv[2])
    else:
        print(f"❌ Unknown command: {command}")
        sys.exit(1)

if __name__ == "__main__":
    main()

