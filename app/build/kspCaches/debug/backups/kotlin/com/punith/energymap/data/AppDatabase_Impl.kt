package com.punith.energymap.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _energyDao: Lazy<EnergyDao> = lazy {
    EnergyDao_Impl(this)
  }

  private val _activityDao: Lazy<ActivityDao> = lazy {
    ActivityDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "0d415bd15ef93084273c577d91dfcf94", "86ffb66529274ea591ac19f65edc0d15") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `EnergyEntry` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `energyLevel` INTEGER NOT NULL, `note` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `ActivityEntry` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER, `note` TEXT NOT NULL, `isOngoing` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '0d415bd15ef93084273c577d91dfcf94')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `EnergyEntry`")
        connection.execSQL("DROP TABLE IF EXISTS `ActivityEntry`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsEnergyEntry: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsEnergyEntry.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEnergyEntry.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEnergyEntry.put("energyLevel", TableInfo.Column("energyLevel", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEnergyEntry.put("note", TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysEnergyEntry: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesEnergyEntry: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoEnergyEntry: TableInfo = TableInfo("EnergyEntry", _columnsEnergyEntry, _foreignKeysEnergyEntry, _indicesEnergyEntry)
        val _existingEnergyEntry: TableInfo = read(connection, "EnergyEntry")
        if (!_infoEnergyEntry.equals(_existingEnergyEntry)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |EnergyEntry(com.punith.energymap.data.EnergyEntry).
              | Expected:
              |""".trimMargin() + _infoEnergyEntry + """
              |
              | Found:
              |""".trimMargin() + _existingEnergyEntry)
        }
        val _columnsActivityEntry: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsActivityEntry.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsActivityEntry.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsActivityEntry.put("startTime", TableInfo.Column("startTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsActivityEntry.put("endTime", TableInfo.Column("endTime", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsActivityEntry.put("note", TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsActivityEntry.put("isOngoing", TableInfo.Column("isOngoing", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysActivityEntry: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesActivityEntry: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoActivityEntry: TableInfo = TableInfo("ActivityEntry", _columnsActivityEntry, _foreignKeysActivityEntry, _indicesActivityEntry)
        val _existingActivityEntry: TableInfo = read(connection, "ActivityEntry")
        if (!_infoActivityEntry.equals(_existingActivityEntry)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |ActivityEntry(com.punith.energymap.data.ActivityEntry).
              | Expected:
              |""".trimMargin() + _infoActivityEntry + """
              |
              | Found:
              |""".trimMargin() + _existingActivityEntry)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "EnergyEntry", "ActivityEntry")
  }

  public override fun clearAllTables() {
    super.performClear(false, "EnergyEntry", "ActivityEntry")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(EnergyDao::class, EnergyDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ActivityDao::class, ActivityDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun energyDao(): EnergyDao = _energyDao.value

  public override fun activityDao(): ActivityDao = _activityDao.value
}
