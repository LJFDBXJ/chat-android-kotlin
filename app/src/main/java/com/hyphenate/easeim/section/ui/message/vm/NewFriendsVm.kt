package com.hyphenate.easeim.section.ui.message.vm

import android.app.Application
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import androidx.lifecycle.AndroidViewModel
import com.hyphenate.chat.EMMessage
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.hyphenate.chat.EMClient
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.chat.EMConversation
import com.hyphenate.easeui.manager.EaseThreadManager
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.db.entity.InviteMessageStatus
import com.hyphenate.easeim.R
import com.hyphenate.chat.EMTextMessageBody
import com.hyphenate.easeui.manager.EaseSystemMsgManager
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.exceptions.HyphenateException

class NewFriendsVm(application: Application) : AndroidViewModel(application) {

    val inviteMsg: LiveData<List<EMMessage>> get() = _inviteMsg
    private val _inviteMsg = MutableLiveData<List<EMMessage>>()


    val moreInviteMsg: LiveData<List<EMMessage>> get() = _moreInviteMsg
    private val _moreInviteMsg = MutableLiveData<List<EMMessage>>()

    val deleteResult: LiveData<Boolean> get() = _deleteResult
    private val _deleteResult = MutableLiveData<Boolean>()

    val agreeResult: LiveData<String> get() = _agreeResult
    private val _agreeResult = MutableLiveData<String>()

    val refuseObservable: LiveData<String> get() = _refuseObservable
    private val _refuseObservable = MutableLiveData<String>()

    fun loadMessages(limit: Int) {
        val emMessages = EMClient.getInstance().chatManager().searchMsgFromDB(
            EMMessage.Type.TXT,
            System.currentTimeMillis(),
            limit,
            EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID,
            EMConversation.EMSearchDirection.UP
        )
        sortData(emMessages)
        _inviteMsg.postValue(emMessages)
    }

    fun loadMoreMessages(targetId: String?, limit: Int) {
        val conversation = EMClient.getInstance().chatManager().getConversation(
            EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID,
            EMConversation.EMConversationType.Chat,
            true
        )
        val messages = conversation.loadMoreMsgFromDB(targetId, limit)
        sortData(messages)
        _moreInviteMsg.postValue(messages)
    }

    private fun sortData(messages: List<EMMessage>) {
        messages.sortedBy {
            it.msgTime
        }
    }

    fun agreeInvite(emMessage: EMMessage) {
        EaseThreadManager.getInstance().runOnIOThread {
            try {
                val statusParams = emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS)
                val status = InviteMessageStatus.valueOf(statusParams)
                var message = ""
                when (status) {
                    InviteMessageStatus.BEINVITEED -> {//接受成为朋友
                        message = getApplication<Application>().getString(
                            R.string.demo_system_agree_invite,
                            emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM)
                        )
                        EMClient.getInstance().contactManager()
                            .acceptInvitation(emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM))
                    }
                    InviteMessageStatus.BEAPPLYED -> {
                        //accept application to join group
                        message = getApplication<Application>().getString(
                            R.string.demo_system_agree_remote_user_apply_to_join_group,
                            emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM)
                        )
                        EMClient.getInstance().groupManager().acceptApplication(
                            emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM),
                            emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_GROUP_ID)
                        )
                    }
                    InviteMessageStatus.GROUPINVITATION -> {
                        message = getApplication<Application>().getString(
                            R.string.demo_system_agree_received_remote_user_invitation,
                            emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_INVITER)
                        )
                        EMClient.getInstance().groupManager().acceptInvitation(
                            emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_GROUP_ID),
                            emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_INVITER)
                        )
                    }
                    else -> {

                    }
                }
                emMessage.setAttribute(
                    DemoConstant.SYSTEM_MESSAGE_STATUS,
                    InviteMessageStatus.AGREED.name
                )
                emMessage.setAttribute(DemoConstant.SYSTEM_MESSAGE_REASON, message)

                val body = EMTextMessageBody(message)
                emMessage.body = body
                EaseSystemMsgManager.getInstance().updateMessage(emMessage)

                _agreeResult.postValue(message)

                LiveDataBus.get().use(DemoConstant.NOTIFY_CHANGE)
                    .postValue(EaseEvent.create(DemoConstant.NOTIFY_CHANGE, EaseEvent.TYPE.NOTIFY))
            } catch (e: HyphenateException) {
                e.printStackTrace()
                val result = "e.errorCode + e.message "
                _agreeResult.postValue(result)
            }
        }
    }

    fun refuseInvite(emMessage: EMMessage) {
        EaseThreadManager.getInstance().runOnIOThread {
            try {
                val statusParams = emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS)
                val status = InviteMessageStatus.valueOf(statusParams)
                var message = ""
                if (status === InviteMessageStatus.BEINVITEED) { //decline the invitation
                    message = getApplication<Application>().getString(
                        R.string.demo_system_decline_invite,
                        emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM)
                    )
                    EMClient.getInstance().contactManager()
                        .declineInvitation(emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM))
                } else if (status === InviteMessageStatus.BEAPPLYED) { //decline application to join group
                    message = getApplication<Application>().getString(
                        R.string.demo_system_decline_remote_user_apply_to_join_group,
                        emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM)
                    )
                    EMClient.getInstance().groupManager().declineApplication(
                        emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM),
                        emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_GROUP_ID),
                        ""
                    )
                } else if (status === InviteMessageStatus.GROUPINVITATION) {
                    message = getApplication<Application>().getString(
                        R.string.demo_system_decline_received_remote_user_invitation,
                        emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_INVITER)
                    )
                    EMClient.getInstance().groupManager().declineInvitation(
                        emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_GROUP_ID),
                        emMessage.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_INVITER),
                        ""
                    )
                }
                emMessage.setAttribute(
                    DemoConstant.SYSTEM_MESSAGE_STATUS,
                    InviteMessageStatus.REFUSED.name
                )
                emMessage.setAttribute(DemoConstant.SYSTEM_MESSAGE_REASON, message)
                val body = EMTextMessageBody(message)
                emMessage.body = body
                EaseSystemMsgManager.getInstance().updateMessage(emMessage)
                _refuseObservable.postValue(message)
                LiveDataBus.get().use(DemoConstant.NOTIFY_CHANGE)
                    .postValue(EaseEvent.create(DemoConstant.NOTIFY_CHANGE, EaseEvent.TYPE.NOTIFY))
            } catch (e: HyphenateException) {
                e.printStackTrace()
                val result = "${e.errorCode},${e.message}"
                _refuseObservable.postValue(result)
            }
        }
    }

    fun deleteMsg(emMessage: EMMessage) {
        val conversation = EMClient.getInstance().chatManager().getConversation(
            DemoConstant.DEFAULT_SYSTEM_MESSAGE_ID,
            EMConversation.EMConversationType.Chat,
            true
        )
        conversation.removeMessage(emMessage.msgId)
        _deleteResult.postValue(true)
    }

    fun makeAllMsgRead() {
        val conversation = EMClient.getInstance().chatManager().getConversation(
            DemoConstant.DEFAULT_SYSTEM_MESSAGE_ID,
            EMConversation.EMConversationType.Chat,
            true
        )
        conversation.markAllMessagesAsRead()
        LiveDataBus.get().use(DemoConstant.NOTIFY_CHANGE)
            .postValue(EaseEvent.create(DemoConstant.NOTIFY_CHANGE, EaseEvent.TYPE.NOTIFY))
    }

}