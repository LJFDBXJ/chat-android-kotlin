package com.hyphenate.easeim.section.ui.chat.presenter

import android.text.TextUtils
import com.hyphenate.EMContactListener
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMTextMessageBody
import com.hyphenate.chat.EMUserInfo
import com.hyphenate.easeim.AppClient
import com.hyphenate.easeim.R
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.db.entity.EmUserEntity
import com.hyphenate.easeim.common.db.entity.InviteMessageStatus
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.manager.PushAndMessageHelper
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeui.manager.EaseSystemMsgManager
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.model.EaseNotifier
import com.hyphenate.util.EMLog

class ChatContactListener(private val notifier: EaseNotifier) : EMContactListener {
    private val TAG = "ChatContactListener"
    override fun onContactAdded(username: String) {
        val requestUserIds=arrayOf(username)
        EMClient.getInstance().userInfoManager()
            .fetchUserInfoByUserId(requestUserIds, object : EMValueCallBack<Map<String?, EMUserInfo?>> {
                override fun onSuccess(value: Map<String?, EMUserInfo?>) {
                    val userInfo = value[username]
                    val entity = EmUserEntity()
                    entity.username = username
                    if (userInfo != null) {
                        entity.nickname = userInfo.nickName
                        entity.email = userInfo.email
                        entity.avatar = userInfo.avatarUrl
                        entity.birth = userInfo.birth
                        entity.gender = userInfo.gender
                        entity.ext = userInfo.ext
                        entity.contact = 0
                        entity.sign = userInfo.signature
                    }
                    SpDbModel.instance.insert(entity)
                    SdkHelper.instance.updateContactList()
                    val event =
                        EaseEvent.create(DemoConstant.CONTACT_ADD, EaseEvent.TYPE.CONTACT)
                    event.message = username
                    LiveDataBus.get().use(DemoConstant.CONTACT_ADD).postValue(event)
                    AppClient.instance.toast(
                        AppClient.instance.getString(
                            R.string.demo_contact_listener_onContactAdded,
                            username
                        )
                    )
                    EMLog.i(
                        TAG,
                        AppClient.instance.getString(
                            R.string.demo_contact_listener_onContactAdded,
                            username
                        )
                    )
                }

                override fun onError(error: Int, errorMsg: String) {
                    EMLog.i(
                        TAG,
                        AppClient.instance.getString(R.string.demo_contact_get_userInfo_failed) + username + "error:" + error + " errorMsg:" + errorMsg
                    )
                }
            })
    }

    override fun onContactDeleted(username: String) {
        EMLog.i("ChatContactListener", "onContactDeleted")
        val deleteUsername = SpDbModel.instance.isDeleteUsername(username = username)
        val num = SdkHelper.instance.deleteContact(username = username)
        SdkHelper.instance.updateContactList()
        val event = EaseEvent.create(DemoConstant.CONTACT_DELETE, EaseEvent.TYPE.CONTACT)
        event.message = username
        LiveDataBus.get().use(DemoConstant.CONTACT_DELETE).postValue(event)
        if (deleteUsername || num == 0) {
            AppClient.instance.toast(
                AppClient.instance.getString(
                    R.string.demo_contact_listener_onContactDeleted,
                    username
                )
            )
            EMLog.i(
                TAG,
                AppClient.instance.getString(
                    R.string.demo_contact_listener_onContactDeleted,
                    username
                )
            )
        } else {
            //showToast(AppClient.instance.getString(R.string.demo_contact_listener_onContactDeleted_by_other, username));
            EMLog.i(
                TAG,
                AppClient.instance.getString(
                    R.string.demo_contact_listener_onContactDeleted_by_other,
                    username
                )
            )
        }
    }

