package com.hyphenate.easeim.common.manager

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.hyphenate.EMCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMImageMessageBody
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMTextMessageBody
import com.hyphenate.easeim.AppClient.Companion.instance
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.db.entity.InviteMessage
import com.hyphenate.easeim.common.db.entity.InviteMessageStatus
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.hyphenate.easeui.utils.EaseFileUtils
import com.hyphenate.exceptions.HyphenateException

/**
 * 用于处理推送及消息相关
 */
object PushAndMessageHelper {
    private const val isLock = false

    /**
     * 转发消息
     * @param toChatUsername
     * @param msgId
     */
    fun sendForwardMessage(toChatUsername: String, msgId: String?) {
        if (TextUtils.isEmpty(msgId)) {
            return
        }
        val message = SdkHelper.instance.chatManager.getMessage(msgId)
        when (message.type) {
            EMMessage.Type.TXT -> if (message.getBooleanAttribute(
                    EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION,
                    false
                )
            ) {
                sendBigExpressionMessage(
                    toChatUsername, (message.body as EMTextMessageBody).message,
                    message.getStringAttribute(EaseConstant.MESSAGE_ATTR_EXPRESSION_ID, null)
                )
            } else {
                // get the content and send it
                val content = (message.body as EMTextMessageBody).message
                sendTextMessage(toChatUsername, content)
            }
            EMMessage.Type.IMAGE -> {
                // send image
                val uri = getImageForwardUri(message.body as EMImageMessageBody)
                if (uri != null) {
                    sendImageMessage(toChatUsername, uri)
                } else {
                    LiveDataBus.get().use(DemoConstant.MESSAGE_FORWARD)
                        .postValue(
                            EaseEvent(
                                instance.applicationContext.getString(R.string.no_image_resource),
                                EaseEvent.TYPE.MESSAGE
                            )
                        )
                }
            }
            else -> {}
        }
    }

    fun getImageForwardUri(body: EMImageMessageBody?): Uri? {
        if (body == null) {
            return null
        }
        var localUri = body.localUri
        val context = instance.applicationContext
        if (EaseFileUtils.isFileExistByUri(context, localUri)) {
            return localUri
        }
        localUri = body.thumbnailLocalUri()
        return if (EaseFileUtils.isFileExistByUri(context, localUri)) {
            localUri
        } else null
    }

    /**
     * 获取系统消息内容
     * @param msg
     * @return
     */
    fun getSystemMessage(msg: InviteMessage): String {
        val status = msg.statusEnum ?: return ""
        val messge: String
        val context: Context = instance
        val builder = StringBuilder(context.getString(status.msgContent))
        messge = when (status) {
            InviteMessageStatus.BEINVITEED, InviteMessageStatus.AGREED, InviteMessageStatus.BEREFUSED -> String.format(
                builder.toString(),
                msg.from
            )
            InviteMessageStatus.BEAGREED -> builder.toString()
            InviteMessageStatus.BEAPPLYED, InviteMessageStatus.GROUPINVITATION -> String.format(
                builder.toString(),
                msg.from,
                msg.groupName
            )
            InviteMessageStatus.GROUPINVITATION_ACCEPTED, InviteMessageStatus.GROUPINVITATION_DECLINED, InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY_ACCEPT, InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY_DECLINE, InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE, InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE_ACCEPT, InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE_DECLINE, InviteMessageStatus.MULTI_DEVICE_GROUP_KICK, InviteMessageStatus.MULTI_DEVICE_GROUP_BAN, InviteMessageStatus.MULTI_DEVICE_GROUP_ALLOW, InviteMessageStatus.MULTI_DEVICE_GROUP_ASSIGN_OWNER, InviteMessageStatus.MULTI_DEVICE_GROUP_ADD_ADMIN, InviteMessageStatus.MULTI_DEVICE_GROUP_REMOVE_ADMIN, InviteMessageStatus.MULTI_DEVICE_GROUP_ADD_MUTE, InviteMessageStatus.MULTI_DEVICE_GROUP_REMOVE_MUTE -> String.format(
                builder.toString(),
                msg.groupInviter
            )
            InviteMessageStatus.MULTI_DEVICE_CONTACT_ADD, InviteMessageStatus.MULTI_DEVICE_CONTACT_BAN, InviteMessageStatus.MULTI_DEVICE_CONTACT_ALLOW, InviteMessageStatus.MULTI_DEVICE_CONTACT_ACCEPT, InviteMessageStatus.MULTI_DEVICE_CONTACT_DECLINE -> String.format(
                builder.toString(),
                msg.from
            )
            InviteMessageStatus.REFUSED, InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY -> builder.toString()
            else -> ""
        }
        return messge
    }

