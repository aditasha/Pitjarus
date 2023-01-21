package com.aditasha.pitjarus.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aditasha.pitjarus.domain.model.StoreEntity

@Dao
interface StoreDao {

    @Query("DELETE FROM store")
    suspend fun deleteAllStore()

    @Insert
    suspend fun insertStoreList(storeList: List<StoreEntity>)

    @Query("SELECT * FROM store")
    suspend fun fetchAllStore(): List<StoreEntity>

    @Query("SELECT * FROM store WHERE room_id = :roomId LIMIT 1")
    suspend fun fetchStore(roomId: Int): StoreEntity

    @Query("UPDATE store SET picture = :picture WHERE room_id = :roomId")
    suspend fun updatePicture(roomId: Int, picture: String)

    @Query("UPDATE store SET visited = :visited, last_visited = :lastVisited WHERE room_id = :roomId")
    suspend fun updateVisited(roomId: Int, visited: Boolean, lastVisited: Long)

}