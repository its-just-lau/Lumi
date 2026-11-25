package com.lumi.app.core.storage.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction

/**
 * Elemento guardado en la caché local: la colección a la que pertenece (tareas, materias...),
 * su id y el JSON que devolvió la API.
 */
@Entity(tableName = "cache_items", primaryKeys = ["collection", "id"])
data class CacheItem(
    val collection: String,
    val id: String,
    val json: String
)

@Dao
abstract class CacheDao {

    @Query("SELECT json FROM cache_items WHERE collection = :collection")
    abstract suspend fun getJson(collection: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(items: List<CacheItem>)

    @Query("DELETE FROM cache_items WHERE collection = :collection")
    abstract suspend fun clear(collection: String)

    @Query("DELETE FROM cache_items")
    abstract suspend fun clearAll()

    /** Reemplaza toda la colección de forma atómica. */
    @Transaction
    open suspend fun replace(collection: String, items: List<CacheItem>) {
        clear(collection)
        insertAll(items)
    }
}

@Database(entities = [CacheItem::class], version = 1, exportSchema = false)
abstract class LumiDatabase : RoomDatabase() {

    abstract fun cacheDao(): CacheDao

    companion object {
        fun create(context: Context): LumiDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                LumiDatabase::class.java,
                "lumi.db"
            ).build()
        }
    }
}
