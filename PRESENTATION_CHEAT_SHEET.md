# M-Hike Presentation Cheat Sheet
## Quick Answers for Common Questions

---

## 🎯 FEATURE A: Enter Details of Hikes

**"Which file contains the hike data model?"**
→ `Entities.kt` - Contains the `Hike` data class with all required and optional fields

**"Where is the form to enter hike details?"**
→ `HikeFormFragment.kt` - Handles user input with validation

**"How do you validate required fields?"**
→ `validateAndBuildDraft()` function in `HikeFormFragment.kt` checks all fields and shows errors using `TextInputLayout.error`

**"What UI controls did you use?"**
→ TextInputEditText (text), AutoCompleteTextView (dropdown), DatePicker (date), Switch (parking), Slider (elevation), RatingBar (rating)

**"What are your 2+ creative fields?"**
→ elevationGainM (elevation in meters), rating (1-5 stars), photoUri, latitude/longitude coordinates, addedToCalendar

---

## 💾 FEATURE B: Store, View and Delete

**"Where is the database?"**
→ `AppDatabase.kt` - SQLite database named "mhike.db" using Room ORM, version 4

**"Which file has the database queries?"**
→ `Daos.kt` - Contains `HikeDao` and `ObservationDao` interfaces with all SQL queries

**"How do you display all hikes?"**
→ `HikeListFragment.kt` uses RecyclerView with `TagHikeAdapter`, observes `hikeVm.hikes` LiveData

**"How do you delete a single hike?"**
→ `confirmDeleteHike()` in `HikeListFragment.kt` → calls `hikeVm.delete(hike)` → executes `@Delete` query in `HikeDao`

**"How do you reset the database?"**
→ Menu option "Reset Database" → `confirmResetDatabase()` → `hikeVm.deleteAll()` → executes `DELETE FROM hikes` query

**"Where do you edit hikes?"**
→ Click edit button → navigates to `HikeFormFragment.kt` with hikeId argument → loads existing data with `fillForm()`

---

## 📝 FEATURE C: Add Observations

**"Where is the observation data model?"**
→ `Entities.kt` - Contains `Observation` data class with foreign key to Hike

**"How do observations link to hikes?"**
→ Foreign key: `hikeId` column references `hikes.id`, with `CASCADE DELETE` (deleting hike deletes observations)

**"Where do you add observations?"**
→ `ObservationFormFragment.kt` - Form to enter observation, timestamp, comments, photo

**"How do you display observations for a hike?"**
→ `HikeDetailFragment.kt` uses RecyclerView with `ObservationAdapter`, observes `obsVm.observeByHike(hikeId)`

**"Can you add multiple observations?"**
→ Yes! Each observation has unique ID and links to parent hike via hikeId. Query: `observeByHike(hikeId)` returns all observations for that hike

**"What's the required field?"**
→ observation (the text), timestamp (defaults to current time)

---

## 🔍 FEATURE D: Search

**"How does simple search work?"**
→ `HikeListFragment.kt` has search bar → `performSearch()` → calls `searchByNameContains(query)` in `HikeDao` using SQL `LIKE '%query%'`

**"Where is the search query?"**
→ `Daos.kt`, line ~51: `@Query("SELECT * FROM hikes WHERE name LIKE '%' || :query || '%'")`

**"What is advanced search?"**
→ `AdvancedSearchBottomSheet.kt` - Bottom sheet dialog with multiple filters (name, location, length range, date, difficulty, parking)

**"How many search criteria?"**
→ 6 criteria: name, location, length (min/max), date, difficulty, parking + 3 sort options (date, name, length)

**"Where is the advanced search query?"**
→ `Daos.kt`, `advancedSearch()` function with nullable parameters - only filters by non-null values

---

## ⭐ FEATURE G: Additional Features

**"What additional features did you add?"**
→ 
1. **Photo support** (camera & gallery) - `PhotoPickerHelper.kt`
2. **GPS & Maps** (location picker) - `MapPickerFragment.kt`
3. **Calendar integration** - `CalendarHelper.kt`
4. **JSON import/export** - `ImportExportManager.kt`
5. **Multi-language** (English, Spanish, French)
6. **Material Design 3** with dark mode

**"How do photos work?"**
→ `PhotoPickerHelper.kt` handles camera/gallery selection with permission handling. Photo URI stored in database, displayed using ImageView

**"How does map picker work?"**
→ `MapPickerFragment.kt` uses OSMDroid library, tap to select location, geocoding for address, returns lat/lng coordinates

