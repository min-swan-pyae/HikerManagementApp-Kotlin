package com.example.hikermanagementapp.`data`

import android.database.Cursor
import android.os.CancellationSignal
import androidx.room.CoroutinesRoom
import androidx.room.CoroutinesRoom.Companion.execute
import androidx.room.EntityDeletionOrUpdateAdapter
import androidx.room.EntityInsertionAdapter
import androidx.room.RoomDatabase
import androidx.room.RoomSQLiteQuery
import androidx.room.RoomSQLiteQuery.Companion.acquire
import androidx.room.SharedSQLiteStatement
import androidx.room.util.createCancellationSignal
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.query
import androidx.sqlite.db.SupportSQLiteStatement
import java.lang.Class
import java.util.ArrayList
import java.util.concurrent.Callable
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.jvm.JvmStatic
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION"])
public class HikeDao_Impl(
  __db: RoomDatabase,
) : HikeDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfHike: EntityInsertionAdapter<Hike>

  private val __deletionAdapterOfHike: EntityDeletionOrUpdateAdapter<Hike>

  private val __updateAdapterOfHike: EntityDeletionOrUpdateAdapter<Hike>

  private val __preparedStmtOfDeleteAll: SharedSQLiteStatement
  init {
    this.__db = __db
    this.__insertionAdapterOfHike = object : EntityInsertionAdapter<Hike>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `hikes` (`id`,`name`,`location`,`date`,`parkingAvailable`,`lengthKm`,`difficulty`,`description`,`elevationGainM`,`rating`,`photoUri`,`latitude`,`longitude`,`addedToCalendar`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: Hike) {
        statement.bindLong(1, entity.id)
        statement.bindString(2, entity.name)
        statement.bindString(3, entity.location)
        statement.bindString(4, entity.date)
        val _tmp: Int = if (entity.parkingAvailable) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        statement.bindDouble(6, entity.lengthKm)
        statement.bindString(7, entity.difficulty)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(8)
        } else {
          statement.bindString(8, _tmpDescription)
        }
        val _tmpElevationGainM: Int? = entity.elevationGainM
        if (_tmpElevationGainM == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpElevationGainM.toLong())
        }
        val _tmpRating: Float? = entity.rating
        if (_tmpRating == null) {
          statement.bindNull(10)
        } else {
          statement.bindDouble(10, _tmpRating.toDouble())
        }
        val _tmpPhotoUri: String? = entity.photoUri
        if (_tmpPhotoUri == null) {
          statement.bindNull(11)
        } else {
          statement.bindString(11, _tmpPhotoUri)
        }
        val _tmpLatitude: Double? = entity.latitude
        if (_tmpLatitude == null) {
          statement.bindNull(12)
        } else {
          statement.bindDouble(12, _tmpLatitude)
        }
        val _tmpLongitude: Double? = entity.longitude
        if (_tmpLongitude == null) {
          statement.bindNull(13)
        } else {
          statement.bindDouble(13, _tmpLongitude)
        }
        val _tmp_1: Int = if (entity.addedToCalendar) 1 else 0
        statement.bindLong(14, _tmp_1.toLong())
      }
    }
    this.__deletionAdapterOfHike = object : EntityDeletionOrUpdateAdapter<Hike>(__db) {
      protected override fun createQuery(): String = "DELETE FROM `hikes` WHERE `id` = ?"

      protected override fun bind(statement: SupportSQLiteStatement, entity: Hike) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfHike = object : EntityDeletionOrUpdateAdapter<Hike>(__db) {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `hikes` SET `id` = ?,`name` = ?,`location` = ?,`date` = ?,`parkingAvailable` = ?,`lengthKm` = ?,`difficulty` = ?,`description` = ?,`elevationGainM` = ?,`rating` = ?,`photoUri` = ?,`latitude` = ?,`longitude` = ?,`addedToCalendar` = ? WHERE `id` = ?"

      protected override fun bind(statement: SupportSQLiteStatement, entity: Hike) {
        statement.bindLong(1, entity.id)
        statement.bindString(2, entity.name)
        statement.bindString(3, entity.location)
        statement.bindString(4, entity.date)
        val _tmp: Int = if (entity.parkingAvailable) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        statement.bindDouble(6, entity.lengthKm)
        statement.bindString(7, entity.difficulty)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(8)
        } else {
          statement.bindString(8, _tmpDescription)
        }
        val _tmpElevationGainM: Int? = entity.elevationGainM
        if (_tmpElevationGainM == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpElevationGainM.toLong())
        }
        val _tmpRating: Float? = entity.rating
        if (_tmpRating == null) {
          statement.bindNull(10)
        } else {
          statement.bindDouble(10, _tmpRating.toDouble())
        }
        val _tmpPhotoUri: String? = entity.photoUri
        if (_tmpPhotoUri == null) {
          statement.bindNull(11)
        } else {
          statement.bindString(11, _tmpPhotoUri)
        }
        val _tmpLatitude: Double? = entity.latitude
        if (_tmpLatitude == null) {
          statement.bindNull(12)
        } else {
          statement.bindDouble(12, _tmpLatitude)
        }
        val _tmpLongitude: Double? = entity.longitude
        if (_tmpLongitude == null) {
          statement.bindNull(13)
        } else {
          statement.bindDouble(13, _tmpLongitude)
        }
        val _tmp_1: Int = if (entity.addedToCalendar) 1 else 0
        statement.bindLong(14, _tmp_1.toLong())
        statement.bindLong(15, entity.id)
      }
    }
    this.__preparedStmtOfDeleteAll = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "DELETE FROM hikes"
        return _query
      }
    }
  }

  public override suspend fun insert(hike: Hike): Long = CoroutinesRoom.execute(__db, true, object :
      Callable<Long> {
    public override fun call(): Long {
      __db.beginTransaction()
      try {
        val _result: Long = __insertionAdapterOfHike.insertAndReturnId(hike)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun delete(hike: Hike): Unit = CoroutinesRoom.execute(__db, true, object :
      Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __deletionAdapterOfHike.handle(hike)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun update(hike: Hike): Unit = CoroutinesRoom.execute(__db, true, object :
      Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __updateAdapterOfHike.handle(hike)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun deleteAll(): Unit = CoroutinesRoom.execute(__db, true, object :
      Callable<Unit> {
    public override fun call() {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfDeleteAll.acquire()
      try {
        __db.beginTransaction()
        try {
          _stmt.executeUpdateDelete()
          __db.setTransactionSuccessful()
        } finally {
          __db.endTransaction()
        }
      } finally {
        __preparedStmtOfDeleteAll.release(_stmt)
      }
    }
  })

  public override fun observeAll(): Flow<List<Hike>> {
    val _sql: String = "SELECT * FROM hikes ORDER BY date DESC, name ASC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("hikes"), object : Callable<List<Hike>> {
      public override fun call(): List<Hike> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfLocation: Int = getColumnIndexOrThrow(_cursor, "location")
          val _cursorIndexOfDate: Int = getColumnIndexOrThrow(_cursor, "date")
          val _cursorIndexOfParkingAvailable: Int = getColumnIndexOrThrow(_cursor,
              "parkingAvailable")
          val _cursorIndexOfLengthKm: Int = getColumnIndexOrThrow(_cursor, "lengthKm")
          val _cursorIndexOfDifficulty: Int = getColumnIndexOrThrow(_cursor, "difficulty")
          val _cursorIndexOfDescription: Int = getColumnIndexOrThrow(_cursor, "description")
          val _cursorIndexOfElevationGainM: Int = getColumnIndexOrThrow(_cursor, "elevationGainM")
          val _cursorIndexOfRating: Int = getColumnIndexOrThrow(_cursor, "rating")
          val _cursorIndexOfPhotoUri: Int = getColumnIndexOrThrow(_cursor, "photoUri")
          val _cursorIndexOfLatitude: Int = getColumnIndexOrThrow(_cursor, "latitude")
          val _cursorIndexOfLongitude: Int = getColumnIndexOrThrow(_cursor, "longitude")
          val _cursorIndexOfAddedToCalendar: Int = getColumnIndexOrThrow(_cursor, "addedToCalendar")
          val _result: MutableList<Hike> = ArrayList<Hike>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: Hike
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpLocation: String
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation)
            val _tmpDate: String
            _tmpDate = _cursor.getString(_cursorIndexOfDate)
            val _tmpParkingAvailable: Boolean
            val _tmp: Int
            _tmp = _cursor.getInt(_cursorIndexOfParkingAvailable)
            _tmpParkingAvailable = _tmp != 0
            val _tmpLengthKm: Double
            _tmpLengthKm = _cursor.getDouble(_cursorIndexOfLengthKm)
            val _tmpDifficulty: String
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty)
            val _tmpDescription: String?
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription)
            }
            val _tmpElevationGainM: Int?
            if (_cursor.isNull(_cursorIndexOfElevationGainM)) {
              _tmpElevationGainM = null
            } else {
              _tmpElevationGainM = _cursor.getInt(_cursorIndexOfElevationGainM)
            }
            val _tmpRating: Float?
            if (_cursor.isNull(_cursorIndexOfRating)) {
              _tmpRating = null
            } else {
              _tmpRating = _cursor.getFloat(_cursorIndexOfRating)
            }
            val _tmpPhotoUri: String?
            if (_cursor.isNull(_cursorIndexOfPhotoUri)) {
              _tmpPhotoUri = null
            } else {
              _tmpPhotoUri = _cursor.getString(_cursorIndexOfPhotoUri)
            }
            val _tmpLatitude: Double?
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude)
            }
            val _tmpLongitude: Double?
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude)
            }
            val _tmpAddedToCalendar: Boolean
            val _tmp_1: Int
            _tmp_1 = _cursor.getInt(_cursorIndexOfAddedToCalendar)
            _tmpAddedToCalendar = _tmp_1 != 0
            _item =
                Hike(_tmpId,_tmpName,_tmpLocation,_tmpDate,_tmpParkingAvailable,_tmpLengthKm,_tmpDifficulty,_tmpDescription,_tmpElevationGainM,_tmpRating,_tmpPhotoUri,_tmpLatitude,_tmpLongitude,_tmpAddedToCalendar)
            _result.add(_item)
          }
          return _result
        } finally {
          _cursor.close()
        }
      }

      protected fun finalize() {
        _statement.release()
      }
    })
  }

  public override suspend fun getById(id: Long): Hike? {
    val _sql: String = "SELECT * FROM hikes WHERE id = ?"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, id)
    val _cancellationSignal: CancellationSignal? = createCancellationSignal()
    return execute(__db, false, _cancellationSignal, object : Callable<Hike?> {
      public override fun call(): Hike? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfLocation: Int = getColumnIndexOrThrow(_cursor, "location")
          val _cursorIndexOfDate: Int = getColumnIndexOrThrow(_cursor, "date")
          val _cursorIndexOfParkingAvailable: Int = getColumnIndexOrThrow(_cursor,
              "parkingAvailable")
          val _cursorIndexOfLengthKm: Int = getColumnIndexOrThrow(_cursor, "lengthKm")
          val _cursorIndexOfDifficulty: Int = getColumnIndexOrThrow(_cursor, "difficulty")
          val _cursorIndexOfDescription: Int = getColumnIndexOrThrow(_cursor, "description")
          val _cursorIndexOfElevationGainM: Int = getColumnIndexOrThrow(_cursor, "elevationGainM")
          val _cursorIndexOfRating: Int = getColumnIndexOrThrow(_cursor, "rating")
          val _cursorIndexOfPhotoUri: Int = getColumnIndexOrThrow(_cursor, "photoUri")
          val _cursorIndexOfLatitude: Int = getColumnIndexOrThrow(_cursor, "latitude")
          val _cursorIndexOfLongitude: Int = getColumnIndexOrThrow(_cursor, "longitude")
          val _cursorIndexOfAddedToCalendar: Int = getColumnIndexOrThrow(_cursor, "addedToCalendar")
          val _result: Hike?
          if (_cursor.moveToFirst()) {
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpLocation: String
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation)
            val _tmpDate: String
            _tmpDate = _cursor.getString(_cursorIndexOfDate)
            val _tmpParkingAvailable: Boolean
            val _tmp: Int
            _tmp = _cursor.getInt(_cursorIndexOfParkingAvailable)
            _tmpParkingAvailable = _tmp != 0
            val _tmpLengthKm: Double
            _tmpLengthKm = _cursor.getDouble(_cursorIndexOfLengthKm)
            val _tmpDifficulty: String
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty)
            val _tmpDescription: String?
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription)
            }
            val _tmpElevationGainM: Int?
            if (_cursor.isNull(_cursorIndexOfElevationGainM)) {
              _tmpElevationGainM = null
            } else {
              _tmpElevationGainM = _cursor.getInt(_cursorIndexOfElevationGainM)
            }
            val _tmpRating: Float?
            if (_cursor.isNull(_cursorIndexOfRating)) {
              _tmpRating = null
            } else {
              _tmpRating = _cursor.getFloat(_cursorIndexOfRating)
            }
            val _tmpPhotoUri: String?
            if (_cursor.isNull(_cursorIndexOfPhotoUri)) {
              _tmpPhotoUri = null
            } else {
              _tmpPhotoUri = _cursor.getString(_cursorIndexOfPhotoUri)
            }
            val _tmpLatitude: Double?
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude)
            }
            val _tmpLongitude: Double?
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude)
            }
            val _tmpAddedToCalendar: Boolean
            val _tmp_1: Int
            _tmp_1 = _cursor.getInt(_cursorIndexOfAddedToCalendar)
            _tmpAddedToCalendar = _tmp_1 != 0
            _result =
                Hike(_tmpId,_tmpName,_tmpLocation,_tmpDate,_tmpParkingAvailable,_tmpLengthKm,_tmpDifficulty,_tmpDescription,_tmpElevationGainM,_tmpRating,_tmpPhotoUri,_tmpLatitude,_tmpLongitude,_tmpAddedToCalendar)
          } else {
            _result = null
          }
          return _result
        } finally {
          _cursor.close()
          _statement.release()
        }
      }
    })
  }

  public override fun searchByNameContains(query: String): Flow<List<Hike>> {
    val _sql: String = "SELECT * FROM hikes WHERE name LIKE '%' || ? || '%' ORDER BY name ASC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindString(_argIndex, query)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("hikes"), object : Callable<List<Hike>> {
      public override fun call(): List<Hike> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfLocation: Int = getColumnIndexOrThrow(_cursor, "location")
          val _cursorIndexOfDate: Int = getColumnIndexOrThrow(_cursor, "date")
          val _cursorIndexOfParkingAvailable: Int = getColumnIndexOrThrow(_cursor,
              "parkingAvailable")
          val _cursorIndexOfLengthKm: Int = getColumnIndexOrThrow(_cursor, "lengthKm")
          val _cursorIndexOfDifficulty: Int = getColumnIndexOrThrow(_cursor, "difficulty")
          val _cursorIndexOfDescription: Int = getColumnIndexOrThrow(_cursor, "description")
          val _cursorIndexOfElevationGainM: Int = getColumnIndexOrThrow(_cursor, "elevationGainM")
          val _cursorIndexOfRating: Int = getColumnIndexOrThrow(_cursor, "rating")
          val _cursorIndexOfPhotoUri: Int = getColumnIndexOrThrow(_cursor, "photoUri")
          val _cursorIndexOfLatitude: Int = getColumnIndexOrThrow(_cursor, "latitude")
          val _cursorIndexOfLongitude: Int = getColumnIndexOrThrow(_cursor, "longitude")
          val _cursorIndexOfAddedToCalendar: Int = getColumnIndexOrThrow(_cursor, "addedToCalendar")
          val _result: MutableList<Hike> = ArrayList<Hike>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: Hike
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpLocation: String
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation)
            val _tmpDate: String
            _tmpDate = _cursor.getString(_cursorIndexOfDate)
            val _tmpParkingAvailable: Boolean
            val _tmp: Int
            _tmp = _cursor.getInt(_cursorIndexOfParkingAvailable)
            _tmpParkingAvailable = _tmp != 0
            val _tmpLengthKm: Double
            _tmpLengthKm = _cursor.getDouble(_cursorIndexOfLengthKm)
            val _tmpDifficulty: String
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty)
            val _tmpDescription: String?
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription)
            }
            val _tmpElevationGainM: Int?
            if (_cursor.isNull(_cursorIndexOfElevationGainM)) {
              _tmpElevationGainM = null
            } else {
              _tmpElevationGainM = _cursor.getInt(_cursorIndexOfElevationGainM)
            }
            val _tmpRating: Float?
            if (_cursor.isNull(_cursorIndexOfRating)) {
              _tmpRating = null
            } else {
              _tmpRating = _cursor.getFloat(_cursorIndexOfRating)
            }
            val _tmpPhotoUri: String?
            if (_cursor.isNull(_cursorIndexOfPhotoUri)) {
              _tmpPhotoUri = null
            } else {
              _tmpPhotoUri = _cursor.getString(_cursorIndexOfPhotoUri)
            }
            val _tmpLatitude: Double?
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude)
            }
            val _tmpLongitude: Double?
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude)
            }
            val _tmpAddedToCalendar: Boolean
            val _tmp_1: Int
            _tmp_1 = _cursor.getInt(_cursorIndexOfAddedToCalendar)
            _tmpAddedToCalendar = _tmp_1 != 0
            _item =
                Hike(_tmpId,_tmpName,_tmpLocation,_tmpDate,_tmpParkingAvailable,_tmpLengthKm,_tmpDifficulty,_tmpDescription,_tmpElevationGainM,_tmpRating,_tmpPhotoUri,_tmpLatitude,_tmpLongitude,_tmpAddedToCalendar)
            _result.add(_item)
          }
          return _result
        } finally {
          _cursor.close()
        }
      }

      protected fun finalize() {
        _statement.release()
      }
    })
  }

  public override fun advancedSearch(
    name: String?,
    location: String?,
    minLen: Double?,
    maxLen: Double?,
    date: String?,
    difficulty: String?,
    parking: Boolean?,
  ): Flow<List<Hike>> {
    val _sql: String =
        "SELECT * FROM hikes WHERE (? IS NULL OR name LIKE '%' || ? || '%') AND (? IS NULL OR location LIKE '%' || ? || '%') AND (? IS NULL OR lengthKm >= ?) AND (? IS NULL OR lengthKm <= ?) AND (? IS NULL OR date = ?) AND (? IS NULL OR difficulty = ?) AND (? IS NULL OR parkingAvailable = ?) ORDER BY date DESC, name ASC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 14)
    var _argIndex: Int = 1
    if (name == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindString(_argIndex, name)
    }
    _argIndex = 2
    if (name == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindString(_argIndex, name)
    }
    _argIndex = 3
    if (location == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindString(_argIndex, location)
    }
    _argIndex = 4
    if (location == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindString(_argIndex, location)
    }
    _argIndex = 5
    if (minLen == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindDouble(_argIndex, minLen)
    }
    _argIndex = 6
    if (minLen == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindDouble(_argIndex, minLen)
    }
    _argIndex = 7
    if (maxLen == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindDouble(_argIndex, maxLen)
    }
    _argIndex = 8
    if (maxLen == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindDouble(_argIndex, maxLen)
    }
    _argIndex = 9
    if (date == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindString(_argIndex, date)
    }
    _argIndex = 10
    if (date == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindString(_argIndex, date)
    }
    _argIndex = 11
    if (difficulty == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindString(_argIndex, difficulty)
    }
    _argIndex = 12
    if (difficulty == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindString(_argIndex, difficulty)
    }
    _argIndex = 13
    val _tmp: Int? = parking?.let { if (it) 1 else 0 }
    if (_tmp == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindLong(_argIndex, _tmp.toLong())
    }
    _argIndex = 14
    val _tmp_1: Int? = parking?.let { if (it) 1 else 0 }
    if (_tmp_1 == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindLong(_argIndex, _tmp_1.toLong())
    }
    return CoroutinesRoom.createFlow(__db, false, arrayOf("hikes"), object : Callable<List<Hike>> {
      public override fun call(): List<Hike> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfLocation: Int = getColumnIndexOrThrow(_cursor, "location")
          val _cursorIndexOfDate: Int = getColumnIndexOrThrow(_cursor, "date")
          val _cursorIndexOfParkingAvailable: Int = getColumnIndexOrThrow(_cursor,
              "parkingAvailable")
          val _cursorIndexOfLengthKm: Int = getColumnIndexOrThrow(_cursor, "lengthKm")
          val _cursorIndexOfDifficulty: Int = getColumnIndexOrThrow(_cursor, "difficulty")
          val _cursorIndexOfDescription: Int = getColumnIndexOrThrow(_cursor, "description")
          val _cursorIndexOfElevationGainM: Int = getColumnIndexOrThrow(_cursor, "elevationGainM")
          val _cursorIndexOfRating: Int = getColumnIndexOrThrow(_cursor, "rating")
          val _cursorIndexOfPhotoUri: Int = getColumnIndexOrThrow(_cursor, "photoUri")
          val _cursorIndexOfLatitude: Int = getColumnIndexOrThrow(_cursor, "latitude")
          val _cursorIndexOfLongitude: Int = getColumnIndexOrThrow(_cursor, "longitude")
          val _cursorIndexOfAddedToCalendar: Int = getColumnIndexOrThrow(_cursor, "addedToCalendar")
          val _result: MutableList<Hike> = ArrayList<Hike>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: Hike
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpLocation: String
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation)
            val _tmpDate: String
            _tmpDate = _cursor.getString(_cursorIndexOfDate)
            val _tmpParkingAvailable: Boolean
            val _tmp_2: Int
            _tmp_2 = _cursor.getInt(_cursorIndexOfParkingAvailable)
            _tmpParkingAvailable = _tmp_2 != 0
            val _tmpLengthKm: Double
            _tmpLengthKm = _cursor.getDouble(_cursorIndexOfLengthKm)
            val _tmpDifficulty: String
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty)
            val _tmpDescription: String?
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription)
            }
            val _tmpElevationGainM: Int?
            if (_cursor.isNull(_cursorIndexOfElevationGainM)) {
              _tmpElevationGainM = null
            } else {
              _tmpElevationGainM = _cursor.getInt(_cursorIndexOfElevationGainM)
            }
            val _tmpRating: Float?
            if (_cursor.isNull(_cursorIndexOfRating)) {
              _tmpRating = null
            } else {
              _tmpRating = _cursor.getFloat(_cursorIndexOfRating)
            }
            val _tmpPhotoUri: String?
            if (_cursor.isNull(_cursorIndexOfPhotoUri)) {
              _tmpPhotoUri = null
            } else {
              _tmpPhotoUri = _cursor.getString(_cursorIndexOfPhotoUri)
            }
            val _tmpLatitude: Double?
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude)
            }
            val _tmpLongitude: Double?
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude)
            }
            val _tmpAddedToCalendar: Boolean
            val _tmp_3: Int
            _tmp_3 = _cursor.getInt(_cursorIndexOfAddedToCalendar)
            _tmpAddedToCalendar = _tmp_3 != 0
            _item =
                Hike(_tmpId,_tmpName,_tmpLocation,_tmpDate,_tmpParkingAvailable,_tmpLengthKm,_tmpDifficulty,_tmpDescription,_tmpElevationGainM,_tmpRating,_tmpPhotoUri,_tmpLatitude,_tmpLongitude,_tmpAddedToCalendar)
            _result.add(_item)
          }
          return _result
        } finally {
          _cursor.close()
        }
      }

      protected fun finalize() {
        _statement.release()
      }
    })
  }

  public override suspend fun findDuplicate(
    name: String,
    location: String,
    date: String,
    lengthKm: Double,
    difficulty: String,
    parkingAvailable: Boolean,
    elevationGainM: Int?,
    latitude: Double?,
    longitude: Double?,
  ): Hike? {
    val _sql: String =
        "SELECT * FROM hikes WHERE name = ? AND location = ? AND date = ? AND lengthKm = ? AND difficulty = ? AND parkingAvailable = ? AND (? IS NULL AND elevationGainM IS NULL OR elevationGainM = ?) AND (? IS NULL AND latitude IS NULL OR latitude = ?) AND (? IS NULL AND longitude IS NULL OR longitude = ?) LIMIT 1"
    val _statement: RoomSQLiteQuery = acquire(_sql, 12)
    var _argIndex: Int = 1
    _statement.bindString(_argIndex, name)
    _argIndex = 2
    _statement.bindString(_argIndex, location)
    _argIndex = 3
    _statement.bindString(_argIndex, date)
    _argIndex = 4
    _statement.bindDouble(_argIndex, lengthKm)
    _argIndex = 5
    _statement.bindString(_argIndex, difficulty)
    _argIndex = 6
    val _tmp: Int = if (parkingAvailable) 1 else 0
    _statement.bindLong(_argIndex, _tmp.toLong())
    _argIndex = 7
    if (elevationGainM == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindLong(_argIndex, elevationGainM.toLong())
    }
    _argIndex = 8
    if (elevationGainM == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindLong(_argIndex, elevationGainM.toLong())
    }
    _argIndex = 9
    if (latitude == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindDouble(_argIndex, latitude)
    }
    _argIndex = 10
    if (latitude == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindDouble(_argIndex, latitude)
    }
    _argIndex = 11
    if (longitude == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindDouble(_argIndex, longitude)
    }
    _argIndex = 12
    if (longitude == null) {
      _statement.bindNull(_argIndex)
    } else {
      _statement.bindDouble(_argIndex, longitude)
    }
    val _cancellationSignal: CancellationSignal? = createCancellationSignal()
    return execute(__db, false, _cancellationSignal, object : Callable<Hike?> {
      public override fun call(): Hike? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfLocation: Int = getColumnIndexOrThrow(_cursor, "location")
          val _cursorIndexOfDate: Int = getColumnIndexOrThrow(_cursor, "date")
          val _cursorIndexOfParkingAvailable: Int = getColumnIndexOrThrow(_cursor,
              "parkingAvailable")
          val _cursorIndexOfLengthKm: Int = getColumnIndexOrThrow(_cursor, "lengthKm")
          val _cursorIndexOfDifficulty: Int = getColumnIndexOrThrow(_cursor, "difficulty")
          val _cursorIndexOfDescription: Int = getColumnIndexOrThrow(_cursor, "description")
          val _cursorIndexOfElevationGainM: Int = getColumnIndexOrThrow(_cursor, "elevationGainM")
          val _cursorIndexOfRating: Int = getColumnIndexOrThrow(_cursor, "rating")
          val _cursorIndexOfPhotoUri: Int = getColumnIndexOrThrow(_cursor, "photoUri")
          val _cursorIndexOfLatitude: Int = getColumnIndexOrThrow(_cursor, "latitude")
          val _cursorIndexOfLongitude: Int = getColumnIndexOrThrow(_cursor, "longitude")
          val _cursorIndexOfAddedToCalendar: Int = getColumnIndexOrThrow(_cursor, "addedToCalendar")
          val _result: Hike?
          if (_cursor.moveToFirst()) {
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpLocation: String
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation)
            val _tmpDate: String
            _tmpDate = _cursor.getString(_cursorIndexOfDate)
            val _tmpParkingAvailable: Boolean
            val _tmp_1: Int
            _tmp_1 = _cursor.getInt(_cursorIndexOfParkingAvailable)
            _tmpParkingAvailable = _tmp_1 != 0
            val _tmpLengthKm: Double
            _tmpLengthKm = _cursor.getDouble(_cursorIndexOfLengthKm)
            val _tmpDifficulty: String
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty)
            val _tmpDescription: String?
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription)
            }
            val _tmpElevationGainM: Int?
            if (_cursor.isNull(_cursorIndexOfElevationGainM)) {
              _tmpElevationGainM = null
            } else {
              _tmpElevationGainM = _cursor.getInt(_cursorIndexOfElevationGainM)
            }
            val _tmpRating: Float?
            if (_cursor.isNull(_cursorIndexOfRating)) {
              _tmpRating = null
            } else {
              _tmpRating = _cursor.getFloat(_cursorIndexOfRating)
            }
            val _tmpPhotoUri: String?
            if (_cursor.isNull(_cursorIndexOfPhotoUri)) {
              _tmpPhotoUri = null
            } else {
              _tmpPhotoUri = _cursor.getString(_cursorIndexOfPhotoUri)
            }
            val _tmpLatitude: Double?
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude)
            }
            val _tmpLongitude: Double?
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude)
            }
            val _tmpAddedToCalendar: Boolean
            val _tmp_2: Int
            _tmp_2 = _cursor.getInt(_cursorIndexOfAddedToCalendar)
            _tmpAddedToCalendar = _tmp_2 != 0
            _result =
                Hike(_tmpId,_tmpName,_tmpLocation,_tmpDate,_tmpParkingAvailable,_tmpLengthKm,_tmpDifficulty,_tmpDescription,_tmpElevationGainM,_tmpRating,_tmpPhotoUri,_tmpLatitude,_tmpLongitude,_tmpAddedToCalendar)
          } else {
            _result = null
          }
          return _result
        } finally {
          _cursor.close()
          _statement.release()
        }
      }
    })
  }

  public companion object {
    @JvmStatic
    public fun getRequiredConverters(): List<Class<*>> = emptyList()
  }
}
