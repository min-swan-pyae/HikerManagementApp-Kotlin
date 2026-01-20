# Core Components Explanation - HikerApp, MainActivity, Reminders, ReminderWorker

This document explains the core infrastructure components that support the entire M-Hike application.

---

## 1. HikerApp.kt - Application Class

**Location:** `app/src/main/java/com/example/hikermanagementapp/HikerApp.kt`

### Purpose
This is the **main Application class** that runs when the app first starts, BEFORE any activities or fragments are created.

### Key Responsibilities
1. **Initialize app-wide settings** (Material Design 3 dynamic colors)
2. **Create notification channels** for hike reminders (Feature G)
3. **Provide singleton instance** accessible throughout the app

### Important Functions

#### `onCreate()`
- **When Called:** App startup (runs once)
- **What It Does:**
  - Stores singleton instance: `instance = this`
  - Applies dynamic colors: `DynamicColors.applyToActivitiesIfAvailable(this)`
  - Creates notification channel: `createNotificationChannel()`

#### `createNotificationChannel()`
- **Purpose:** Sets up notification channel for reminders
- **Why Needed:** Android 8.0+ requires all notifications to belong to a channel
- **Channel Details:**
  - ID: `"mhike_reminders"` (constant: `CHANNEL_REMINDERS`)
  - Name: `"Hike Reminders"`
  - Importance: `IMPORTANCE_DEFAULT`
- **Used By:** ReminderWorker when displaying notifications

### Important Constants
- `CHANNEL_REMINDERS = "mhike_reminders"` - Notification channel ID
- `instance` - Singleton instance for accessing app context anywhere

### Connects To
- **AndroidManifest.xml** - Registered as `android:name=".HikerApp"`
- **ReminderWorker** - Uses the notification channel created here
- **Repository classes** - Can access via `HikerApp.instance`

### Feature Connection
**Feature G (Additional Features)** - Part of the hike reminder notification system

---

## 2. MainActivity.kt - Main Activity

**Location:** `app/src/main/java/com/example/hikermanagementapp/MainActivity.kt`

### Purpose
This is the **ONLY Activity** in the entire app. Uses **Single Activity Architecture** where all screens are fragments managed by Navigation Component.

### Key Responsibilities
1. **Host all fragments** via NavHostFragment
2. **Set up toolbar** (top bar with title and back button)
3. **Handle navigation** between fragments
4. **Apply edge-to-edge display** and system bar insets

### Important Functions

#### `onCreate(savedInstanceState: Bundle?)`
- **When Called:** Activity is created
- **What It Does:**
  1. Enables edge-to-edge display: `enableEdgeToEdge()`
  2. Sets layout: `setContentView(R.layout.activity_main)`
  3. Handles system bar insets (padding)
  4. Calls `setupNavigation()` to configure navigation

#### `setupNavigation()`
- **Purpose:** Connects toolbar with Navigation Component
- **What It Does:**
  1. Finds NavHostFragment: `supportFragmentManager.findFragmentById(R.id.nav_host_fragment)`
  2. Gets NavController: `fragment.navController`
  3. Configures toolbar: `setSupportActionBar(toolbar)`
  4. Defines top-level destinations: `AppBarConfiguration(setOf(R.id.hikeListFragment))`
  5. Connects toolbar to navigation: `toolbar.setupWithNavController(navController, appBarConfiguration)`
  6. Sets up back button click handler
  7. Applies Material Design 3 colors to toolbar

**Top-level destination:** HikeListFragment (no back button shown)
**Other screens:** Back button automatically appears

#### `onSupportNavigateUp(): Boolean`
- **When Called:** User presses back button in toolbar
- **Priority Order:**
  1. Check if fragment has `OnBackPressedCallback` (e.g., unsaved changes dialog)
  2. Use NavController to navigate back
  3. Use default activity back behavior
- **Returns:** `true` if navigation handled, `false` otherwise

### Layout Components
**activity_main.xml** contains:
- `MaterialToolbar` (id: toolbar) - Top app bar
- `NavHostFragment` (id: nav_host_fragment) - Container for all fragments

### Navigation Architecture
```
MainActivity (Activity)
    └── NavHostFragment (Fragment Container)
            ├── HikeListFragment (Home screen)
            ├── AddHikeFragment (Add new hike)
            ├── HikeDetailFragment (View hike details)
            ├── EditHikeFragment (Edit hike)
            ├── ObservationListFragment (View observations)
            └── AddObservationFragment (Add observation)
```

### Feature Connection
Supports **ALL FEATURES (A-G)** by hosting all fragment screens

