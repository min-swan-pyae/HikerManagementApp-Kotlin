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
public class ObservationDao_Impl(
  __db: RoomDatabase,
) : ObservationDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfObservation: EntityInsertionAdapter<Observation>

  private val __deletionAdapterOfObservation: EntityDeletionOrUpdateAdapter<Observation>

  private val __updateAdapterOfObservation: EntityDeletionOrUpdateAdapter<Observation>

  private val __preparedStmtOfDeleteByHike: SharedSQLiteStatement

  private val __preparedStmtOfDeleteAll: SharedSQLiteStatement
  init {
    this.__db = __db
    this.__insertionAdapterOfObservation = object : EntityInsertionAdapter<Observation>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `observations` (`id`,`hikeId`,`observation`,`timestamp`,`comments`,`photoUri`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: Observation) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.hikeId)
        statement.bindString(3, entity.observation)
        statement.bindLong(4, entity.timestamp)
        val _tmpComments: String? = entity.comments
        if (_tmpComments == null) {
          statement.bindNull(5)
        } else {
          statement.bindString(5, _tmpComments)
        }
        val _tmpPhotoUri: String? = entity.photoUri
        if (_tmpPhotoUri == null) {
          statement.bindNull(6)
        } else {
          statement.bindString(6, _tmpPhotoUri)
        }
      }
    }
    this.__deletionAdapterOfObservation = object : EntityDeletionOrUpdateAdapter<Observation>(__db)
        {
      protected override fun createQuery(): String = "DELETE FROM `observations` WHERE `id` = ?"

      protected override fun bind(statement: SupportSQLiteStatement, entity: Observation) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfObservation = object : EntityDeletionOrUpdateAdapter<Observation>(__db) {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `observations` SET `id` = ?,`hikeId` = ?,`observation` = ?,`timestamp` = ?,`comments` = ?,`photoUri` = ? WHERE `id` = ?"

      protected override fun bind(statement: SupportSQLiteStatement, entity: Observation) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.hikeId)
        statement.bindString(3, entity.observation)
        statement.bindLong(4, entity.timestamp)
        val _tmpComments: String? = entity.comments
        if (_tmpComments == null) {
          statement.bindNull(5)
        } else {
          statement.bindString(5, _tmpComments)
        }
        val _tmpPhotoUri: String? = entity.photoUri
        if (_tmpPhotoUri == null) {
          statement.bindNull(6)
        } else {
          statement.bindString(6, _tmpPhotoUri)
        }
        statement.bindLong(7, entity.id)
      }
    }
    this.__preparedStmtOfDeleteByHike = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "DELETE FROM observations WHERE hikeId = ?"
        return _query
      }
    }
    this.__preparedStmtOfDeleteAll = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "DELETE FROM observations"
        return _query
      }
    }
  }

  public override suspend fun insert(obs: Observation): Long = CoroutinesRoom.execute(__db, true,
      object : Callable<Long> {
    public override fun call(): Long {
      __db.beginTransaction()
      try {
        val _result: Long = __insertionAdapterOfObservation.insertAndReturnId(obs)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun delete(obs: Observation): Unit = CoroutinesRoom.execute(__db, true,
      object : Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __deletionAdapterOfObservation.handle(obs)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun update(obs: Observation): Unit = CoroutinesRoom.execute(__db, true,
      object : Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __updateAdapterOfObservation.handle(obs)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun deleteByHike(hikeId: Long): Unit = CoroutinesRoom.execute(__db, true,
      object : Callable<Unit> {
    public override fun call() {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfDeleteByHike.acquire()
      var _argIndex: Int = 1
      _stmt.bindLong(_argIndex, hikeId)
      try {
        __db.beginTransaction()
        try {
          _stmt.executeUpdateDelete()
          __db.setTransactionSuccessful()
        } finally {
          __db.endTransaction()
        }
      } finally {
        __preparedStmtOfDeleteByHike.release(_stmt)
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

  public override fun observeByHike(hikeId: Long): Flow<List<Observation>> {
    val _sql: String = "SELECT * FROM observations WHERE hikeId = ? ORDER BY timestamp DESC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, hikeId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("observations"), object :
        Callable<List<Observation>> {
      public override fun call(): List<Observation> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfHikeId: Int = getColumnIndexOrThrow(_cursor, "hikeId")
          val _cursorIndexOfObservation: Int = getColumnIndexOrThrow(_cursor, "observation")
          val _cursorIndexOfTimestamp: Int = getColumnIndexOrThrow(_cursor, "timestamp")
          val _cursorIndexOfComments: Int = getColumnIndexOrThrow(_cursor, "comments")
          val _cursorIndexOfPhotoUri: Int = getColumnIndexOrThrow(_cursor, "photoUri")
          val _result: MutableList<Observation> = ArrayList<Observation>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: Observation
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpHikeId: Long
            _tmpHikeId = _cursor.getLong(_cursorIndexOfHikeId)
            val _tmpObservation: String
            _tmpObservation = _cursor.getString(_cursorIndexOfObservation)
            val _tmpTimestamp: Long
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp)
            val _tmpComments: String?
            if (_cursor.isNull(_cursorIndexOfComments)) {
              _tmpComments = null
            } else {
              _tmpComments = _cursor.getString(_cursorIndexOfComments)
            }
            val _tmpPhotoUri: String?
            if (_cursor.isNull(_cursorIndexOfPhotoUri)) {
              _tmpPhotoUri = null
            } else {
              _tmpPhotoUri = _cursor.getString(_cursorIndexOfPhotoUri)
            }
            _item =
                Observation(_tmpId,_tmpHikeId,_tmpObservation,_tmpTimestamp,_tmpComments,_tmpPhotoUri)
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

  public override suspend fun getById(id: Long): Observation? {
    val _sql: String = "SELECT * FROM observations WHERE id = ?"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, id)
    val _cancellationSignal: CancellationSignal? = createCancellationSignal()
    return execute(__db, false, _cancellationSignal, object : Callable<Observation?> {
      public override fun call(): Observation? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfHikeId: Int = getColumnIndexOrThrow(_cursor, "hikeId")
          val _cursorIndexOfObservation: Int = getColumnIndexOrThrow(_cursor, "observation")
          val _cursorIndexOfTimestamp: Int = getColumnIndexOrThrow(_cursor, "timestamp")
          val _cursorIndexOfComments: Int = getColumnIndexOrThrow(_cursor, "comments")
          val _cursorIndexOfPhotoUri: Int = getColumnIndexOrThrow(_cursor, "photoUri")
          val _result: Observation?
          if (_cursor.moveToFirst()) {
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpHikeId: Long
            _tmpHikeId = _cursor.getLong(_cursorIndexOfHikeId)
            val _tmpObservation: String
            _tmpObservation = _cursor.getString(_cursorIndexOfObservation)
            val _tmpTimestamp: Long
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp)
            val _tmpComments: String?
            if (_cursor.isNull(_cursorIndexOfComments)) {
              _tmpComments = null
            } else {
              _tmpComments = _cursor.getString(_cursorIndexOfComments)
            }
            val _tmpPhotoUri: String?
            if (_cursor.isNull(_cursorIndexOfPhotoUri)) {
              _tmpPhotoUri = null
            } else {
              _tmpPhotoUri = _cursor.getString(_cursorIndexOfPhotoUri)
            }
            _result =
                Observation(_tmpId,_tmpHikeId,_tmpObservation,_tmpTimestamp,_tmpComments,_tmpPhotoUri)
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
