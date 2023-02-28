package com.hyphenate.easeim.common.db.entity

import androidx.room.Entity
import com.hyphenate.easeim.common.db.DbHelper.Companion.dbHelper
import com.hyphenate.easeim.common.db.entity.MsgTypeManageEntity.msgType
import java.io.Serializable

@Entity(tableName = "em_invite_message", primaryKeys = ["time"])
class InviteMessage : Serializable {
    var id = 0
    var from: String? = null
    var time: Long = 0
    var reason: String? = null
    var type = msgType.NOTIFICATION.name
        private set
    var status: String? = null
        private set
    var groupId: String? = null
    var groupName: String? = null
    var groupInviter: String? = null
    var isUnread = false//是否已读
    val statusEnum: InviteMessageStatus?
        get() {
            var status: InviteMessageStatus? = null
            try {
                status = InviteMessageStatus.valueOf(this.status!!)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            return status
        }

    fun setStatus(status: InviteMessageStatus) {
        this.status = status.name
    }

    fun setStatus(status: String?) {
        this.status = status
    }

    val typeEnum: msgType?
        get() {
            var type: msgType? = null
            try {
                type = msgType.valueOf(this.type)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            return type
        }

    fun setType(type: msgType) {
        //保存相应类型的MsgTypeManageEntity
        val entity = MsgTypeManageEntity()
        entity.type = type.name
        val msgTypeManageDao = dbHelper().msgTypeManageDao
        msgTypeManageDao?.insert(entity)
        this.type = type.name
    }

    fun setType(type: String) {
        this.type = type
    }
}