package com.hyphenate.easeim.common.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hyphenate.easeim.common.db.entity.AppKeyEntity

@Dao
interface AppKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg keys: AppKeyEntity?): List<Long>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(keys: List<AppKeyEntity>?): List<Long>?

    @Query("select * from app_key  order by timestamp asc")
    fun loadAllAppKeys(): List<AppKeyEntity>?

    @Query("delete from app_key where appKey = :arg0")
    fun deleteAppKey(arg0: String)

    @Query("select * from app_key where appKey = :arg0")
    fun queryKey(arg0: String): List<AppKeyEntity>?
}