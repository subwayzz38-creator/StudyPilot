# StudyPilot Android app

A starter Kotlin + Jetpack Compose Android project for the exam revision timetable dated 25 Nov–10 Dec 2026.

## Build APK with GitHub Actions
1. Create a GitHub repository named `StudyPilot`.
2. Upload all files in this project, preserving folder structure.
3. Open **Actions** and run **Build StudyPilot APK** (or push to `main`).
4. Download the `StudyPilot-debug-apk` artifact from the completed workflow.
5. Extract `app-debug.apk` and install it on your Android phone.

## On the phone
- Allow notifications when prompted.
- To reduce delays, set Settings → Apps → StudyPilot → Battery to unrestricted / allow background activity where available.
- This version uses Android inexact allow-while-idle alarms, so battery saving may delay some reminders.

## Notes
- Study topic labels are placeholders from the timetable and should be matched to your course notes.
- This is a starter build. Test reminders on your device before relying on them for exam-day alerts.
