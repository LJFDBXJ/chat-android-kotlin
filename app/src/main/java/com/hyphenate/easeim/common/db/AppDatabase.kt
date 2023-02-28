package com.hyphenate.easeim.common.db

import androidx.room.Database
import com.hyphenate.easeim.common.db.entity.EmUserEntity
import com.hyphenate.easeim.common.db.entity.InviteMessage
import com.hyphenate.easeim.common.db.entity.MsgTypeManageEntity
import com.hyphenate.easeim.common.db.entity.AppKeyEntity
import androidx.room.TypeConverters
import androidx.room.RoomDatabase
import com.hyphenate.easeim.common.db.converter.DateConverter
import com.hyphenate.easeim.common.db.dao.EmUserDao
import com.hyphenate.easeim.common.db.dao.InviteMessageDao
import com.hyphenate.easeim.common.db.dao.MsgTypeManageDao
import com.hyphenate.easeim.common.db.dao.AppKeyDao

@Database(
    entities = [EmUserEntity::class, InviteMessage::class, MsgTypeManageEntity::class, AppKeyEntity::class],
    version = 17
)
@TypeConverters(
    DateConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): EmUserDao?
    abstract fun inviteMessageDao(): InviteMessageDao?
    abstract fun msgTypeManageDao(): MsgTypeManageDao?
    abstract fun appKeyDao(): AppKeyDao?
}