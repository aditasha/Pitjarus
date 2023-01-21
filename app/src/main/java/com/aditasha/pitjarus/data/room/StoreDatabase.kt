package com.aditasha.pitjarus.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aditasha.pitjarus.domain.model.StoreEntity

@Database(
    entities = [StoreEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StoreDatabase : RoomDatabase() {

    abstract fun storeDao(): StoreDao
}