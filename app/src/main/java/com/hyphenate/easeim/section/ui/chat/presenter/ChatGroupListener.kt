package com.hyphenate.easeim.section.ui.chat.presenter

import android.text.TextUtils
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMMucSharedFile
import com.hyphenate.chat.EMTextMessageBody
import com.hyphenate.easeim.AppClient
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.db.entity.InviteMessageStatus
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.manager.PushAndMessageHelper
import com.hyphenate.easeim.section.ui.group.GroupHelper
import com.hyphenate.easeui.interfaces.EaseGroupListener
import com.hyphenate.easeui.manager.EaseSystemMsgManager
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.model.EaseNotifier
import com.hyphenate.util.EMLog
import java.util.*

class ChatGroupListener(private val notifier: EaseNotifier) : EaseGroupListener() {
    private val TAG = "ChatGroupListener"
    override fun onInvitationReceived(
        groupId: String,
        mGroupName: String,
        inviter: String,
        reason: String
    ) {
        var groupName = mGroupName
        super.onInvitationReceived(groupId, groupName, inviter, reason)
        //移除相同的请求
        val allMessages = EaseSystemMsgManager.getInstance().allMessages
        if (!allMessages.isNullOrEmpty()) {
            for (message in allMessages) {
                val ext = message.ext()
                if (ext != null && ext.containsKey(DemoConstant.SYSTEM_MESSAGE_GROUP_ID) && TextUtils.equals(
                        groupId,
                        ext[DemoConstant.SYSTEM_MESSAGE_GROUP_ID] as String?
                    )
                    && ext.containsKey(DemoConstant.SYSTEM_MESSAGE_INVITER) && TextUtils.equals(
                        inviter,
                        ext[DemoConstant.SYSTEM_MESSAGE_INVITER] as String?
                    )
                ) {
                    EaseSystemMsgManager.getInstance().removeMessage(message)
                }
            }
        }
        groupName = groupName.ifEmpty { groupId }
        val ext = EaseSystemMsgManager.getInstance().createMsgExt()
        ext[DemoConstant.SYSTEM_MESSAGE_FROM] = groupId
        ext[DemoConstant.SYSTEM_MESSAGE_GROUP_ID] = groupId
        ext[DemoConstant.SYSTEM_MESSAGE_REASON] = reason
        ext[DemoConstant.SYSTEM_MESSAGE_NAME] = groupName
        ext[DemoConstant.SYSTEM_MESSAGE_INVITER] = inviter
        ext[DemoConstant.SYSTEM_MESSAGE_STATUS] = InviteMessageStatus.GROUPINVITATION.name
        val message =
            EaseSystemMsgManager.getInstance().createMessage(
                PushAndMessageHelper.getSystemMessage(ext), ext
            )
        notifyNewInviteMessage(message)
        val event =
            EaseEvent.create(DemoConstant.NOTIFY_GROUP_INVITE_RECEIVE, EaseEvent.TYPE.NOTIFY)
        LiveDataBus.get().use(DemoConstant.NOTIFY_CHANGE).postValue(event)
        AppClient.instance.toast(
            AppClient.instance.getString(
                InviteMessageStatus.GROUPINVITATION.msgContent,
                inviter,
                groupName
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(
                InviteMessageStatus.GROUPINVITATION.msgContent,
                inviter,
                groupName
            )
        )
    }

    override fun onInvitationAccepted(groupId: String, invitee: String, reason: String) {
        super.onInvitationAccepted(groupId, invitee, reason)
        //user accept your invitation
        val groupName = GroupHelper.getGroupName(groupId)
        val ext = EaseSystemMsgManager.getInstance().createMsgExt()
        ext[DemoConstant.SYSTEM_MESSAGE_FROM] = groupId
        ext[DemoConstant.SYSTEM_MESSAGE_GROUP_ID] = groupId
        ext[DemoConstant.SYSTEM_MESSAGE_REASON] = reason
        ext[DemoConstant.SYSTEM_MESSAGE_NAME] = groupName
        ext[DemoConstant.SYSTEM_MESSAGE_INVITER] = invitee
        ext[DemoConstant.SYSTEM_MESSAGE_STATUS] =
            InviteMessageStatus.GROUPINVITATION_ACCEPTED.name
        val message =
            EaseSystemMsgManager.getInstance()
                .createMessage(PushAndMessageHelper.getSystemMessage(ext), ext)
        notifyNewInviteMessage(message)
        val event =
            EaseEvent.create(DemoConstant.NOTIFY_GROUP_INVITE_ACCEPTED, EaseEvent.TYPE.NOTIFY)
        LiveDataBus.get().use(DemoConstant.NOTIFY_CHANGE).postValue(event)
        AppClient.instance.toast(
            AppClient.instance.getString(
                InviteMessageStatus.GROUPINVITATION_ACCEPTED.msgContent,
                invitee
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(
                InviteMessageStatus.GROUPINVITATION_ACCEPTED.msgContent,
                invitee
            )
        )
    }

    override fun onInvitationDeclined(groupId: String, invitee: String, reason: String) {
        super.onInvitationDeclined(groupId, invitee, reason)
        //user declined your invitation
        val groupName = GroupHelper.getGroupName(groupId)
        val ext = EaseSystemMsgManager.getInstance().createMsgExt()
        ext[DemoConstant.SYSTEM_MESSAGE_FROM] = groupId
        ext[DemoConstant.SYSTEM_MESSAGE_GROUP_ID] = groupId
        ext[DemoConstant.SYSTEM_MESSAGE_REASON] = reason
        ext[DemoConstant.SYSTEM_MESSAGE_NAME] = groupName
        ext[DemoConstant.SYSTEM_MESSAGE_INVITER] = invitee
        ext[DemoConstant.SYSTEM_MESSAGE_STATUS] =
            InviteMessageStatus.GROUPINVITATION_DECLINED.name
        val message =
            EaseSystemMsgManager.getInstance()
                .createMessage(PushAndMessageHelper.getSystemMessage(ext), ext)
        notifyNewInviteMessage(message)
        val event =
            EaseEvent.create(DemoConstant.NOTIFY_GROUP_INVITE_DECLINED, EaseEvent.TYPE.NOTIFY)
        LiveDataBus.get().use(DemoConstant.NOTIFY_CHANGE).postValue(event)
        AppClient.instance.toast(
            AppClient.instance.getString(
                InviteMessageStatus.GROUPINVITATION_DECLINED.msgContent,
                invitee
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(
                InviteMessageStatus.GROUPINVITATION_DECLINED.msgContent,
                invitee
            )
        )
    }

    override fun onUserRemoved(groupId: String, groupName: String) {
        val easeEvent = EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP_LEAVE)
        easeEvent.message = groupId
        LiveDataBus.get().use(DemoConstant.GROUP_CHANGE).postValue(easeEvent)
        AppClient.instance.toast(
            AppClient.instance.getString(
                R.string.demo_group_listener_onUserRemoved,
                groupName
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(R.string.demo_group_listener_onUserRemoved, groupName)
        )
    }

    override fun onGroupDestroyed(groupId: String, groupName: String) {
        val easeEvent = EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP_LEAVE)
        easeEvent.message = groupId
        LiveDataBus.get().use(DemoConstant.GROUP_CHANGE).postValue(easeEvent)
        AppClient.instance.toast(
            AppClient.instance.getString(
                R.string.demo_group_listener_onGroupDestroyed,
                groupName
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(R.string.demo_group_listener_onGroupDestroyed, groupName)
        )
    }

    override fun onRequestToJoinReceived(
        groupId: String,
        groupName: String,
        applicant: String,
        reason: String
    ) {
        super.onRequestToJoinReceived(groupId, groupName, applicant, reason)
        //移除相同的请求
        val allMessages = EaseSystemMsgManager.getInstance().allMessages
        if (allMessages != null && !allMessages.isEmpty()) {
            for (message in allMessages) {
                val ext = message.ext()
                if (ext != null && ext.containsKey(DemoConstant.SYSTEM_MESSAGE_GROUP_ID) && TextUtils.equals(
                        groupId,
                        ext[DemoConstant.SYSTEM_MESSAGE_GROUP_ID] as String?
                    )
                    && ext.containsKey(DemoConstant.SYSTEM_MESSAGE_FROM) && TextUtils.equals(
                        applicant,
                        ext[DemoConstant.SYSTEM_MESSAGE_FROM] as String?
                    )
                ) {
                    EaseSystemMsgManager.getInstance().removeMessage(message)
                }
            }
        }
        // user apply to join group
        val ext = EaseSystemMsgManager.getInstance().createMsgExt()
        ext[DemoConstant.SYSTEM_MESSAGE_FROM] = applicant
        ext[DemoConstant.SYSTEM_MESSAGE_GROUP_ID] = groupId
        ext[DemoConstant.SYSTEM_MESSAGE_REASON] = reason
        ext[DemoConstant.SYSTEM_MESSAGE_NAME] = groupName
        ext[DemoConstant.SYSTEM_MESSAGE_STATUS] = InviteMessageStatus.BEAPPLYED.name
        val message =
            EaseSystemMsgManager.getInstance()
                .createMessage(PushAndMessageHelper.getSystemMessage(ext), ext)
        notifyNewInviteMessage(message)
        val event =
            EaseEvent.create(DemoConstant.NOTIFY_GROUP_JOIN_RECEIVE, EaseEvent.TYPE.NOTIFY)
        LiveDataBus.get().use(DemoConstant.NOTIFY_CHANGE).postValue(event)
        AppClient.instance.toast(
            AppClient.instance.getString(
                InviteMessageStatus.BEAPPLYED.msgContent,
                applicant,
                groupName
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(
                InviteMessageStatus.BEAPPLYED.msgContent,
                applicant,
                groupName
            )
        )
    }

    override fun onRequestToJoinAccepted(groupId: String, groupName: String, accepter: String) {
        super.onRequestToJoinAccepted(groupId, groupName, accepter)
        // your application was accepted
        val msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT)
        msg.chatType = EMMessage.ChatType.GroupChat
        msg.from = accepter
        msg.to = groupId
        msg.msgId = UUID.randomUUID().toString()
        msg.setAttribute(DemoConstant.EM_NOTIFICATION_TYPE, true)
        msg.addBody(
            EMTextMessageBody(
                AppClient.instance.getString(
                    R.string.demo_group_listener_onRequestToJoinAccepted,
                    accepter,
                    groupName
                )
            )
        )
        msg.setStatus(EMMessage.Status.SUCCESS)
        // save accept message
        EMClient.getInstance().chatManager().saveMessage(msg)
        // notify the accept message
        notifier.vibrateAndPlayTone(msg)
        val event =
            EaseEvent.create(DemoConstant.MESSAGE_GROUP_JOIN_ACCEPTED, EaseEvent.TYPE.MESSAGE)
        LiveDataBus.get().use(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event)
        AppClient.instance.toast(
            AppClient.instance.getString(
                R.string.demo_group_listener_onRequestToJoinAccepted,
                accepter,
                groupName
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(
                R.string.demo_group_listener_onRequestToJoinAccepted,
                accepter,
                groupName
            )
        )
    }

    override fun onRequestToJoinDeclined(
        groupId: String,
        groupName: String,
        decliner: String,
        reason: String
    ) {
        super.onRequestToJoinDeclined(groupId, groupName, decliner, reason)
        AppClient.instance.toast(
            AppClient.instance.getString(
                R.string.demo_group_listener_onRequestToJoinDeclined,
                decliner,
                groupName
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(
                R.string.demo_group_listener_onRequestToJoinDeclined,
                decliner,
                groupName
            )
        )
    }

    override fun onAutoAcceptInvitationFromGroup(
        groupId: String,
        inviter: String,
        inviteMessage: String
    ) {
        super.onAutoAcceptInvitationFromGroup(groupId, inviter, inviteMessage)
        val groupName = GroupHelper.getGroupName(groupId)
        val msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT)
        msg.chatType = EMMessage.ChatType.GroupChat
        msg.from = inviter
        msg.to = groupId
        msg.msgId = UUID.randomUUID().toString()
        msg.setAttribute(DemoConstant.EM_NOTIFICATION_TYPE, true)
        msg.addBody(
            EMTextMessageBody(
                AppClient.instance.getString(
                    R.string.demo_group_listener_onAutoAcceptInvitationFromGroup,
                    groupName
                )
            )
        )
        msg.setStatus(EMMessage.Status.SUCCESS)
        // save invitation as messages
        EMClient.getInstance().chatManager().saveMessage(msg)
        // notify invitation message
        notifier.vibrateAndPlayTone(msg)
        val event =
            EaseEvent.create(DemoConstant.MESSAGE_GROUP_AUTO_ACCEPT, EaseEvent.TYPE.MESSAGE)
        LiveDataBus.get().use(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event)
        AppClient.instance.toast(
            AppClient.instance.getString(
                R.string.demo_group_listener_onAutoAcceptInvitationFromGroup,
                groupName
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(
                R.string.demo_group_listener_onAutoAcceptInvitationFromGroup,
                groupName
            )
        )
    }

    override fun onMuteListAdded(groupId: String, mutes: List<String>, muteExpire: Long) {
        super.onMuteListAdded(groupId, mutes, muteExpire)
        val content = getContentFromList(mutes)
        AppClient.instance.toast(
            AppClient.instance.getString(
                R.string.demo_group_listener_onMuteListAdded,
                content
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(R.string.demo_group_listener_onMuteListAdded, content)
        )
    }

    override fun onMuteListRemoved(groupId: String, mutes: List<String>) {
        super.onMuteListRemoved(groupId, mutes)
        val content = getContentFromList(mutes)
        AppClient.instance.toast(
            AppClient.instance.getString(
                R.string.demo_group_listener_onMuteListRemoved,
                content
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(R.string.demo_group_listener_onMuteListRemoved, content)
        )
    }

    override fun onWhiteListAdded(groupId: String, whitelist: List<String>) {
        val easeEvent = EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP)
        easeEvent.message = groupId
        LiveDataBus.get().use(DemoConstant.GROUP_CHANGE).postValue(easeEvent)
        val content = getContentFromList(whitelist)
        AppClient.instance.toast(
            AppClient.instance.getString(
                R.string.demo_group_listener_onWhiteListAdded,
                content
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(R.string.demo_group_listener_onWhiteListAdded, content)
        )
    }

    override fun onWhiteListRemoved(groupId: String, whitelist: List<String>) {
        val easeEvent = EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP)
        easeEvent.message = groupId
        LiveDataBus.get().use(DemoConstant.GROUP_CHANGE).postValue(easeEvent)
        val content = getContentFromList(whitelist)
        AppClient.instance.toast(
            AppClient.instance.getString(
                R.string.demo_group_listener_onWhiteListRemoved,
                content
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(R.string.demo_group_listener_onWhiteListRemoved, content)
        )
    }

    override fun onAllMemberMuteStateChanged(groupId: String, isMuted: Boolean) {
        val easeEvent = EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP)
        easeEvent.message = groupId
        LiveDataBus.get().use(DemoConstant.GROUP_CHANGE).postValue(easeEvent)
        AppClient.instance.toast(AppClient.instance.getString(if (isMuted) R.string.demo_group_listener_onAllMemberMuteStateChanged_mute else R.string.demo_group_listener_onAllMemberMuteStateChanged_not_mute))
        EMLog.i(
            TAG,
            AppClient.instance.getString(if (isMuted) R.string.demo_group_listener_onAllMemberMuteStateChanged_mute else R.string.demo_group_listener_onAllMemberMuteStateChanged_not_mute)
        )
    }

    override fun onAdminAdded(groupId: String, administrator: String) {
        super.onAdminAdded(groupId, administrator)
        LiveDataBus.get().use(DemoConstant.GROUP_CHANGE)
            .postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP))
        AppClient.instance.toast(
            AppClient.instance.getString(
                R.string.demo_group_listener_onAdminAdded,
                administrator
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(R.string.demo_group_listener_onAdminAdded, administrator)
        )
    }

    override fun onAdminRemoved(groupId: String, administrator: String) {
        LiveDataBus.get().use(DemoConstant.GROUP_CHANGE)
            .postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP))
        AppClient.instance.toast(
            AppClient.instance.getString(
                R.string.demo_group_listener_onAdminRemoved,
                administrator
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(R.string.demo_group_listener_onAdminRemoved, administrator)
        )
    }

    override fun onOwnerChanged(groupId: String, newOwner: String, oldOwner: String) {
        LiveDataBus.get().use(DemoConstant.GROUP_CHANGE).postValue(
            EaseEvent.create(
                DemoConstant.GROUP_OWNER_TRANSFER,
                EaseEvent.TYPE.GROUP
            )
        )
        AppClient.instance.toast(
            AppClient.instance.getString(
                R.string.demo_group_listener_onOwnerChanged,
                oldOwner,
                newOwner
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(
                R.string.demo_group_listener_onOwnerChanged,
                oldOwner,
                newOwner
            )
        )
    }

    override fun onMemberJoined(groupId: String, member: String) {
        LiveDataBus.get().use(DemoConstant.GROUP_CHANGE)
            .postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP))
        AppClient.instance.toast(
            AppClient.instance.getString(
                R.string.demo_group_listener_onMemberJoined,
                member
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(R.string.demo_group_listener_onMemberJoined, member)
        )
    }

    override fun onMemberExited(groupId: String, member: String) {
        LiveDataBus.get().use(DemoConstant.GROUP_CHANGE)
            .postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP))
        AppClient.instance.toast(
            AppClient.instance.getString(
                R.string.demo_group_listener_onMemberExited,
                member
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(R.string.demo_group_listener_onMemberExited, member)
        )
    }

    override fun onAnnouncementChanged(groupId: String, announcement: String) {
        AppClient.instance.toast(AppClient.instance.getString(R.string.demo_group_listener_onAnnouncementChanged))
        EMLog.i(
            TAG,
            AppClient.instance.getString(R.string.demo_group_listener_onAnnouncementChanged)
        )
    }

    override fun onSharedFileAdded(groupId: String, sharedFile: EMMucSharedFile) {
        LiveDataBus.get().use(DemoConstant.GROUP_SHARE_FILE_CHANGE).postValue(
            EaseEvent.create(
                DemoConstant.GROUP_SHARE_FILE_CHANGE,
                EaseEvent.TYPE.GROUP
            )
        )
        AppClient.instance.toast(
            AppClient.instance.getString(
                R.string.demo_group_listener_onSharedFileAdded,
                sharedFile.fileName
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(
                R.string.demo_group_listener_onSharedFileAdded,
                sharedFile.fileName
            )
        )
    }

    override fun onSharedFileDeleted(groupId: String, fileId: String) {
        LiveDataBus.get().use(DemoConstant.GROUP_SHARE_FILE_CHANGE).postValue(
            EaseEvent.create(
                DemoConstant.GROUP_SHARE_FILE_CHANGE,
                EaseEvent.TYPE.GROUP
            )
        )
        AppClient.instance.toast(
            AppClient.instance.getString(
                R.string.demo_group_listener_onSharedFileDeleted,
                fileId
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(R.string.demo_group_listener_onSharedFileDeleted, fileId)
        )

    }

    private fun notifyNewInviteMessage(msg: EMMessage?) {
        // notify there is new message
        notifier.vibrateAndPlayTone(null)
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
}
