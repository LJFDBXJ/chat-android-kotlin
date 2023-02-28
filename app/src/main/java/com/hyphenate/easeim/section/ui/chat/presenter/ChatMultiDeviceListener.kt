package com.hyphenate.easeim.section.ui.chat.presenter

import android.text.TextUtils
import com.hyphenate.EMMultiDeviceListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMTextMessageBody
import com.hyphenate.easeim.AppClient
import com.hyphenate.easeim.R
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.db.DbHelper
import com.hyphenate.easeim.common.db.entity.EmUserEntity
import com.hyphenate.easeim.common.db.entity.InviteMessageStatus
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.manager.PushAndMessageHelper
import com.hyphenate.easeui.manager.EaseSystemMsgManager
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.model.EaseNotifier
import com.hyphenate.exceptions.HyphenateException
import com.hyphenate.util.EMLog
import java.util.*

class ChatMultiDeviceListener(private val notifier: EaseNotifier) : EMMultiDeviceListener {
    private val TAG = "ChatMultiDeviceListener"
    override fun onContactEvent(event: Int, target: String, ext: String) {
        EMLog.i(TAG, "onContactEvent event$event")
        val dbHelper = DbHelper.dbHelper()
        var message: String? = null
        when (event) {
            EMMultiDeviceListener.CONTACT_REMOVE -> {
                EMLog.i("ChatMultiDeviceListener", "CONTACT_REMOVE")
                message = DemoConstant.CONTACT_REMOVE
                dbHelper.userDao?.deleteUser(target)
                removeTargetSystemMessage(target, DemoConstant.SYSTEM_MESSAGE_FROM)
                // TODO: 2020/1/16 0016 确认此处逻辑，是否是删除当前的target
                SdkHelper.instance.chatManager.deleteConversation(target, false)
                AppClient.instance.toast("CONTACT_REMOVE")
            }
            EMMultiDeviceListener.CONTACT_ACCEPT -> {
                EMLog.i("ChatMultiDeviceListener", "CONTACT_ACCEPT")
                message = DemoConstant.CONTACT_ACCEPT
                val entity = EmUserEntity()
                entity.username = target
                dbHelper.userDao?.insert(entity)
                updateContactNotificationStatus(
                    from = target,
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_CONTACT_ACCEPT
                )
                AppClient.instance.toast("CONTACT_ACCEPT")
            }
            EMMultiDeviceListener.CONTACT_DECLINE -> {
                EMLog.i("ChatMultiDeviceListener", "CONTACT_DECLINE")
                message = DemoConstant.CONTACT_DECLINE
                updateContactNotificationStatus(
                    from = target,
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_CONTACT_DECLINE
                )
                AppClient.instance.toast("CONTACT_DECLINE")
            }
            EMMultiDeviceListener.CONTACT_BAN -> {
                EMLog.i("ChatMultiDeviceListener", "CONTACT_BAN")
                message = DemoConstant.CONTACT_BAN
                dbHelper.userDao?.deleteUser(arg0 = target)
                removeTargetSystemMessage(target, DemoConstant.SYSTEM_MESSAGE_FROM)
                SdkHelper.instance.chatManager.deleteConversation(target, false)
                updateContactNotificationStatus(
                    from = target,
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_CONTACT_BAN
                )
                AppClient.instance.toast("CONTACT_BAN")
            }
            EMMultiDeviceListener.CONTACT_ALLOW -> {
                EMLog.i("ChatMultiDeviceListener", "CONTACT_ALLOW")
                message = DemoConstant.CONTACT_ALLOW
                updateContactNotificationStatus(
                    from = target,
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_CONTACT_ALLOW
                )
                AppClient.instance.toast("CONTACT_ALLOW")
            }
        }
        if (!TextUtils.isEmpty(message)) {
            val easeEvent = EaseEvent.create(message, EaseEvent.TYPE.CONTACT)
            LiveDataBus.get().use(message!!).postValue(easeEvent)
        }
    }

