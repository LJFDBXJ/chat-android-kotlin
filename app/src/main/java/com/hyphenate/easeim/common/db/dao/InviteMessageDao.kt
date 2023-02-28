package com.hyphenate.easeim.common.db.dao

import com.hyphenate.easeim.common.db.entity.InviteMessage
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface InviteMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg entities: InviteMessage?): List<Long>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entities: List<InviteMessage>?): List<Long>?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg entities: InviteMessage?): Int

    @Query("update em_invite_message set isUnread = 0")
    fun makeAllReaded()

    @Query("select * from em_invite_message")
    fun loadAllInviteMessages(): LiveData<List<InviteMessage>>?

    @Query("select * from em_invite_message order by isUnread desc, time desc limit:limit offset:offset")
    fun loadMessages(limit: Int, offset: Int): LiveData<List<InviteMessage>>?

    @Query("select * from em_invite_message")
    fun loadAll(): List<InviteMessage>?

    @Query("select `from` from em_invite_message")
    fun loadAllNames(): List<String>?

    @Query("select * from em_invite_message order by time desc limit 1")
    fun lastInviteMessage(): InviteMessage?

    @Query("select count(isUnread) from em_invite_message where isUnread = 1")
    fun queryUnreadCount(): Int

    @Query("delete from em_invite_message where groupId = :groupId")
    fun deleteByGroupId(groupId: String?)

    @Query("delete from em_invite_message where groupId=:groupId and `from`= :username")
    fun deleteByGroupId(groupId: String?, username: String?)

    @Query("delete from em_invite_message where `from`=:from")
    fun deleteByFrom(from: String?)

    @Query("delete from em_invite_message where :key =:from")
    fun delete(key: String?, from: String?)

    @Delete
    fun delete(vararg msg: InviteMessage?)
}