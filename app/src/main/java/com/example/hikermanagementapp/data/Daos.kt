package com.example.hikermanagementapp.data


import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * FEATURE B & D: Data Access Object for Hike operations
 *
 * Defines all database queries for managing hikes.
 * Room generates implementation automatically.
 * Uses Flow for reactive data (automatically updates UI when data changes).
 */
@Dao
interface HikeDao {
    /**
     * FEATURE B: View all hikes
     * Returns Flow that emits new list whenever database changes.
     * Ordered by date (newest first), then by name.
     */
    @Query("SELECT * FROM hikes ORDER BY date DESC, name ASC")
    fun observeAll(): Flow<List<Hike>>

    /**
     * FEATURE B: Get single hike by ID
     * Used for viewing details and editing.
     */
    @Query("SELECT * FROM hikes WHERE id = :id")
    suspend fun getById(id: Long): Hike?

    /**
     * FEATURE B: Insert new hike
     * Returns the auto-generated ID of the inserted hike.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(hike: Hike): Long

    /**
     * FEATURE B: Update existing hike
     * Used when editing hike details.
     */
    @Update
    suspend fun update(hike: Hike)

    /**
     * FEATURE B: Delete single hike
     * Cascade delete will also remove all observations for this hike.
     */
    @Delete
    suspend fun delete(hike: Hike)

    /**
     * FEATURE B: Reset database - delete all hikes
     * "Reset Database"
     */
    @Query("DELETE FROM hikes")
    suspend fun deleteAll()

    /**
     * FEATURE D: Simple search by name
     */
    @Query("SELECT * FROM hikes WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchByNameContains(query: String): Flow<List<Hike>>

    /**
     * FEATURE D: Advanced search with multiple criteria
     * All parameters are optional (nullable).
     * NULL parameters are ignored in the WHERE clause.

     */
    @Query(
        "SELECT * FROM hikes WHERE (:name IS NULL OR name LIKE '%' || :name || '%') " +
            "AND (:location IS NULL OR location LIKE '%' || :location || '%') " +
            "AND (:minLen IS NULL OR lengthKm >= :minLen) " +
            "AND (:maxLen IS NULL OR lengthKm <= :maxLen) " +
            "AND (:date IS NULL OR date = :date) " +
            "AND (:difficulty IS NULL OR difficulty = :difficulty) " +
            "AND (:parking IS NULL OR parkingAvailable = :parking) " +
            "ORDER BY date DESC, name ASC"
    )
    fun advancedSearch(
        name: String?,
        location: String?,
        minLen: Double?,
        maxLen: Double?,
        date: String?,
        difficulty: String?,
        parking: Boolean?
    ): Flow<List<Hike>>

    /**
     * FEATURE G: Duplicate detection
     * Checks if a hike with identical core attributes already exists.
     * Used during import to prevent duplicates.
     */
    @Query(
        "SELECT * FROM hikes WHERE " +
            "name = :name AND " +
            "location = :location AND " +
            "date = :date AND " +
            "lengthKm = :lengthKm AND " +
            "difficulty = :difficulty AND " +
            "parkingAvailable = :parkingAvailable AND " +
            "(:elevationGainM IS NULL AND elevationGainM IS NULL OR elevationGainM = :elevationGainM) AND " +
            "(:latitude IS NULL AND latitude IS NULL OR latitude = :latitude) AND " +
            "(:longitude IS NULL AND longitude IS NULL OR longitude = :longitude) " +
            "LIMIT 1"
    )
    suspend fun findDuplicate(
        name: String,
        location: String,
        date: String,
        lengthKm: Double,
        difficulty: String,
        parkingAvailable: Boolean,
        elevationGainM: Int?,
        latitude: Double?,
        longitude: Double?
    ): Hike?
}

/**
 * FEATURE C: Data Access Object for Observation operations
 *
 * Manages all database queries for observations linked to hikes.
 */
@Dao
interface ObservationDao {
    /**
     * FEATURE C: Get all observations for a specific hike
     * Returns Flow for reactive updates.
     * Ordered by timestamp (newest first).
     */
    @Query("SELECT * FROM observations WHERE hikeId = :hikeId ORDER BY timestamp DESC")
    fun observeByHike(hikeId: Long): Flow<List<Observation>>

    /**
     * FEATURE C: Get single observation by ID
     * Used for editing observations.
     */
    @Query("SELECT * FROM observations WHERE id = :id")
    suspend fun getById(id: Long): Observation?

    /**
     * FEATURE C: Insert new observation
     * Returns auto-generated ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(obs: Observation): Long

    /**
     * FEATURE C: Update existing observation
     */
    @Update
    suspend fun update(obs: Observation)

    /**
     * FEATURE C: Delete single observation
     */
    @Delete
    suspend fun delete(obs: Observation)

    /**
     * FEATURE C: Delete all observations for a specific hike
     * Used when deleting a hike manually.
     */
    @Query("DELETE FROM observations WHERE hikeId = :hikeId")
    suspend fun deleteByHike(hikeId: Long)

    /**
     * FEATURE B: Delete all observations (part of reset database)
     */
    @Query("DELETE FROM observations")
    suspend fun deleteAll()
}