---

## 3. Reminders.kt - Reminder Scheduler

**Location:** `app/src/main/java/com/example/hikermanagementapp/util/Reminders.kt`

### Purpose
**Schedules notifications** to remind users about upcoming hikes on the morning of the hike (8:00 AM).

### Key Responsibilities
1. **Calculate delay** from now until notification time
2. **Create WorkRequest** with hike details
3. **Schedule with WorkManager** (ensures notification shows even if app is closed)

### Important Functions

#### `scheduleReminder(context, hikeId, hikeName, dateIso)`
- **Parameters:**
  - `context: Context` - Application context
  - `hikeId: Long` - Hike database ID (for unique identification)
  - `hikeName: String` - Name to show in notification
  - `dateIso: String` - Hike date in format "yyyy-MM-dd"

- **Process:**
  1. Parse date string: `LocalDate.parse(dateIso)`
  2. Set trigger time: `ZonedDateTime.of(date, LocalTime.of(8, 0), ZoneId.systemDefault())`
  3. Calculate delay: `Duration.between(now, trigger).toMillis()`
  4. Handle past dates: If delay ≤ 0, set to 1 minute
  5. Create data bundle with hike info
  6. Build WorkRequest: `OneTimeWorkRequestBuilder<ReminderWorker>()`
  7. Schedule with WorkManager using unique name: `"hike_reminder_" + hikeId`

- **Work Policy:** `ExistingWorkPolicy.REPLACE` - New reminder replaces old one for same hike

### Important Constants
- `UNIQUE_PREFIX = "hike_reminder_"` - Prefix for unique work names

### Example Flow
```
User adds hike "Snowdon" on 2024-12-25
    ↓
HikeRepository calls Reminders.scheduleReminder()
    ↓
Reminders calculates: Dec 25 at 8:00 AM
    ↓
WorkManager stores the task
    ↓
On Dec 25 at 8:00 AM: WorkManager triggers ReminderWorker
    ↓
User sees notification: "Snowdon on 2024-12-25"
```

### Used By
- **HikeRepository** - Calls after inserting/updating hikes

### Uses
- **WorkManager** - Android Jetpack library for background tasks
- **ReminderWorker** - The worker that actually shows the notification

### Feature Connection
**Feature G (Additional Features)** - Hike reminder notifications

---

## 4. ReminderWorker.kt - Notification Display Worker

**Location:** `app/src/main/java/com/example/hikermanagementapp/util/ReminderWorker.kt`

### Purpose
**Displays the actual notification** when WorkManager triggers it at the scheduled time.

### Key Responsibilities
1. **Execute in background** at scheduled time
2. **Check notification permission** (Android 13+)
3. **Build and display notification**

### Important Functions

