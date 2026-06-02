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
public class EnergyDao_Impl(
  __db: RoomDatabase,
) : EnergyDao {
  private val __db: RoomDatabase

  private val __deleteAdapterOfEnergyEntry: EntityDeleteOrUpdateAdapter<EnergyEntry>

  private val __upsertAdapterOfEnergyEntry: EntityUpsertAdapter<EnergyEntry>
  init {
    this.__db = __db
    this.__deleteAdapterOfEnergyEntry = object : EntityDeleteOrUpdateAdapter<EnergyEntry>() {
      protected override fun createQuery(): String = "DELETE FROM `EnergyEntry` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: EnergyEntry) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__upsertAdapterOfEnergyEntry = EntityUpsertAdapter<EnergyEntry>(object : EntityInsertAdapter<EnergyEntry>() {
      protected override fun createQuery(): String = "INSERT INTO `EnergyEntry` (`id`,`timestamp`,`energyLevel`,`note`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: EnergyEntry) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.timestamp)
        statement.bindLong(3, entity.energyLevel.toLong())
        statement.bindText(4, entity.note)
      }
    }, object : EntityDeleteOrUpdateAdapter<EnergyEntry>() {
      protected override fun createQuery(): String = "UPDATE `EnergyEntry` SET `id` = ?,`timestamp` = ?,`energyLevel` = ?,`note` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: EnergyEntry) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.timestamp)
        statement.bindLong(3, entity.energyLevel.toLong())
        statement.bindText(4, entity.note)
        statement.bindLong(5, entity.id)
      }
    })
  }

  public override suspend fun delete(entry: EnergyEntry): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfEnergyEntry.handle(_connection, entry)
  }

  public override suspend fun upsert(entry: EnergyEntry): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfEnergyEntry.upsert(_connection, entry)
  }

  public override fun observeEntries(): Flow<List<EnergyEntry>> {
    val _sql: String = "SELECT * FROM EnergyEntry ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("EnergyEntry")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfEnergyLevel: Int = getColumnIndexOrThrow(_stmt, "energyLevel")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _result: MutableList<EnergyEntry> = mutableListOf()
        while (_stmt.step()) {
          val _item: EnergyEntry
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpEnergyLevel: Int
          _tmpEnergyLevel = _stmt.getLong(_columnIndexOfEnergyLevel).toInt()
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          _item = EnergyEntry(_tmpId,_tmpTimestamp,_tmpEnergyLevel,_tmpNote)
          _result.add(_item)
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
