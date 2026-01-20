# M-Hike App - Features Documentation
## Quick Reference Guide for Presentation

---

## 📋 FEATURE A: Enter Details of Hikes (10%)

### Files Involved:
1. **`Entities.kt`** - Data model defining Hike entity with all fields
2. **`HikeFormFragment.kt`** - UI for entering hike details
3. **`fragment_hike_form.xml`** - Layout file with input controls
4. **`HikeDraft.kt`** - Temporary data holder for form validation

### Key Functions & Concepts:

#### In `Entities.kt`:
- **`Hike` data class** - Defines all hike fields:
  - **Required fields**: name, location, date, parkingAvailable, lengthKm, difficulty
  - **Optional fields**: description, elevationGainM, rating, photoUri, latitude, longitude
  - **Additional creative fields**: elevationGainM (elevation gain in meters), rating (1-5 stars)

#### In `HikeFormFragment.kt`:
- **`onViewCreated()`** - Sets up the form with proper controls
- **`validateAndBuildDraft()`** - Validates all required fields before proceeding
- **`updateContinueEnabled()`** - Live validation that enables/disables Continue button
- **`showDatePicker()`** - Date picker for selecting hike date
- **`fillForm()`** - Loads existing hike data when editing

### UI Controls Used:
- TextInputEditText - for name, location, description
- AutoCompleteTextView - for difficulty level (dropdown)
- DatePicker - for date selection
- Switch - for parking availability (Yes/No)
- EditText with number input - for length in km
- Slider - for elevation gain
- RatingBar - for rating (0-5 stars)

### Validation:
- Error messages shown via `TextInputLayout.error` property
- Toast messages for user feedback
- Required fields checked before allowing user to continue

---

## 📦 FEATURE B: Store, View and Delete Hike Details (15%)

### Files Involved:
1. **`AppDatabase.kt`** - SQLite database configuration using Room
2. **`Daos.kt`** - Database Access Objects (HikeDao, ObservationDao)
3. **`HikeRepository.kt`** - Repository pattern for data operations
4. **`HikeListFragment.kt`** - Display list of all hikes
5. **`HikeDetailFragment.kt`** - View individual hike details
6. **`HikeViewModel.kt`** - ViewModel for managing hike data

### Key Functions & Concepts:

#### In `AppDatabase.kt`:
- **`@Database` annotation** - Defines database with Hike and Observation tables, version 4
- **`getInstance()`** - Singleton pattern for database access
- **`MIGRATION_2_3`** - Database migration adding latitude/longitude
- **`MIGRATION_3_4`** - Database migration adding addedToCalendar field

#### In `Daos.kt` (HikeDao):
- **`observeAll()`** - Returns Flow of all hikes (reactive updates)
- **`getById(id: Long)`** - Retrieve single hike by ID
- **`insert(hike: Hike)`** - Add new hike to database
- **`update(hike: Hike)`** - Update existing hike
- **`delete(hike: Hike)`** - Delete single hike
- **`deleteAll()`** - Reset database (delete all hikes)

#### In `HikeRepository.kt`:
- Wrapper around DAO providing clean API for ViewModels
- All functions delegate to DAO methods

#### In `HikeViewModel.kt`:
- **`hikes: LiveData<List<Hike>>`** - Observable list of all hikes
- **`insert()`**, **`update()`**, **`delete()`** - CRUD operations
- **`deleteAll()`** - Reset database functionality
- Uses coroutines for async database operations

#### In `HikeListFragment.kt`:
- **`setupRecyclerView()`** - Displays list of hikes using RecyclerView
- **`observeHikes()`** - Observes LiveData for automatic UI updates
- **`confirmDeleteHike()`** - Shows confirmation dialog before deletion
- **`confirmResetDatabase()`** - Shows confirmation dialog before resetting DB
- **Menu action: "Reset Database"** - Deletes all hike data

#### In `HikeDetailFragment.kt`:
- **`bindHike()`** - Displays full hike details with all fields
- Displays hike information using Chips for attributes
- Shows photo if available

### Storage:
- SQLite database named **"mhike.db"**
- Uses Room persistence library
- Data persists on device storage
- Foreign key relationships for observations

---

## 🔍 FEATURE C: Add Observations to a Hike (15%)

### Files Involved:
1. **`Entities.kt`** - Observation data model
2. **`ObservationFormFragment.kt`** - Form for entering observations
3. **`ObservationConfirmFragment.kt`** - Confirmation screen
4. **`ObservationAdapter.kt`** - RecyclerView adapter for observation list
5. **`ObservationViewModel.kt`** - ViewModel for observation operations
6. **`ObservationRepository.kt`** - Repository for observation data
7. **`HikeDetailFragment.kt`** - Shows observations list for a hike

