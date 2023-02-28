package com.hyphenate.easeim.common.db.dao

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import com.hyphenate.easeim.common.db.entity.EmUserEntity
import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.Query
import com.hyphenate.easeui.domain.EaseUser

@Dao
interface EmUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg users: EmUserEntity?): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(users: List<EmUserEntity?>?): List<Long>

    @Query("select * from em_users where username = :arg0")
    fun loadUserById(arg0: String?): LiveData<List<EaseUser>>

    @Query("select * from em_users where username = :arg0")
    fun loadUserByUserId(arg0: String?): List<EaseUser>

    @Query("select * from em_users where contact = 0")
    fun loadUsers(): LiveData<List<EaseUser>>

    @Query("select * from em_users where contact = 0")
    fun loadContacts(): List<EaseUser>

    @Query("select * from em_users where contact = 1")
    fun loadBlackUsers(): LiveData<List<EaseUser>>

    @Query("select * from em_users where contact = 1")
    fun loadBlackEaseUsers(): List<EaseUser>

    @Query("select username from em_users")
    fun loadAllUsers(): List<String>?

    @Query("select username from em_users where contact = 0 or contact = 1")
    fun loadContactUsers(): List<String>?

    @Query("select * from em_users")
    fun loadAllEaseUsers(): List<EaseUser>

    @Query("select * from em_users where contact = 0 or contact = 1")
    fun loadAllContactUsers(): List<EaseUser>

    @Query("delete from em_users")
    fun clearUsers(): Int

    @Query("delete from em_users where contact = 1")
    fun clearBlackUsers(): Int

    @Query("delete from em_users where username = :arg0")
    fun deleteUser(arg0: String?): Int

    @Query("update em_users set contact = :arg0  where username = :arg1")
    fun updateContact(arg0: Int, arg1: String?): Int

    @Query("select username from em_users where lastModifyTimestamp + :arg0  <= :arg1")
    fun loadTimeOutEaseUsers(arg0: Long, arg1: Long): List<String>

    @Query("select username from em_users where lastModifyTimestamp + :arg0  <= :arg1 and contact = 1")
    fun loadTimeOutFriendUser(arg0: Long, arg1: Long): List<String>
}