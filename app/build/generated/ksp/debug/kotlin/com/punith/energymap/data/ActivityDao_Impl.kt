package com.punith.energymap.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ActivityDao_Impl(
  __db: RoomDatabase,
) : ActivityDao {
  private val __db: RoomDatabase

  private val __deleteAdapterOfActivityEntry: EntityDeleteOrUpdateAdapter<ActivityEntry>

  private val __upsertAdapterOfActivityEntry: EntityUpsertAdapter<ActivityEntry>
  init {
    this.__db = __db
    this.__deleteAdapterOfActivityEntry = object : EntityDeleteOrUpdateAdapter<ActivityEntry>() {
      protected override fun createQuery(): String = "DELETE FROM `ActivityEntry` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ActivityEntry) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__upsertAdapterOfActivityEntry = EntityUpsertAdapter<ActivityEntry>(object : EntityInsertAdapter<ActivityEntry>() {
      protected override fun createQuery(): String = "INSERT INTO `ActivityEntry` (`id`,`title`,`startTime`,`endTime`,`note`,`isOngoing`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ActivityEntry) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindLong(3, entity.startTime)
        val _tmpEndTime: Long? = entity.endTime
        if (_tmpEndTime == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpEndTime)
        }
        statement.bindText(5, entity.note)
        val _tmp: Int = if (entity.isOngoing) 1 else 0
        statement.bindLong(6, _tmp.toLong())
      }
    }, object : EntityDeleteOrUpdateAdapter<ActivityEntry>() {
      protected override fun createQuery(): String = "UPDATE `ActivityEntry` SET `id` = ?,`title` = ?,`startTime` = ?,`endTime` = ?,`note` = ?,`isOngoing` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ActivityEntry) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindLong(3, entity.startTime)
        val _tmpEndTime: Long? = entity.endTime
        if (_tmpEndTime == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpEndTime)
        }
        statement.bindText(5, entity.note)
        val _tmp: Int = if (entity.isOngoing) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindLong(7, entity.id)
      }
    })
  }

  public override suspend fun delete(entry: ActivityEntry): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfActivityEntry.handle(_connection, entry)
  }

  public override suspend fun upsert(entry: ActivityEntry): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfActivityEntry.upsert(_connection, entry)
  }

  public override fun observeEntries(): Flow<List<ActivityEntry>> {
    val _sql: String = "SELECT * FROM ActivityEntry ORDER BY startTime DESC"
    return createFlow(__db, false, arrayOf("ActivityEntry")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfIsOngoing: Int = getColumnIndexOrThrow(_stmt, "isOngoing")
        val _result: MutableList<ActivityEntry> = mutableListOf()
        while (_stmt.step()) {
          val _item: ActivityEntry
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpIsOngoing: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsOngoing).toInt()
          _tmpIsOngoing = _tmp != 0
          _item = ActivityEntry(_tmpId,_tmpTitle,_tmpStartTime,_tmpEndTime,_tmpNote,_tmpIsOngoing)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getCurrentOngoingEntry(): ActivityEntry? {
    val _sql: String = "SELECT * FROM ActivityEntry WHERE isOngoing = 1 ORDER BY startTime DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfIsOngoing: Int = getColumnIndexOrThrow(_stmt, "isOngoing")
        val _result: ActivityEntry?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpIsOngoing: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsOngoing).toInt()
          _tmpIsOngoing = _tmp != 0
          _result = ActivityEntry(_tmpId,_tmpTitle,_tmpStartTime,_tmpEndTime,_tmpNote,_tmpIsOngoing)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun hasOverlappingEntry(
    newStart: Long,
    newEnd: Long,
    excludeId: Long?,
  ): Boolean {
    val _sql: String = """
        |
        |        SELECT COUNT(*) > 0
        |        FROM ActivityEntry
        |        WHERE (? IS NULL OR id != ?)
        |            AND startTime < ?
        |            AND COALESCE(endTime, 9223372036854775807) > ?
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        if (excludeId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, excludeId)
        }
        _argIndex = 2
        if (excludeId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, excludeId)
        }
        _argIndex = 3
        _stmt.bindLong(_argIndex, newEnd)
        _argIndex = 4
        _stmt.bindLong(_argIndex, newStart)
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp != 0
        } else {
          _result = false
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
