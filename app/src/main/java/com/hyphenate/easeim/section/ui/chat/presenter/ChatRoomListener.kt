package com.hyphenate.easeim.section.ui.chat.presenter

import android.text.TextUtils
import com.hyphenate.EMChatRoomChangeListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.adapter.EMAChatRoomManagerListener
import com.hyphenate.easeim.AppClient
import com.hyphenate.easeim.R
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.util.EMLog

 class ChatRoomListener() : EMChatRoomChangeListener {
    private val TAG = "ChatRoomListener"
    override fun onChatRoomDestroyed(roomId: String, roomName: String) {
        setChatRoomEvent(roomId, EaseEvent.TYPE.CHAT_ROOM_LEAVE)
         AppClient.instance.toast(
             AppClient.instance.getString(
                R.string.demo_chat_room_listener_onChatRoomDestroyed,
                roomName
            )
        )
        EMLog.i(
            TAG,
             AppClient.instance.getString(
                R.string.demo_chat_room_listener_onChatRoomDestroyed,
                roomName
            )
        )
    }

    override fun onMemberJoined(roomId: String, participant: String) {
        setChatRoomEvent(roomId, EaseEvent.TYPE.CHAT_ROOM)
         AppClient.instance. toast(
             AppClient.instance.getString(
                R.string.demo_chat_room_listener_onMemberJoined,
                participant
            )
        )
        EMLog.i(
            TAG,
             AppClient.instance.getString(
                R.string.demo_chat_room_listener_onMemberJoined,
                participant
            )
        )
    }

    override fun onMemberExited(roomId: String, roomName: String, participant: String) {
        setChatRoomEvent(roomId, EaseEvent.TYPE.CHAT_ROOM)
         AppClient.instance.toast( AppClient.instance.getString(
                R.string.demo_chat_room_listener_onMemberExited,
                participant
            )
        )
        EMLog.i(
            TAG,
             AppClient.instance.getString(
                R.string.demo_chat_room_listener_onMemberExited,
                participant
            )
        )
    }

    override fun onRemovedFromChatRoom(
        reason: Int,
        roomId: String,
        roomName: String,
        participant: String
    ) {
        if (TextUtils.equals(SdkHelper.instance.currentUser, participant)) {
            setChatRoomEvent(roomId, EaseEvent.TYPE.CHAT_ROOM)
            if (reason == EMAChatRoomManagerListener.BE_KICKED) {
                 AppClient.instance.toast(R.string.quiting_the_chat_room)
                 AppClient.instance.toast(R.string.quiting_the_chat_room)
            } else {
                 AppClient.instance.toast(
                     AppClient.instance.getString(
                        R.string.demo_chat_room_listener_onRemovedFromChatRoom,
                        participant
                    )
                )
                EMLog.i(
                    TAG,
                     AppClient.instance.getString(
                        R.string.demo_chat_room_listener_onRemovedFromChatRoom,
                        participant
                    )
                )
            }
        }
    }

    override fun onMuteListAdded(chatRoomId: String, mutes: List<String>, expireTime: Long) {
        setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM)
        val content = getContentFromList(mutes)
         AppClient.instance.toast( AppClient.instance.getString(
                R.string.demo_chat_room_listener_onMuteListAdded,
                content
            )
        )
        EMLog.i(
            TAG,
             AppClient.instance.getString(R.string.demo_chat_room_listener_onMuteListAdded, content)
        )
    }

    override fun onMuteListRemoved(chatRoomId: String, mutes: List<String>) {
        setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM)
        val content = getContentFromList(mutes)
         AppClient.instance.toast(
             AppClient.instance.getString(
                R.string.demo_chat_room_listener_onMuteListRemoved,
                content
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(
                R.string.demo_chat_room_listener_onMuteListRemoved,
                content
            )
        )
    }

    override fun onWhiteListAdded(chatRoomId: String, whitelist: List<String>) {
        val content = getContentFromList(whitelist)
         AppClient.instance.toast( AppClient.instance.getString(
                R.string.demo_chat_room_listener_onWhiteListAdded,
                content
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(R.string.demo_chat_room_listener_onWhiteListAdded, content)
        )
    }

    override fun onWhiteListRemoved(chatRoomId: String, whitelist: List<String>) {
        val content = getContentFromList(whitelist)
         AppClient.instance.toast( AppClient.instance.getString(
                R.string.demo_chat_room_listener_onWhiteListRemoved,
                content
            )
        )
        EMLog.i(
            TAG,
             AppClient.instance.getString(
                R.string.demo_chat_room_listener_onWhiteListRemoved,
                content
            )
        )
    }

    override fun onAllMemberMuteStateChanged(chatRoomId: String, isMuted: Boolean) {
         AppClient.instance.toast( AppClient.instance.getString(if (isMuted) R.string.demo_chat_room_listener_onAllMemberMuteStateChanged_mute else R.string.demo_chat_room_listener_onAllMemberMuteStateChanged_note_mute))
        EMLog.i(
            TAG,
             AppClient.instance.getString(if (isMuted) R.string.demo_chat_room_listener_onAllMemberMuteStateChanged_mute else R.string.demo_chat_room_listener_onAllMemberMuteStateChanged_note_mute)
        )
    }

    override fun onAdminAdded(chatRoomId: String, admin: String) {
        setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM)
         AppClient.instance.toast(
            AppClient.instance.getString(
                R.string.demo_chat_room_listener_onAdminAdded,
                admin
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(R.string.demo_chat_room_listener_onAdminAdded, admin)
        )
    }

    override fun onAdminRemoved(chatRoomId: String, admin: String) {
        setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM)
         AppClient.instance.toast(
             AppClient.instance.getString(
                R.string.demo_chat_room_listener_onAdminRemoved,
                admin
            )
        )
        EMLog.i(
            TAG,
             AppClient.instance.getString(R.string.demo_chat_room_listener_onAdminRemoved, admin)
        )
    }

    override fun onOwnerChanged(chatRoomId: String, newOwner: String, oldOwner: String) {
        setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM)
         AppClient.instance.toast(
             AppClient.instance.getString(
                R.string.demo_chat_room_listener_onOwnerChanged,
                oldOwner,
                newOwner
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(
                R.string.demo_chat_room_listener_onOwnerChanged,
                oldOwner,
                newOwner
            )
        )
    }


    override fun onAnnouncementChanged(chatRoomId: String, announcement: String?) {
        setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM)
        AppClient.instance.toast(R.string.demo_chat_room_listener_onAnnouncementChanged)
        EMLog.i(
            TAG,
            AppClient.instance.getString(R.string.demo_chat_room_listener_onAnnouncementChanged)
        )
    }


    private fun getContentFromList(members: List<String>): String {
        val sb = StringBuilder()
        for (member in members) {
            if (!TextUtils.isEmpty(sb.toString().trim { it <= ' ' })) {
                sb.append(",")
            }
            sb.append(member)
        }
        var content = sb.toString()
        if (content.contains(EMClient.getInstance().currentUser)) {
            content = AppClient.instance.getString(R.string.you)
        }
        return content
    }

    private fun setChatRoomEvent(roomId: String, type: EaseEvent.TYPE) {
        val easeEvent = EaseEvent(DemoConstant.CHAT_ROOM_CHANGE, type)
        easeEvent.message = roomId
        LiveDataBus.get().use(DemoConstant.CHAT_ROOM_CHANGE).postValue(easeEvent)
    }

}