    override fun onGroupEvent(event: Int, groupId: String, usernames: List<String>) {
        EMLog.i(TAG, "onGroupEvent event$event")
        var message: String? = null
        when (event) {
            EMMultiDeviceListener.GROUP_CREATE -> {
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = "",  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_CREATE
                )
                AppClient.instance.toast("GROUP_CREATE")
            }
            EMMultiDeviceListener.GROUP_DESTROY -> {
                removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID)
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = "",  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_DESTROY
                )
                message = DemoConstant.GROUP_CHANGE
                AppClient.instance.toast("GROUP_DESTROY")
            }
            EMMultiDeviceListener.GROUP_JOIN -> {
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = "",  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_JOIN
                )
                message = DemoConstant.GROUP_CHANGE
                AppClient.instance.toast("GROUP_JOIN")
            }
            EMMultiDeviceListener.GROUP_LEAVE -> {
                removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID)
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = "",  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_LEAVE
                )
                message = DemoConstant.GROUP_CHANGE
                AppClient.instance.toast("GROUP_LEAVE")
            }
            EMMultiDeviceListener.GROUP_APPLY -> {
                removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID)
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = "",  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY
                )
                AppClient.instance.toast("GROUP_APPLY")
            }
            EMMultiDeviceListener.GROUP_APPLY_ACCEPT -> {
                removeTargetSystemMessage(
                    groupId,
                    DemoConstant.SYSTEM_MESSAGE_GROUP_ID,
                    usernames[0],
                    DemoConstant.SYSTEM_MESSAGE_FROM
                )
                // TODO: person, reason from ext
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = usernames[0],  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY_ACCEPT
                )
                AppClient.instance.toast("GROUP_APPLY_ACCEPT")
            }
            EMMultiDeviceListener.GROUP_APPLY_DECLINE -> {
                removeTargetSystemMessage(
                    groupId,
                    DemoConstant.SYSTEM_MESSAGE_GROUP_ID,
                    usernames[0],
                    DemoConstant.SYSTEM_MESSAGE_FROM
                )
                // TODO: person, reason from ext
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = usernames[0],  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY_DECLINE
                )
                AppClient.instance.toast("GROUP_APPLY_DECLINE")
            }
            EMMultiDeviceListener.GROUP_INVITE -> {
                // TODO: person, reason from ext
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = usernames[0],  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE
                )
                AppClient.instance.toast("GROUP_INVITE")
            }
            EMMultiDeviceListener.GROUP_INVITE_ACCEPT -> {
                val st3 = AppClient.instance.getString(R.string.Invite_you_to_join_a_group_chat)
                val msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT)
                msg.chatType = EMMessage.ChatType.GroupChat
                // TODO: person, reason from ext
                val from = ""
                if (usernames.isNotEmpty()) {
                    msg.from = usernames[0]
                }
                msg.to = groupId
                msg.msgId = UUID.randomUUID().toString()
                msg.setAttribute(DemoConstant.EM_NOTIFICATION_TYPE, true)
                msg.addBody(EMTextMessageBody(msg.from + " " + st3))
                msg.setStatus(EMMessage.Status.SUCCESS)
                // save invitation as messages
                EMClient.getInstance().chatManager().saveMessage(msg)
                removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID)
                // TODO: person, reason from ext
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = "",  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE_ACCEPT
                )
                message = DemoConstant.GROUP_CHANGE
                AppClient.instance.toast("GROUP_INVITE_ACCEPT")
            }
            EMMultiDeviceListener.GROUP_INVITE_DECLINE -> {
                removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID)
                // TODO: person, reason from ext
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = usernames[0],  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE_DECLINE
                )
                AppClient.instance.toast("GROUP_INVITE_DECLINE")
            }
            EMMultiDeviceListener.GROUP_KICK -> {
                // TODO: person, reason from ext
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = usernames[0],  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_KICK
                )
                message = DemoConstant.GROUP_CHANGE
                AppClient.instance.toast("GROUP_KICK")
            }
            EMMultiDeviceListener.GROUP_BAN -> {
                // TODO: person from ext
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = usernames[0],  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_BAN
                )
                message = DemoConstant.GROUP_CHANGE
                AppClient.instance.toast("GROUP_BAN")
            }
            EMMultiDeviceListener.GROUP_ALLOW -> {
                // TODO: person from ext
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = usernames[0],  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_ALLOW
                )
                AppClient.instance.toast("GROUP_ALLOW")
            }
            EMMultiDeviceListener.GROUP_BLOCK -> {
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = "",  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_BLOCK
                )
                AppClient.instance.toast("GROUP_BLOCK")
            }
            EMMultiDeviceListener.GROUP_UNBLOCK -> {
                // TODO: person from ext
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = "",  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_UNBLOCK
                )
                AppClient.instance.toast("GROUP_UNBLOCK")
            }
            EMMultiDeviceListener.GROUP_ASSIGN_OWNER -> {
                // TODO: person from ext
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = usernames[0],  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_ASSIGN_OWNER
                )
                AppClient.instance.toast("GROUP_ASSIGN_OWNER")
            }
            EMMultiDeviceListener.GROUP_ADD_ADMIN -> {
                // TODO: person from ext
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = usernames[0],  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_ADD_ADMIN
                )
                message = DemoConstant.GROUP_CHANGE
                AppClient.instance.toast("GROUP_ADD_ADMIN")
            }
            EMMultiDeviceListener.GROUP_REMOVE_ADMIN -> {
                // TODO: person from ext
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = usernames[0],  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_REMOVE_ADMIN
                )
                message = DemoConstant.GROUP_CHANGE
                AppClient.instance.toast("GROUP_REMOVE_ADMIN")
            }
            EMMultiDeviceListener.GROUP_ADD_MUTE -> {
                // TODO: person from ext
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = usernames[0],  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_ADD_MUTE
                )
                AppClient.instance.toast("GROUP_ADD_MUTE")
            }
            EMMultiDeviceListener.GROUP_REMOVE_MUTE -> {
                // TODO: person from ext
                saveGroupNotification(
                    groupId = groupId,  /*groupName*/
                    groupName = "",  /*person*/
                    inviter = usernames[0],  /*reason*/
                    reason = "",
                    status = InviteMessageStatus.MULTI_DEVICE_GROUP_REMOVE_MUTE
                )
                AppClient.instance.toast("GROUP_REMOVE_MUTE")
            }
            else -> {}
        }
        if (!TextUtils.isEmpty(message)) {
            val easeEvent = EaseEvent.create(message, EaseEvent.TYPE.GROUP)
            LiveDataBus.get().use(message!!).postValue(easeEvent)
        }
    }

    private fun saveGroupNotification(
        groupId: String,
        groupName: String,
        inviter: String,
        reason: String,
        status: InviteMessageStatus
    ) {
        val ext = EaseSystemMsgManager.getInstance().createMsgExt()
        ext[DemoConstant.SYSTEM_MESSAGE_FROM] = groupId
        ext[DemoConstant.SYSTEM_MESSAGE_GROUP_ID] = groupId
        ext[DemoConstant.SYSTEM_MESSAGE_REASON] = reason
        ext[DemoConstant.SYSTEM_MESSAGE_NAME] = groupName
        ext[DemoConstant.SYSTEM_MESSAGE_INVITER] = inviter
        ext[DemoConstant.SYSTEM_MESSAGE_STATUS] = status.name
        val message = EaseSystemMsgManager.getInstance().createMessage(
            PushAndMessageHelper.getSystemMessage(
                ext
            ), ext
        )
        notifyNewInviteMessage(message)
    }

    private fun updateContactNotificationStatus(
        from: String,
        reason: String,
        status: InviteMessageStatus
    ) {
        var msg: EMMessage? = null
        val conversation = EaseSystemMsgManager.getInstance().conversation
        val allMessages = conversation.allMessages
        if (allMessages != null && !allMessages.isEmpty()) {
            for (message in allMessages) {
                val ext = message.ext()
                if (ext != null && (ext.containsKey(DemoConstant.SYSTEM_MESSAGE_FROM)
                            && TextUtils.equals(
                        from,
                        ext[DemoConstant.SYSTEM_MESSAGE_FROM] as String?
                    ))
                ) {
                    msg = message
                }
            }
        }
        if (msg != null) {
            msg.setAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS, status.name)
            EaseSystemMsgManager.getInstance().updateMessage(msg)
        } else {
            // save invitation as message
            val ext = EaseSystemMsgManager.getInstance().createMsgExt()
            ext[DemoConstant.SYSTEM_MESSAGE_FROM] = from
            ext[DemoConstant.SYSTEM_MESSAGE_REASON] = reason
            ext[DemoConstant.SYSTEM_MESSAGE_STATUS] = status.name
            msg = EaseSystemMsgManager.getInstance().createMessage(
                PushAndMessageHelper.getSystemMessage(
                    ext
                ), ext
            )
            notifyNewInviteMessage(msg)
        }
    }


    /**
     * 移除目标所有的消息记录，如果目标被删除
     * @param target
     */
    private fun removeTargetSystemMessage(target: String, params: String) {
        val conversation = EaseSystemMsgManager.getInstance().conversation
        val messages = conversation.allMessages
        if (messages != null && !messages.isEmpty()) {
            for (message in messages) {
                var from: String? = null
                try {
                    from = message.getStringAttribute(params)
                } catch (e: HyphenateException) {
                    e.printStackTrace()
                }
                if (TextUtils.equals(from, target)) {
                    conversation.removeMessage(message.msgId)
                }
            }
        }
    }

    /**
     * 移除目标所有的消息记录，如果目标被删除
     * @param target1
     */
    private fun removeTargetSystemMessage(
        target1: String,
        params1: String,
        target2: String,
        params2: String
    ) {
        val conversation = EaseSystemMsgManager.getInstance().conversation
        val messages = conversation.allMessages
        if (messages != null && !messages.isEmpty()) {
            for (message in messages) {
                var targetParams1: String? = null
                var targetParams2: String? = null
                try {
                    targetParams1 = message.getStringAttribute(params1)
                    targetParams2 = message.getStringAttribute(params2)
                } catch (e: HyphenateException) {
                    e.printStackTrace()
                }
                if (TextUtils.equals(targetParams1, target1) && TextUtils.equals(
                        targetParams2,
                        target2
                    )
                ) {
                    conversation.removeMessage(message.msgId)
                }
            }
        }
    }

    private fun notifyNewInviteMessage(msg: EMMessage?) {
        // notify there is new message
        notifier.vibrateAndPlayTone(null)
    }
}