### Key Functions & Concepts:

#### In `Entities.kt`:
- **`Observation` data class** - Defines observation entity:
  - **hikeId** - Foreign key linking to parent Hike
  - **observation** - Required field (the observation text)
  - **timestamp** - Required field (time of observation)
  - **comments** - Optional field (additional notes)
  - **photoUri** - Optional field (photo of observation)
- **`@ForeignKey`** - Cascade delete (deleting hike deletes observations)

#### In `ObservationFormFragment.kt`:
- **`onViewCreated()`** - Sets up observation form
- **`pickDateTime()`** - Date & time picker with default to current time
- **`updateDateTimeField()`** - Formats and displays timestamp
- **`validateAndSave()`** - Validates required observation field
- **Photo support** - Can attach photos to observations
- **Edit mode** - Can edit existing observations

#### In `Daos.kt` (ObservationDao):
- **`observeByHike(hikeId: Long)`** - Get all observations for a specific hike
- **`insert(obs: Observation)`** - Add new observation
- **`update(obs: Observation)`** - Update existing observation
- **`delete(obs: Observation)`** - Delete single observation
- **`deleteByHike(hikeId: Long)`** - Delete all observations for a hike
- **`deleteAll()`** - Delete all observations

#### In `HikeDetailFragment.kt`:
- **`setupRecyclerView()`** - Displays observations list
- **`obsVm.observeByHike(hikeId)`** - Observes observations for current hike
- **FAB button** - "Add Observation" floating action button
- **`navigateToObservationForm()`** - Opens observation form
- **Edit/Delete actions** - Available on each observation item

#### Multiple Observations:
- Each hike can have unlimited observations
- Displayed in RecyclerView sorted by timestamp (newest first)
- Each observation can be viewed, edited, or deleted independently

---

## 🔎 FEATURE D: Search (10%)

### Files Involved:
1. **`HikeListFragment.kt`** - Search bar and search logic
2. **`Daos.kt`** - Search queries (HikeDao)
3. **`AdvancedSearchBottomSheet.kt`** - Advanced filter dialog
4. **`SearchStateManager.kt`** - Manages search state

### Key Functions & Concepts:

#### Simple Search - In `HikeListFragment.kt`:
- **`setupSearchBar()`** - Text input for search
- **`performSearch(query: String)`** - Executes search and updates list
- **Search field**: EditText with TextWatcher for real-time search
- **Partial matching**: Searches for hikes containing the entered text

#### In `Daos.kt`:
- **`searchByNameContains(query: String)`** - SQL query using LIKE operator
  ```sql
  SELECT * FROM hikes WHERE name LIKE '%' || :query || '%'
  ```
- Returns Flow for reactive search results

#### Advanced Search - In `AdvancedSearchBottomSheet.kt`:
- **Multiple criteria**:
  - Name (partial match)
  - Location (partial match)
  - Length range (min/max using RangeSlider)
  - Date (exact match)
  - Difficulty level (Easy/Moderate/Hard)
  - Parking availability (Yes/No)
- **`btnApply.setOnClickListener()`** - Applies selected filters
- **`btnClear.setOnClickListener()`** - Clears all filters

#### In `Daos.kt`:
- **`advancedSearch()`** - Complex SQL query with multiple optional parameters
  - Uses NULL checks to make each parameter optional
  - Combines all criteria with AND logic
  - Sorts results by date DESC, name ASC

#### In `HikeListFragment.kt`:
- **`applyAdvancedFilter()`** - Processes advanced search results
- **`clearAdvancedFilter()`** - Resets to show all hikes
- **Filter chip** - Shows when advanced filter is active
- **Sorting options** - Sort by date, name, or length

#### Search Features:
- **Real-time search** - Updates as you type
- **Partial matching** - Finds "Snow" in "Snowdon"
- **Multiple criteria** - Combine name, location, length, date, difficulty, parking
- **Filter indicator** - Chip shows when advanced filter is active
- **Sort options** - Sort results by different fields

---

## 📱 FEATURE G: Additional Features (10%)

### Additional Features Implemented:

### 1. **Photo Support**
**Files**: `PhotoPickerHelper.kt`, `HikeFormFragment.kt`, `ObservationFormFragment.kt`