    /**
     * 获取系统消息内容
     * @param msg
     * @return
     */
    @Throws(HyphenateException::class)
    fun getSystemMessage(msg: EMMessage): String {
        val messageStatus = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS)
        if (TextUtils.isEmpty(messageStatus)) {
            return ""
        }
        val status = InviteMessageStatus.valueOf(messageStatus) ?: return ""
        val messge: String
        val context: Context = instance
        val builder = StringBuilder(context.getString(status.msgContent))
        messge = when (status) {
            InviteMessageStatus.BEINVITEED, InviteMessageStatus.AGREED, InviteMessageStatus.BEREFUSED -> String.format(
                builder.toString(),
                msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM)
            )
            InviteMessageStatus.BEAGREED, InviteMessageStatus.MULTI_DEVICE_GROUP_LEAVE -> builder.toString()
            InviteMessageStatus.BEAPPLYED, InviteMessageStatus.GROUPINVITATION -> {
                val name = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_NAME)
                String.format(
                    builder.toString(),
                    msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM),
                    name
                )
            }
            InviteMessageStatus.GROUPINVITATION_ACCEPTED, InviteMessageStatus.GROUPINVITATION_DECLINED, InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY_ACCEPT, InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY_DECLINE, InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE, InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE_ACCEPT, InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE_DECLINE, InviteMessageStatus.MULTI_DEVICE_GROUP_KICK, InviteMessageStatus.MULTI_DEVICE_GROUP_BAN, InviteMessageStatus.MULTI_DEVICE_GROUP_ALLOW, InviteMessageStatus.MULTI_DEVICE_GROUP_ASSIGN_OWNER, InviteMessageStatus.MULTI_DEVICE_GROUP_ADD_ADMIN, InviteMessageStatus.MULTI_DEVICE_GROUP_REMOVE_ADMIN, InviteMessageStatus.MULTI_DEVICE_GROUP_ADD_MUTE, InviteMessageStatus.MULTI_DEVICE_GROUP_REMOVE_MUTE -> String.format(
                builder.toString(),
                msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_INVITER)
            )
            InviteMessageStatus.MULTI_DEVICE_CONTACT_ADD, InviteMessageStatus.MULTI_DEVICE_CONTACT_BAN, InviteMessageStatus.MULTI_DEVICE_CONTACT_ALLOW, InviteMessageStatus.MULTI_DEVICE_CONTACT_ACCEPT, InviteMessageStatus.MULTI_DEVICE_CONTACT_DECLINE -> String.format(
                builder.toString(),
                msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM)
            )
            InviteMessageStatus.REFUSED, InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY -> builder.toString()
            else -> ""
        }
        return messge
    }

    /**
     * 获取系统消息内容
     * @param msg
     * @return
     */
    @JvmStatic
    @Throws(NullPointerException::class)
    fun getSystemMessage(msg: Map<String?, Any?>): String {
        val messageStatus = msg[DemoConstant.SYSTEM_MESSAGE_STATUS] as String?
        if (TextUtils.isEmpty(messageStatus)) {
            return ""
        }
        val status = InviteMessageStatus.valueOf(messageStatus!!) ?: return ""
        val messge: String
        val context: Context = instance
        val builder = StringBuilder(context.getString(status.msgContent))
        messge = when (status) {
            InviteMessageStatus.BEINVITEED, InviteMessageStatus.AGREED, InviteMessageStatus.BEREFUSED -> String.format(
                builder.toString(),
                msg[DemoConstant.SYSTEM_MESSAGE_FROM]
            )
            InviteMessageStatus.BEAGREED, InviteMessageStatus.MULTI_DEVICE_GROUP_LEAVE -> builder.toString()
            InviteMessageStatus.BEAPPLYED -> String.format(
                builder.toString(),
                msg[DemoConstant.SYSTEM_MESSAGE_FROM],
                msg[DemoConstant.SYSTEM_MESSAGE_NAME] as String?
            )
            InviteMessageStatus.GROUPINVITATION -> String.format(
                builder.toString(),
                msg[DemoConstant.SYSTEM_MESSAGE_INVITER],
                msg[DemoConstant.SYSTEM_MESSAGE_NAME] as String?
            )
            InviteMessageStatus.GROUPINVITATION_ACCEPTED, InviteMessageStatus.GROUPINVITATION_DECLINED, InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY_ACCEPT, InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY_DECLINE, InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE, InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE_ACCEPT, InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE_DECLINE, InviteMessageStatus.MULTI_DEVICE_GROUP_KICK, InviteMessageStatus.MULTI_DEVICE_GROUP_BAN, InviteMessageStatus.MULTI_DEVICE_GROUP_ALLOW, InviteMessageStatus.MULTI_DEVICE_GROUP_ASSIGN_OWNER, InviteMessageStatus.MULTI_DEVICE_GROUP_ADD_ADMIN, InviteMessageStatus.MULTI_DEVICE_GROUP_REMOVE_ADMIN, InviteMessageStatus.MULTI_DEVICE_GROUP_ADD_MUTE, InviteMessageStatus.MULTI_DEVICE_GROUP_REMOVE_MUTE -> String.format(
                builder.toString(),
                msg[DemoConstant.SYSTEM_MESSAGE_INVITER]
            )
            InviteMessageStatus.MULTI_DEVICE_CONTACT_ADD, InviteMessageStatus.MULTI_DEVICE_CONTACT_BAN, InviteMessageStatus.MULTI_DEVICE_CONTACT_ALLOW, InviteMessageStatus.MULTI_DEVICE_CONTACT_ACCEPT, InviteMessageStatus.MULTI_DEVICE_CONTACT_DECLINE -> String.format(
                builder.toString(),
                msg[DemoConstant.SYSTEM_MESSAGE_FROM]
            )
            InviteMessageStatus.REFUSED, InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY -> builder.toString()
            else -> ""
        }
        return messge
    }

    /**
     * send big expression message
     * @param toChatUsername
     * @param name
     * @param identityCode
     */
    private fun sendBigExpressionMessage(
        toChatUsername: String,
        name: String,
        identityCode: String
    ) {
        val message = EaseCommonUtils.createExpressionMessage(toChatUsername, name, identityCode)
        sendMessage(message)
    }

    /**
     * 发送文本消息
     * @param toChatUsername
     * @param content
     */
    private fun sendTextMessage(toChatUsername: String, content: String) {
        val message = EMMessage.createTxtSendMessage(content, toChatUsername)
        sendMessage(message)
    }

    /**
     * send image message
     * @param toChatUsername
     * @param imageUri
     */
    private fun sendImageMessage(toChatUsername: String, imageUri: Uri) {
        val message = EMMessage.createImageSendMessage(imageUri, false, toChatUsername)
        sendMessage(message)
    }

    /**
     * send image message
     * @param toChatUsername
     * @param imagePath
     */
    private fun sendImageMessage(toChatUsername: String, imagePath: String) {
        val message = EMMessage.createImageSendMessage(imagePath, false, toChatUsername)
        sendMessage(message)
    }

    /**
     * send message
     * @param message
     */
    private fun sendMessage(message: EMMessage) {
        message.setMessageStatusCallback(object : EMCallBack {
            override fun onSuccess() {
                LiveDataBus.get().use(DemoConstant.MESSAGE_FORWARD)
                    .postValue(
                        EaseEvent(
                            instance.getString(R.string.has_been_send),
                            EaseEvent.TYPE.MESSAGE
                        )
                    )
            }

            override fun onError(code: Int, error: String) {}
            override fun onProgress(progress: Int, status: String) {}
        })
        // send message
        EMClient.getInstance().chatManager().sendMessage(message)
    }
}