# M‑Hike: Hiker Management App (Native Kotlin/Android)


Summary
- Tech stack: Kotlin, AndroidX, Material 3, Room (SQLite), ViewModel + LiveData, Coroutines, Navigation Component, ViewBinding, WorkManager, OSMDroid (OpenStreetMap)
- Min SDK: 28 (Android 9)
- Target/Compile SDK: 36
- Package: com.example.hikermanagementapp



How to build and run
Recommended: Android Studio (Hedgehog+)
- Open the root folder (HikerManagementApp) in Android Studio
- Wait for Gradle sync to finish
- Connect a device or start an emulator (API 28+)
- Run the app (Shift+F10) targeting the ‘app’ module

Windows command line (cmd.exe)
- Ensure JDK 17+ is available (Android Studio bundles JBR). If needed, set JAVA_HOME to your Android Studio JBR:

```
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
```

- Build debug APK:

```
"D:\Andriod Studio Playground\Apps\HikerManagementApp\gradlew.bat" -p "D:\Andriod Studio Playground\Apps\HikerManagementApp" assembleDebug --no-daemon
```

- APK output: app/build/outputs/apk/debug/app-debug.apk

Notes
- If the build machine does not have the Android SDK, configure local.properties (automatically created by Android Studio) with sdk.dir pointing to the SDK location. Building purely from cmd requires the SDK present.
- The project targets recent Android SDKs; ensure your installed SDK platforms and build tools are up to date.

 Permissions and privacy
- Location: ACCESS_FINE/COARSE_LOCATION (for “Use current location” and map overlay)
- Notifications: POST_NOTIFICATIONS (Android 13+) for reminders; optional
- Photos/Camera: CAMERA; READ_MEDIA_IMAGES (Android 13+) or legacy storage read on older devices
- Internet/Network: for OSMDroid map tiles

At runtime, permissions are requested only when a feature needs them. The app remains functional without optional permissions (e.g., you can add hikes without attaching photos).

######
  Data model (Room)
- Hike
  - id (PK, auto)
  - name, location, date (yyyy-MM-dd), parkingAvailable (Boolean)
  - lengthKm (Double), difficulty (String), description (String?)
  - elevationGainM (Int?), rating (Float?), photoUri (String?)
  - latitude (Double?), longitude (Double?)
  - addedToCalendar (Boolean)
- Observation
  - id (PK, auto), hikeId (FK → Hike, cascade delete)
  - observation (String), timestamp (Long epoch millis), comments (String?), photoUri (String?)

######
How to use (quick walkthrough)
- Create hike: + button → fill required fields → optionally use current location or pick on map → optionally add photo/elevation/rating → Continue → Confirm → Save
- List and search: type in “Search by name” to filter; use Filters menu (bottom sheet) for advanced search/sort
- View details: tap a hike to see chips, long location text, description, map preview (if coordinates), and observations
- Add observations: in hike details, use the + button; set observation text (required), adjust date/time, add comments or photo → Save → Confirm
- Edit/Delete: from hike list (for hikes) or hike details item actions (for observations)
- Import: From hike list menu → Import JSON (pick a .json file) or Import JSON (Paste) to paste content exported by this app
- Export: Hike details menu → Export JSON; share to clipboard/email/notes
- Calendar: Hike details menu → Add to Calendar; app remembers that it was added
- Reset DB: Hike list menu → Reset database (deletes everything)

JSON format (export/import)
- Export produces a human-readable text with a JSON block at the end
- Import accepts either the raw JSON or the entire shared text; duplicate hikes are detected based on key fields (name, location, date, length, difficulty, parking, elevationGain, coordinates)

Example JSON
{
  "name": "Snowdon",
  "location": "Llanberis, UK",
  "date": "2025-07-10",
  "parkingAvailable": true,
  "lengthKm": 14.5,
  "difficulty": "Hard",
  "description": "…",
  "elevationGainM": 980,
  "rating": 4.5,
  "latitude": 53.0685,
  "longitude": -4.0760,
  "observations": [
    { "observation": "Raven spotted", "timestamp": 1752135600000, "comments": "near summit" }
  ]
}

 Testing checklist (manual)
Required fields and validation
- Leave a required field blank in the hike form → expect -> disabled Continue button
- Enter a non-positive length → You can't even type "-", only positive value available

CRUD and DB reset
- Add 2–3 hikes, edit one, delete one → list updates accordingly
- Use “Reset database” → all hikes and observations removed

Observations
- Add multiple observations to one hike, edit one, delete one → list updates; empty state visible when none
- Check date/time default is current time; change via pickers

Search and filters
- Type partial name → list filters live
- Open Filters → set length range, difficulty=Moderate, parking=Yes, sort by length → results update and sorted

Location & maps
- Tap “Use current location” (grant permission) → location text updated; “Pick on map” → tap map → confirm → location text updated
- When hike has coordinates, detail page shows a map preview with a marker

Photos
- Add a photo from gallery and from camera for hikes and observations → thumbnails displayed in list/detail

Import/Export
- Export a hike with observations → share text; then Import (Paste) → duplicate detection prevents re-adding the same hike

Calendar & reminders
- From details, “Add to Calendar” → Calendar insert opens; after returning, item becomes “Added to Calendar”
- Reminders are scheduled for the morning of the hike date (requires OS scheduling; may be deferred on some OEMs)

#####
Known limitations and notes
- If your device denies notification permission (Android 13+), reminders won’t appear
- Geocoder availability varies by device/region; app falls back to lat/lng strings when reverse geocoding fails
- OSMDroid downloads map tiles over the network; ensure connectivity
- On very old devices (below API 28) the app is not supported


####
 Build/grade readiness
- The project is structured to be compiled directly in Android Studio with no extra setup beyond the standard Android SDK
- A complete demonstration path is described in “Testing checklist”
- Source code follows Kotlin and Android best practices (clear naming, separation of concerns, ViewModels, repositories)

Troubleshooting
- Gradle sync/build issues: update Android Gradle Plugin, Kotlin, and install required SDK platforms/build tools in Android Studio → SDK Manager
- Missing SDK on CLI builds: open the project once in Android Studio to generate local.properties with your sdk.dir, or create it manually
- Map tiles not loading: check Internet permission and connectivity

License
- Educational coursework project. No warranty.