#### `doWork(): Result`
- **When Called:** By WorkManager at scheduled time (set by Reminders.kt)
- **Runs On:** Background thread (won't freeze UI)

- **Process:**
  1. Extract data from input:
     - `hikeId = inputData.getLong(KEY_HIKE_ID, -1L)`
     - `hikeName = inputData.getString(KEY_HIKE_NAME)`
     - `date = inputData.getString(KEY_HIKE_DATE)`
  
  2. Get NotificationManager: `NotificationManagerCompat.from(applicationContext)`
  
  3. Check permission:
     - Android 13+: Check `POST_NOTIFICATIONS` permission
     - Android 12-: Always allowed
  
  4. Build notification:
     ```kotlin
     NotificationCompat.Builder(context, HikerApp.CHANNEL_REMINDERS)
         .setSmallIcon(R.drawable.ic_launcher_foreground)
         .setContentTitle("Hike Reminder")
         .setContentText("$hikeName on $date")
         .setAutoCancel(true)
         .build()
     ```
  
  5. Show notification: `nm.notify(hikeId.toInt(), notif)`
  
  6. Return success: `Result.success()`

### Important Constants
- `KEY_HIKE_ID = "hike_id"` - Key for hike ID
- `KEY_HIKE_NAME = "hike_name"` - Key for hike name
- `KEY_HIKE_DATE = "hike_date"` - Key for hike date

**Note:** These keys MUST match the keys used in Reminders.scheduleReminder()

### Notification Details
- **Channel:** `HikerApp.CHANNEL_REMINDERS` (created in HikerApp.kt)
- **Icon:** `ic_launcher_foreground`
- **Title:** Localized string "Hike Reminder"
- **Content:** "{HikeName} on {Date}"
- **Behavior:** Auto-dismiss when tapped

### Data Flow
```
Reminders.kt creates Data bundle:
    Data.Builder()
        .putLong(KEY_HIKE_ID, 123)
        .putString(KEY_HIKE_NAME, "Snowdon")
        .putString(KEY_HIKE_DATE, "2024-12-25")
    ↓
WorkManager stores and waits
    ↓
At scheduled time, WorkManager passes data to ReminderWorker
    ↓
ReminderWorker.doWork() extracts data:
    hikeId = inputData.getLong(KEY_HIKE_ID)
    hikeName = inputData.getString(KEY_HIKE_NAME)
    date = inputData.getString(KEY_HIKE_DATE)
    ↓
Builds and shows notification
```

### Permission Handling
- **Android 13+ (API 33+):** Requires `POST_NOTIFICATIONS` runtime permission
- **Android 12- (API < 33):** No permission needed
- **Code:** Checks permission before showing notification

### Feature Connection
**Feature G (Additional Features)** - Hike reminder notifications

---

## How All Four Components Work Together

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        APP STARTUP                               │
│                                                                  │
│  1. HikerApp.onCreate() runs FIRST                              │
│     - Creates notification channel "mhike_reminders"            │
│     - Sets up Material Design 3 colors                          │
│     - Stores singleton instance                                 │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    MainActivity STARTS                           │
│                                                                  │
│  2. MainActivity.onCreate()                                      │
│     - Sets up toolbar                                           │
│     - Initializes Navigation Component                          │
│     - Loads first fragment (HikeListFragment)                   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                   USER ADDS A HIKE                               │
│                                                                  │
│  3. User enters hike details in AddHikeFragment                 │
│     ↓                                                            │
│  4. HikeRepository.insertHike() saves to database               │
│     ↓                                                            │
│  5. HikeRepository calls Reminders.scheduleReminder()           │
│     - Calculates delay until hike date at 8:00 AM              │
│     - Creates WorkRequest with hike details                     │
│     - Schedules with WorkManager                                │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│               WORKMANAGER WAITS...                               │
│                                                                  │
│  (App can be closed, phone can restart - WorkManager persists)  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│            HIKE DATE ARRIVES (8:00 AM)                           │
│                                                                  │
│  6. WorkManager triggers ReminderWorker.doWork()                │
│     ↓                                                            │
│  7. ReminderWorker checks notification permission               │
│     ↓                                                            │
│  8. ReminderWorker builds notification using channel            │
│     created by HikerApp                                         │
│     ↓                                                            │
│  9. User sees notification: "Snowdon on 2024-12-25"            │
└─────────────────────────────────────────────────────────────────┘
```

---

## Quick Reference for Presentation

### If Professor Asks About HikerApp:
- "It's the Application class that initializes the app"
- "Creates notification channel for reminders"
- "Provides singleton instance for accessing app context"
- "Registered in AndroidManifest.xml"

### If Professor Asks About MainActivity:
- "It's the only Activity - Single Activity Architecture"
- "Hosts all fragments via NavHostFragment"
- "Sets up toolbar with automatic back button"
- "Uses Navigation Component for fragment navigation"

### If Professor Asks About Reminders:
- "Schedules hike reminder notifications"
- "Uses WorkManager for reliable background execution"
- "Calculates delay until hike date at 8:00 AM"
- "Each hike gets unique reminder that can be replaced"

### If Professor Asks About ReminderWorker:
- "Background worker that displays notifications"
- "Triggered by WorkManager at scheduled time"
- "Checks notification permission before showing"
- "Uses notification channel created by HikerApp"

### If Professor Asks How They Connect:
- "HikerApp creates infrastructure (notification channel)"
- "MainActivity hosts all UI screens"
- "Reminders schedules notifications when hikes are added"
- "ReminderWorker shows notifications at scheduled time"
- "All work together for Feature G - Reminder system"

---

## Technologies Used

1. **Application Class** - Android fundamental component
2. **Single Activity Architecture** - Modern Android pattern
3. **Navigation Component** - Jetpack library for navigation
4. **WorkManager** - Jetpack library for background tasks
5. **NotificationCompat** - Backward-compatible notifications
6. **Material Design 3** - Modern UI design system
7. **Coroutines** - Kotlin async programming (in Worker)

---

## Summary

These four components form the **infrastructure** of your M-Hike app:

- **HikerApp** = App initialization and setup
- **MainActivity** = UI container and navigation
- **Reminders** = Scheduling system
- **ReminderWorker** = Notification display

They enable **Feature G (Additional Features)** - the hike reminder notification system, while MainActivity supports all features A-F by hosting their fragment screens.