    override fun onContactInvited(username: String, reason: String) {
        EMLog.i("ChatContactListener", "onContactInvited")
        val allMessages = EaseSystemMsgManager.getInstance().allMessages
        if (!allMessages.isNullOrEmpty()) {
            for (message in allMessages) {
                val ext = message.ext()
                if (ext != null && !ext.containsKey(DemoConstant.SYSTEM_MESSAGE_GROUP_ID)
                    && ext.containsKey(DemoConstant.SYSTEM_MESSAGE_FROM) && TextUtils.equals(
                        username,
                        ext[DemoConstant.SYSTEM_MESSAGE_FROM] as String?
                    )
                ) {
                    EaseSystemMsgManager.getInstance().removeMessage(message)
                }
            }
        }
        val ext = EaseSystemMsgManager.getInstance().createMsgExt()
        ext[DemoConstant.SYSTEM_MESSAGE_FROM] = username
        ext[DemoConstant.SYSTEM_MESSAGE_REASON] = reason
        ext[DemoConstant.SYSTEM_MESSAGE_STATUS] = InviteMessageStatus.BEINVITEED.name
        val message =
            EaseSystemMsgManager.getInstance().createMessage(
                PushAndMessageHelper.getSystemMessage(
                    ext
                ), ext
            )
        notifyNewInviteMessage(message)
        val event = EaseEvent.create(DemoConstant.CONTACT_CHANGE, EaseEvent.TYPE.CONTACT)
        LiveDataBus.get().use(DemoConstant.CONTACT_CHANGE).postValue(event)
        AppClient.instance.toast(
            AppClient.instance.getString(
                InviteMessageStatus.BEINVITEED.msgContent,
                username
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(InviteMessageStatus.BEINVITEED.msgContent, username)
        )
    }

    override fun onFriendRequestAccepted(username: String) {
        EMLog.i("ChatContactListener", "onFriendRequestAccepted")
        val allMessages = EaseSystemMsgManager.getInstance().allMessages
        if (allMessages != null && !allMessages.isEmpty()) {
            for (message in allMessages) {
                val ext = message.ext()
                if (ext != null && (ext.containsKey(DemoConstant.SYSTEM_MESSAGE_FROM)
                            && TextUtils.equals(
                        username,
                        ext[DemoConstant.SYSTEM_MESSAGE_FROM] as String?
                    ))
                ) {
                    updateMessage(message)
                    return
                }
            }
        }
        val ext = EaseSystemMsgManager.getInstance().createMsgExt()
        ext[DemoConstant.SYSTEM_MESSAGE_FROM] = username
        ext[DemoConstant.SYSTEM_MESSAGE_STATUS] = InviteMessageStatus.BEAGREED.name
        val message =
            EaseSystemMsgManager.getInstance().createMessage(
                PushAndMessageHelper.getSystemMessage(
                    ext
                ), ext
            )
        notifyNewInviteMessage(message)
        val event = EaseEvent.create(DemoConstant.CONTACT_CHANGE, EaseEvent.TYPE.CONTACT)
        LiveDataBus.get().use(DemoConstant.CONTACT_CHANGE).postValue(event)
        AppClient.instance.toast(AppClient.instance.getString(InviteMessageStatus.BEAGREED.msgContent))
        EMLog.i(
            TAG,
            AppClient.instance.getString(InviteMessageStatus.BEAGREED.msgContent)
        )
    }

    override fun onFriendRequestDeclined(username: String) {
        EMLog.i("ChatContactListener", "onFriendRequestDeclined")
        val ext = EaseSystemMsgManager.getInstance().createMsgExt()
        ext[DemoConstant.SYSTEM_MESSAGE_FROM] = username
        ext[DemoConstant.SYSTEM_MESSAGE_STATUS] = InviteMessageStatus.BEREFUSED.name
        val message =
            EaseSystemMsgManager.getInstance().createMessage(
                PushAndMessageHelper.getSystemMessage(
                    ext
                ), ext
            )
        notifyNewInviteMessage(message)
        val event = EaseEvent.create(DemoConstant.CONTACT_CHANGE, EaseEvent.TYPE.CONTACT)
        LiveDataBus.get().use(DemoConstant.CONTACT_CHANGE).postValue(event)
        AppClient.instance.toast(
            AppClient.instance.getString(
                InviteMessageStatus.BEREFUSED.msgContent,
                username
            )
        )
        EMLog.i(
            TAG,
            AppClient.instance.getString(InviteMessageStatus.BEREFUSED.msgContent, username)
        )
    }

    private fun notifyNewInviteMessage(msg: EMMessage?) {
        // notify there is new message
        notifier.vibrateAndPlayTone(null)
    }

    private fun updateMessage(message: EMMessage) {
        message.setAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.BEAGREED.name)
        val body = EMTextMessageBody(PushAndMessageHelper.getSystemMessage(message.ext()))
        message.addBody(body)
        EaseSystemMsgManager.getInstance().updateMessage(message)
    }

}