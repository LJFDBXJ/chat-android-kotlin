package com.hyphenate.easeim.common.db.entity

import android.text.TextUtils
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import com.hyphenate.easeim.common.db.DbHelper
import java.io.Serializable

@Entity(
    tableName = "em_msg_type",
    primaryKeys = ["id"],
    indices = [Index(value = ["type"], unique = true)]
)
class MsgTypeManageEntity : Serializable {
    var id = 0
    var type: String? = null
    var extField: String? = null

    @get:Ignore
    val lastMsg: Any?
        get() {
            if (TextUtils.equals(type, msgType.NOTIFICATION.name)) {
                val inviteMessageDao = DbHelper.dbHelper().inviteMessageDao
                return inviteMessageDao?.lastInviteMessage()
            }
            return null
        }
    val unReadCount: Int
        get() {
            if (TextUtils.equals(type, msgType.NOTIFICATION.name)) {
                val inviteMessageDao = DbHelper.dbHelper().inviteMessageDao
                return inviteMessageDao?.queryUnreadCount() ?: 0
            }
            return 0
        }

    enum class msgType {
        /**
         * 通知
         */
        NOTIFICATION
    }
}