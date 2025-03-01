package com.lchristmann.tracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocationDao {

    @Insert
    suspend fun insertLocation(location: LocationEntity)

    @Query("SELECT * FROM location_table WHERE synced = 0")
    suspend fun getUnsyncedLocations(): List<LocationEntity>

    @Query("UPDATE location_table SET synced = 1 WHERE id = :id")
    suspend fun markLocationSynced(id: Int)
}
