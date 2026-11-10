# Rongo for Android

Colorful notes, checklists, spreadsheet tables, reminders (real Android notifications),
calendar, clock, focus timer and priorities. Your data stays on the phone.

## First launch

- Allow notifications when asked.
- Open Settings in the app and tap "Allow system alerts". On Android 12 and newer this
  may take you to an "Alarms and reminders" screen: switch it on so reminders fire on time.
- On some phones (Xiaomi, Oppo, Vivo, Samsung) also set battery use for Rongo to
  "Unrestricted" so the system does not delay reminders.

## What is new in 1.5.0

- Home-screen widgets show a note you choose. Add a widget, pick a note and a look (Glass, Night, Priority or Minimal). Each widget can show a different note.
- Tap a widget to open its note.
- Notes is in the middle of the bottom bar, with Settings beside it.
- Archive and Trash are in Settings.

## What is new in 1.4.0

- Home-screen widget "Rongo priorities" with Urgent, High, Medium and Low tiles. Its colours follow the priority and the time of day.
- Updates install over the previous version without uninstalling. Every build is signed with the same release key (see below).

### Signing (one-time setup for the maintainer)
The build signs the APK with a key kept in GitHub secrets: `RONGO_KEYSTORE_B64` (base64 of rongo-release.jks), `RONGO_STORE_PASSWORD` and `RONGO_KEY_PASSWORD`. Keep the keystore file safe. Losing it means future updates can't install over the app.

## What is new in 1.3.0

- Notes use softer, glass-like colors that show the blurred background through them.
- Priority widgets on the Today screen show your Urgent, High, Medium and Low items, with the next items and times.
- Each widget changes color with the time of day: morning, afternoon, evening and night.

## What is new in 1.2.0

- Glass look: panels, buttons, the menu bar, the search bar and the preview use frosted, blurred surfaces.
- A tip on the Notes screen explains that you can long press a note to preview it. It hides after you use it or dismiss it.
- Light vibration when you press buttons and tick items, and a short buzz when you delete, archive or finish. Turn it off in Settings > Tap vibration.

### Earlier in 1.1.x

- Reminder sounds are about 5 seconds, louder, and vibrate, with a different pattern for each priority (Low, Medium, High, Urgent). Android keeps a separate channel for each level; you can change them in Android Settings > Apps > Rongo > Notifications.
- Reminder notifications show the Rongo logo.
- Writing no longer jumps to the top, and the line you are typing stays visible, including when the keyboard covers the bottom of the screen.
- Numbered lists work with `1.` and `1.x`, in English and Bangla digits. Enter continues the list; Enter on an empty item ends it. Bullets `- item` work too.
- Tap a note, checklist or table to edit it. Long press to see a floating preview with Edit, Duplicate, Archive and Delete.
- Copy and Paste buttons above the note text.
- Tap the Rongo logo for a menu with Settings.
- Clock and calendar use a more premium font.
- A one-time "What is new" window shows after installing or updating.

## Change the app

The whole app is `www/index.html`. After editing it, push to GitHub and the workflow
builds a new APK. To build on your own computer instead: `npm install`, `npx cap sync android`,
then `cd android && ./gradlew assembleDebug` (needs JDK 21 and the Android SDK).