#### Key Functions:
- **`showPhotoOptionsDialog()`** - Choose camera or gallery
- **`launchCamera()`** - Take photo with camera
- **`pickImageLauncher`** - Select from gallery
- **Permission handling** - Requests CAMERA and READ_MEDIA_IMAGES permissions
- **Photo display** - Shows photos in hike details and observations
- **Storage** - Stores photo URI in database

### 2. **Location Features (GPS & Maps)**
**Files**: `MapPickerFragment.kt`, `HikeFormFragment.kt`, `HikeDetailFragment.kt`, `PermissionHelper.kt`

#### Key Functions:
- **`fetchAndFillLocation()`** - Gets current GPS location automatically
- **`btnUseLocation.setOnClickListener()`** - Button to use current location
- **`MapPickerFragment`** - Interactive map to pick location
  - Uses **OSMDroid** library for maps
  - **Tap to select** location on map
  - **Search location** - Geocoding to find places
  - **Reverse geocoding** - Convert coordinates to address
- **`bindHike()` in HikeDetailFragment** - Shows map preview if coordinates available
- **Permission handling** - Requests ACCESS_FINE_LOCATION permission

#### Map Features:
- **Interactive map picker** - Visual location selection
- **GPS integration** - Automatic location detection
- **Geocoding** - Search for places by name
- **Map preview** - Shows hike location on mini-map in details

### 3. **Calendar Integration**
**Files**: `CalendarHelper.kt`, `HikeDetailFragment.kt`

#### Key Functions:
- **`createCalendarIntent(hike: Hike)`** - Creates calendar event
  - Parses date from string format
  - Sets all-day event
  - Includes hike details in description
  - Sets location field
- **`addToCalendar()`** - Adds hike to device calendar
- **Menu action** - "Add to Calendar" in detail screen
- **State tracking** - `addedToCalendar` field prevents duplicates
- **Reminder feature** - Event appears in user's calendar app

### 4. **Import/Export JSON**
**Files**: `ImportExportManager.kt`, `HikeListFragment.kt`, `HikeDetailFragment.kt`

#### Key Functions:
- **`exportAsJson()`** - Exports single hike with observations
  - Creates JSON representation
  - Includes all hike fields and observations
  - Shareable via Intent (email, messaging, etc.)
- **`parseImportJson()`** - Parses JSON text
- **`handleImportUri()`** - Imports from file
- **`showImportTextDialog()`** - Import by pasting JSON
- **Duplicate detection** - Checks if hike already exists
- **Menu actions**:
  - "Import JSON" - From file picker
  - "Import JSON (Paste)" - From clipboard
  - "Export as JSON" - Share hike data

### 5. **Internationalization (i18n)**
**Files**: `values/strings.xml`, `values-es/strings.xml`, `values-fr/strings.xml`

- **Multiple languages**: English, Spanish, French
- **All UI text** externalized to strings.xml
- **Automatic language switching** based on device settings
- **Accessibility** - Content descriptions for screen readers

### 6. **Enhanced Validation**
- **Duplicate detection** - Warns if similar hike exists
- **Unsaved changes dialog** - Prevents accidental data loss
- **Confirmation screens** - Review data before saving
- **Live validation** - Enables/disables buttons based on input

### 7. **Material Design UI**
- **Material 3 components** - Modern Android design
- **Edge-to-edge display** - Immersive experience
- **Dark mode support** - Theme switching
- **Smooth animations** - Navigation transitions
- **Chips for attributes** - Visual data presentation
- **FAB buttons** - Quick actions
- **Bottom sheets** - Advanced filters

---

## 🏗️ ARCHITECTURE & DESIGN PATTERNS

### Architecture Pattern: **MVVM (Model-View-ViewModel)**

#### Layers:

1. **Data Layer**:
   - `Entities.kt` - Data models
   - `Daos.kt` - Database access
   - `AppDatabase.kt` - Database configuration
   - `*Repository.kt` - Repository pattern

2. **Domain Layer**:
   - `*ViewModel.kt` - Business logic and state management
   - `ServiceLocator.kt` - Dependency injection

3. **Presentation Layer**:
   - `*Fragment.kt` - UI controllers
   - `*.xml` - Layout files
   - `*Adapter.kt` - RecyclerView adapters

### Key Design Patterns:

1. **Repository Pattern** - Abstracts data sources
2. **ViewModel Pattern** - Survives configuration changes
3. **Observer Pattern** - LiveData for reactive UI
4. **Singleton Pattern** - Database instance
5. **Factory Pattern** - ViewModels
6. **Adapter Pattern** - RecyclerView adapters

### Libraries Used:

