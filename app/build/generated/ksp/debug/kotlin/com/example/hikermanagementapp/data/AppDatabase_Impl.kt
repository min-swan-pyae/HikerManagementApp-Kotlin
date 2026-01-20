package com.example.hikermanagementapp.`data`

import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import androidx.room.RoomOpenHelper
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import java.lang.Class
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import javax.`annotation`.processing.Generated
import kotlin.Any
import kotlin.Boolean
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.Set

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION"])
public class AppDatabase_Impl : AppDatabase() {
  private val _hikeDao: Lazy<HikeDao> = lazy {
    HikeDao_Impl(this)
  }


  private val _observationDao: Lazy<ObservationDao> = lazy {
    ObservationDao_Impl(this)
  }


  protected override fun createOpenHelper(config: DatabaseConfiguration): SupportSQLiteOpenHelper {
    val _openCallback: SupportSQLiteOpenHelper.Callback = RoomOpenHelper(config, object :
        RoomOpenHelper.Delegate(4) {
      public override fun createAllTables(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `hikes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `location` TEXT NOT NULL, `date` TEXT NOT NULL, `parkingAvailable` INTEGER NOT NULL, `lengthKm` REAL NOT NULL, `difficulty` TEXT NOT NULL, `description` TEXT, `elevationGainM` INTEGER, `rating` REAL, `photoUri` TEXT, `latitude` REAL, `longitude` REAL, `addedToCalendar` INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_hikes_name` ON `hikes` (`name`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `observations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `hikeId` INTEGER NOT NULL, `observation` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `comments` TEXT, `photoUri` TEXT, FOREIGN KEY(`hikeId`) REFERENCES `hikes`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_observations_hikeId` ON `observations` (`hikeId`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '3fb70e25b25742924de2c6802a47c77d')")
      }

      public override fun dropAllTables(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `hikes`")
        db.execSQL("DROP TABLE IF EXISTS `observations`")
        val _callbacks: List<RoomDatabase.Callback>? = mCallbacks
        if (_callbacks != null) {
          for (_callback: RoomDatabase.Callback in _callbacks) {
            _callback.onDestructiveMigration(db)
          }
        }
      }

      public override fun onCreate(db: SupportSQLiteDatabase) {
        val _callbacks: List<RoomDatabase.Callback>? = mCallbacks
        if (_callbacks != null) {
          for (_callback: RoomDatabase.Callback in _callbacks) {
            _callback.onCreate(db)
          }
        }
      }

      public override fun onOpen(db: SupportSQLiteDatabase) {
        mDatabase = db
        db.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(db)
        val _callbacks: List<RoomDatabase.Callback>? = mCallbacks
        if (_callbacks != null) {
          for (_callback: RoomDatabase.Callback in _callbacks) {
            _callback.onOpen(db)
          }
        }
      }

      public override fun onPreMigrate(db: SupportSQLiteDatabase) {
        dropFtsSyncTriggers(db)
      }

      public override fun onPostMigrate(db: SupportSQLiteDatabase) {
      }

      public override fun onValidateSchema(db: SupportSQLiteDatabase):
          RoomOpenHelper.ValidationResult {
        val _columnsHikes: HashMap<String, TableInfo.Column> = HashMap<String, TableInfo.Column>(14)
        _columnsHikes.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHikes.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHikes.put("location", TableInfo.Column("location", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHikes.put("date", TableInfo.Column("date", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHikes.put("parkingAvailable", TableInfo.Column("parkingAvailable", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHikes.put("lengthKm", TableInfo.Column("lengthKm", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHikes.put("difficulty", TableInfo.Column("difficulty", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHikes.put("description", TableInfo.Column("description", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHikes.put("elevationGainM", TableInfo.Column("elevationGainM", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHikes.put("rating", TableInfo.Column("rating", "REAL", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHikes.put("photoUri", TableInfo.Column("photoUri", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHikes.put("latitude", TableInfo.Column("latitude", "REAL", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHikes.put("longitude", TableInfo.Column("longitude", "REAL", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHikes.put("addedToCalendar", TableInfo.Column("addedToCalendar", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysHikes: HashSet<TableInfo.ForeignKey> = HashSet<TableInfo.ForeignKey>(0)
        val _indicesHikes: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(1)
        _indicesHikes.add(TableInfo.Index("index_hikes_name", false, listOf("name"), listOf("ASC")))
        val _infoHikes: TableInfo = TableInfo("hikes", _columnsHikes, _foreignKeysHikes,
            _indicesHikes)
        val _existingHikes: TableInfo = read(db, "hikes")
        if (!_infoHikes.equals(_existingHikes)) {
          return RoomOpenHelper.ValidationResult(false, """
              |hikes(com.example.hikermanagementapp.data.Hike).
              | Expected:
              |""".trimMargin() + _infoHikes + """
              |
              | Found:
              |""".trimMargin() + _existingHikes)
        }
        val _columnsObservations: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(6)
        _columnsObservations.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsObservations.put("hikeId", TableInfo.Column("hikeId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsObservations.put("observation", TableInfo.Column("observation", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsObservations.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsObservations.put("comments", TableInfo.Column("comments", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsObservations.put("photoUri", TableInfo.Column("photoUri", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysObservations: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(1)
        _foreignKeysObservations.add(TableInfo.ForeignKey("hikes", "CASCADE", "CASCADE",
            listOf("hikeId"), listOf("id")))
        val _indicesObservations: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(1)
        _indicesObservations.add(TableInfo.Index("index_observations_hikeId", false,
            listOf("hikeId"), listOf("ASC")))
        val _infoObservations: TableInfo = TableInfo("observations", _columnsObservations,
            _foreignKeysObservations, _indicesObservations)
        val _existingObservations: TableInfo = read(db, "observations")
        if (!_infoObservations.equals(_existingObservations)) {
          return RoomOpenHelper.ValidationResult(false, """
              |observations(com.example.hikermanagementapp.data.Observation).
              | Expected:
              |""".trimMargin() + _infoObservations + """
              |
              | Found:
              |""".trimMargin() + _existingObservations)
        }
        return RoomOpenHelper.ValidationResult(true, null)
      }
    }, "3fb70e25b25742924de2c6802a47c77d", "8d2d33e66b0a0add54beb7b9abc35bfc")
    val _sqliteConfig: SupportSQLiteOpenHelper.Configuration =
        SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build()
    val _helper: SupportSQLiteOpenHelper = config.sqliteOpenHelperFactory.create(_sqliteConfig)
    return _helper
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: HashMap<String, String> = HashMap<String, String>(0)
    val _viewTables: HashMap<String, Set<String>> = HashMap<String, Set<String>>(0)
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "hikes","observations")
  }

  public override fun clearAllTables() {
    super.assertNotMainThread()
    val _db: SupportSQLiteDatabase = super.openHelper.writableDatabase
    val _supportsDeferForeignKeys: Boolean = android.os.Build.VERSION.SDK_INT >=
        android.os.Build.VERSION_CODES.LOLLIPOP
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE")
      }
      super.beginTransaction()
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE")
      }
      _db.execSQL("DELETE FROM `hikes`")
      _db.execSQL("DELETE FROM `observations`")
      super.setTransactionSuccessful()
    } finally {
      super.endTransaction()
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE")
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close()
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM")
      }
    }
  }

  protected override fun getRequiredTypeConverters(): Map<Class<out Any>, List<Class<out Any>>> {
    val _typeConvertersMap: HashMap<Class<out Any>, List<Class<out Any>>> =
        HashMap<Class<out Any>, List<Class<out Any>>>()
    _typeConvertersMap.put(HikeDao::class.java, HikeDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ObservationDao::class.java, ObservationDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecs(): Set<Class<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: HashSet<Class<out AutoMigrationSpec>> =
        HashSet<Class<out AutoMigrationSpec>>()
    return _autoMigrationSpecsSet
  }

  public override
      fun getAutoMigrations(autoMigrationSpecs: Map<Class<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = ArrayList<Migration>()
    return _autoMigrations
  }

  public override fun hikeDao(): HikeDao = _hikeDao.value

  public override fun observationDao(): ObservationDao = _observationDao.value
}
