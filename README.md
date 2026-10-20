# Rongo for Android

Colorful notes, checklists, spreadsheet tables, reminders (real Android notifications),
calendar, clock, focus timer and priorities. Your data stays on the phone.

## Get the APK (about 10 minutes, no Android Studio)

1. Create a free account at github.com and make a new repository (private is fine).
2. Upload everything in this folder to it. On the repo page choose Add file > Upload files
   and drag the contents in. Make sure the hidden `.github` folder is included
   (it holds `workflows/build-apk.yml`).
3. Open the Actions tab. The "Build Rongo APK" run starts by itself (or press Run workflow).
   It takes about 5 minutes.
4. When it turns green, open the repo's Releases section (right side of the repo page),
   open "Rongo APK", and tap `Rongo.apk` to download it on your phone.
5. Open the file. Android asks to allow installs from your browser: allow it, then Install.
   (Play Protect may warn about an unknown app. This is your own build: choose Install anyway.)

## First launch

- Allow notifications when asked.
- Open Settings in the app and tap "Allow system alerts". On Android 12 and newer this
  may take you to an "Alarms and reminders" screen: switch it on so reminders fire on time.
- On some phones (Xiaomi, Oppo, Vivo, Samsung) also set battery use for Rongo to
  "Unrestricted" so the system does not delay reminders.

## What is new in 1.1

- Reminders sound different by priority. Android keeps a separate notification channel
  for each level (Low, Medium, High, Urgent), so you can hear the urgency. You can change
  any of them in Android Settings > Apps > Rongo > Notifications.
- Reminder notifications show the Rongo logo.
- Writing no longer jumps back to the top, and the line you are typing stays on screen.
- Lists continue automatically: `1. item` + Enter gives `2.`; Enter on an empty item ends the list. Bullets `- item` work the same way.
- Copy and Paste buttons above the note text.
- Tap the Rongo logo to open a menu with Settings.
- Tapping a note, list or table opens a clean full-page view. Use the three dots to Edit, Duplicate, Archive or Delete.

## Change the app

The whole app is `www/index.html`. After editing it, push to GitHub and the workflow
builds a new APK. To build on your own computer instead: `npm install`, `npx cap sync android`,
then `cd android && ./gradlew assembleDebug` (needs JDK 21 and the Android SDK).