**"How does calendar work?"**
→ `CalendarHelper.createCalendarIntent()` creates Intent with `CalendarContract`, opens device calendar app with pre-filled event

**"What library for maps?"**
→ OSMDroid (open-source alternative to Google Maps)

---

## 🏗️ ARCHITECTURE

**"What architecture did you use?"**
→ MVVM (Model-View-ViewModel) with Repository pattern

**"What are the layers?"**
→ 
- **Data Layer**: Entities, DAOs, AppDatabase, Repositories
- **Domain Layer**: ViewModels, ServiceLocator
- **Presentation Layer**: Fragments, Adapters, XML layouts

**"What is ViewModel?"**
→ `HikeViewModel.kt`, `ObservationViewModel.kt` - Manages UI data, survives configuration changes, uses LiveData for reactive updates

**"What is Repository?"**
→ `HikeRepository.kt`, `ObservationRepository.kt` - Abstracts data source, wraps DAO operations

**"What libraries did you use?"**
→ Room (database), Coroutines (async), LiveData (reactive), Navigation (fragments), Material 3 (UI), OSMDroid (maps)

---

## 📱 KEY FRAGMENTS

| Fragment | Purpose | File |
|----------|---------|------|
| HikeListFragment | Display all hikes, search | HikeListFragment.kt |
| HikeFormFragment | Add/edit hike | HikeFormFragment.kt |
| HikeConfirmFragment | Confirm before saving | HikeConfirmFragment.kt |
| HikeDetailFragment | View hike details | HikeDetailFragment.kt |
| MapPickerFragment | Pick location on map | MapPickerFragment.kt |
| ObservationFormFragment | Add/edit observation | ObservationFormFragment.kt |
| AdvancedSearchBottomSheet | Advanced filters | AdvancedSearchBottomSheet.kt |

---

## 🗄️ DATABASE TABLES

### Table: hikes
- **id** (PK, auto-increment)
- name, location, date, parkingAvailable, lengthKm, difficulty
- description, elevationGainM, rating, photoUri, latitude, longitude, addedToCalendar

### Table: observations
- **id** (PK, auto-increment)
- **hikeId** (FK → hikes.id, CASCADE DELETE)
- observation, timestamp, comments, photoUri

---

## 💡 IMPORTANT CONCEPTS

**Room ORM**: Database library that converts Kotlin objects to SQL automatically

**Flow**: Reactive stream that emits data changes automatically

**LiveData**: Observable data holder that notifies UI when data changes

**Coroutines**: Kotlin's async/await for background operations (IO vs Main dispatcher)

**ViewBinding**: Type-safe view access (`binding.etName` instead of `findViewById`)

**Navigation Component**: Fragment navigation with type-safe arguments

**Material 3**: Latest Android design system (chips, bottom sheets, etc.)

---

## 🎤 SAMPLE ANSWERS

**"Walk me through creating a hike"**
→ User clicks FAB → `HikeListFragment` navigates to `HikeFormFragment` → user fills form → clicks Continue → navigates to `HikeConfirmFragment` → user reviews → clicks Save → `hikeVm.insert()` → `HikeDao.insert()` → stored in SQLite → returns to list with new hike visible

**"How do you prevent data loss?"**
→ 
1. Confirmation dialogs before discarding changes
2. Confirmation screen before saving (review data)
3. ViewModel survives rotation
4. Draft state preserved when navigating
5. Foreign key CASCADE for related data

**"Show me the cascade delete"**
→ `Entities.kt` line ~70: `ForeignKey(entity = Hike::class, onDelete = ForeignKey.CASCADE)` - when hike deleted, Room automatically deletes all observations with that hikeId

---

## 📊 TESTING

**Test file**: `HikeDaoInstrumentedTest.kt` - Database integration tests
**Unit test**: `HikeRepositoryTest.kt` - Repository logic tests

---

## 🌐 INTERNATIONALIZATION

Files: `values/strings.xml`, `values-es/strings.xml`, `values-fr/strings.xml`
All UI text externalized for multiple languages

---

## ✅ REQUIREMENTS MET

✅ Feature A: Enter hike details with validation (10%)
✅ Feature B: Store in SQLite, view, edit, delete, reset (15%)
✅ Feature C: Multiple observations per hike (15%)
✅ Feature D: Simple + advanced search (10%)
✅ Feature G: Photos, GPS/Maps, Calendar, JSON, i18n (10%)

**Total: 60% + extensive additional features**