- **Room** - SQLite database ORM
- **Coroutines** - Asynchronous operations
- **LiveData** - Observable data holder
- **Navigation Component** - Fragment navigation
- **Material Components** - UI components
- **OSMDroid** - Maps integration
- **ViewBinding** - Type-safe view access

---

## 📂 FILE STRUCTURE SUMMARY

### Core Data Files:
- `Entities.kt` - Hike and Observation models
- `Daos.kt` - Database queries
- `AppDatabase.kt` - Database setup

### Repository Files:
- `HikeRepository.kt` - Hike data operations
- `ObservationRepository.kt` - Observation data operations

### ViewModel Files:
- `HikeViewModel.kt` - Hike business logic
- `ObservationViewModel.kt` - Observation business logic

### Hike UI Files:
- `HikeListFragment.kt` - List all hikes
- `HikeFormFragment.kt` - Add/edit hike
- `HikeConfirmFragment.kt` - Confirm hike data
- `HikeDetailFragment.kt` - View hike details
- `MapPickerFragment.kt` - Pick location on map
- `AdvancedSearchBottomSheet.kt` - Advanced search filters

### Observation UI Files:
- `ObservationFormFragment.kt` - Add/edit observation
- `ObservationConfirmFragment.kt` - Confirm observation
- `ObservationAdapter.kt` - Display observations list

### Utility Files:
- `PhotoPickerHelper.kt` - Photo selection helper
- `CalendarHelper.kt` - Calendar integration
- `ImportExportManager.kt` - JSON import/export
- `SearchStateManager.kt` - Search state management
- `PermissionHelper.kt` - Runtime permissions

### Main Files:
- `MainActivity.kt` - App entry point
- `HikerApp.kt` - Application class

---

## 🎯 KEY POINTS FOR PRESENTATION

### When Professor Asks About:

**"How do you validate required fields?"**
- Answer: In `HikeFormFragment.kt`, the `validateAndBuildDraft()` function checks all required fields. It shows error messages using `TextInputLayout.error` property and returns null if validation fails.

**"Where is the database?"**
- Answer: SQLite database configured in `AppDatabase.kt` using Room. The database file is "mhike.db" stored in app's private storage. `HikeDao` and `ObservationDao` in `Daos.kt` define all SQL queries.

**"How do you handle multiple observations?"**
- Answer: The `Observation` entity has a foreign key `hikeId` linking to the parent `Hike`. The `observeByHike(hikeId)` query in `ObservationDao` retrieves all observations for a specific hike. They're displayed in a RecyclerView in `HikeDetailFragment.kt`.

**"How does search work?"**
- Answer: Simple search uses `searchByNameContains()` in `HikeDao` with SQL LIKE operator. Advanced search uses `advancedSearch()` with multiple optional parameters. Search UI is in `HikeListFragment.kt` and filter dialog is `AdvancedSearchBottomSheet.kt`.

**"What are your additional features?"**
- Answer: 1) Camera & gallery photo support (`PhotoPickerHelper.kt`), 2) GPS location & interactive maps (`MapPickerFragment.kt`), 3) Calendar integration (`CalendarHelper.kt`), 4) JSON import/export (`ImportExportManager.kt`), 5) Multi-language support (strings.xml in multiple languages), 6) Material Design 3 UI with dark mode.

**"How do you delete all hikes?"**
- Answer: `HikeListFragment.kt` has a menu option "Reset Database" that calls `confirmResetDatabase()`. After user confirmation, it calls `hikeVm.deleteAll()` which executes `deleteAll()` query in `HikeDao`.

**"What design pattern did you use?"**
- Answer: MVVM (Model-View-ViewModel) architecture. Data layer (Entities, DAOs, Database), Domain layer (ViewModels, Repositories), Presentation layer (Fragments, Adapters). Also used Repository pattern, Observer pattern with LiveData, and Singleton for database.

---

## ✅ FEATURES CHECKLIST

- ✅ **Feature A**: Enter hike details with all required & optional fields
- ✅ **Feature B**: Store in SQLite, view all hikes, edit, delete individual, reset database
- ✅ **Feature C**: Add multiple observations per hike with timestamp, comments, photos
- ✅ **Feature D**: Search by name (simple & advanced with multiple criteria)
- ✅ **Feature E**: Not applicable (server upload - skipped)
- ✅ **Feature F**: Not applicable (React Native - skipped)
- ✅ **Feature G**: Photos, GPS/Maps, Calendar, JSON import/export, i18n, enhanced UI

**Total Implementation**: Native Android app in Kotlin with all core features + extensive additional features.

