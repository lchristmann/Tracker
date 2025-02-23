package com.lchristmann.tracker

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface LocationDao {

    @Insert
    suspend fun insertLocation(location: LocationEntity)
}
