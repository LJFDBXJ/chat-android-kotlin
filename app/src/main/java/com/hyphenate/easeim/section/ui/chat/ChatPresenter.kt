package com.hyphenate.easeim.section.ui.chat

import android.app.Activity
import android.text.TextUtils
import com.hyphenate.chat.*
import com.hyphenate.easeim.AppClient
import com.hyphenate.easeim.MainActivity
import com.hyphenate.easeim.R
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.section.ui.chat.presenter.*
import com.hyphenate.easeui.manager.EaseAtMessageHelper
import com.hyphenate.easeui.manager.EaseChatPresenter
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.util.EMLog
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 主要用于chat过程中的全局监听，并对相应的事件进行处理
 * [.init]方法建议在登录成功以后进行调用
 */
class ChatPresenter private constructor() : EaseChatPresenter() {
    private var isGroupsSyncedWithServer = false
    private var isContactsSyncedWithServer = false
    private var isBlackListSyncedWithServer = false
    private var isPushConfigsWithServer = false
    private val connectionListener = ChatConnectionListener(
        isGroupsSyncedWithServer,
        isContactsSyncedWithServer,
        isBlackListSyncedWithServer,
        isPushConfigsWithServer
    )
    private val multiDeviceListener = ChatMultiDeviceListener(notifier)
    private val groupListener = ChatGroupListener(notifier)
    private val contactListener = ChatContactListener(notifier)
    private val chatRoomListener = ChatRoomListener()
    private val conversationListener = ChatConversationListener()

    /**
     * 将需要登录成功进入MainActivity中初始化的逻辑，放到此处进行处理
     */
    fun init() {}
    fun clear() {
        SdkHelper.instance.eMClient.removeConnectionListener(connectionListener)
        SdkHelper.instance.eMClient.removeMultiDeviceListener(multiDeviceListener)
        SdkHelper.instance.groupManager.removeGroupChangeListener(groupListener)
        SdkHelper.instance.contactManager.removeContactListener(contactListener)
        SdkHelper.instance.chatroomManager.removeChatRoomListener(chatRoomListener)
        SdkHelper.instance.chatManager.removeConversationListener(conversationListener)
        instance = null
    }

    override fun onMessageReceived(messages: List<EMMessage>) {
        super.onMessageReceived(messages)
        val event = EaseEvent.create(DemoConstant.MESSAGE_CHANGE_RECEIVE, EaseEvent.TYPE.MESSAGE)
        LiveDataBus.get().use(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event)
        for (message in messages) {
            EMLog.d(TAG, "onMessageReceived id : " + message.msgId)
            EMLog.d(TAG, "onMessageReceived: " + message.type)
            // 如果设置群组离线消息免打扰，则不进行消息通知
            val disabledIds = SdkHelper.instance.pushManager.noPushGroups
            if (disabledIds != null && disabledIds.contains(message.conversationId())) {
                return
            }
            // in background, do not refresh UI, notify it in notification bar
            if (!AppClient.instance.lifecycleCallbacks.isFront) {
                notifier.notify(message)
            }
            //notify new message
            notifier.vibrateAndPlayTone(message)
        }
    }

    /**
     * 判断是否已经启动了MainActivity
     * @return
     */
    @get:Synchronized
    private val isAppLaunchMain: Boolean
        get() {
            val activities: List<Activity> =
                AppClient.instance.lifecycleCallbacks.activityList
            if (activities.isNotEmpty()) {
                for (i in activities.indices.reversed()) {
                    if (activities[i] is MainActivity) {
                        return true
                    }
                }
            }
            return false
        }

    override fun onCmdMessageReceived(messages: List<EMMessage>) {
        super.onCmdMessageReceived(messages)
        val event =
            EaseEvent.create(DemoConstant.MESSAGE_CHANGE_CMD_RECEIVE, EaseEvent.TYPE.MESSAGE)
        LiveDataBus.get().use(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event)
    }

    override fun onMessageRead(messages: List<EMMessage>) {
        super.onMessageRead(messages)
        if (AppClient.instance.lifecycleCallbacks.current() !is ChatActivity) {
            val event = EaseEvent.create(DemoConstant.MESSAGE_CHANGE_RECALL, EaseEvent.TYPE.MESSAGE)
            LiveDataBus.get().use(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event)
        }
    }

    override fun onMessageRecalled(messages: List<EMMessage>) {

        for (msg in messages) {
            if (msg.chatType == EMMessage.ChatType.GroupChat && EaseAtMessageHelper.get()
                    .isAtMeMsg(msg)
            ) {
                EaseAtMessageHelper.get().removeAtMeGroup(msg.to)
            }
            val msgNotification = EMMessage.createReceiveMessage(EMMessage.Type.TXT)
            var text: String? = null
            val recaller = msg.recaller
            val from = msg.from
            text = if (!TextUtils.isEmpty(recaller) && !TextUtils.equals(recaller, from)) {
                String.format(context.getString(R.string.msg_recall_by_another), recaller, from)
            } else {
                String.format(context.getString(R.string.msg_recall_by_user), from)
            }
            val txtBody = EMTextMessageBody(text)
            msgNotification.addBody(txtBody)
            msgNotification.setDirection(msg.direct())
            msgNotification.from = msg.from
            msgNotification.to = msg.to
            msgNotification.isUnread = false
            msgNotification.msgTime = msg.msgTime
            msgNotification.setLocalTime(msg.msgTime)
            msgNotification.chatType = msg.chatType
            msgNotification.setAttribute(DemoConstant.MESSAGE_TYPE_RECALL, true)
            msgNotification.setAttribute(DemoConstant.MESSAGE_TYPE_RECALLER, recaller)
            msgNotification.setStatus(EMMessage.Status.SUCCESS)
            EMClient.getInstance().chatManager().saveMessage(msgNotification)
        }

        val event = EaseEvent.create(DemoConstant.MESSAGE_CHANGE_RECALL, EaseEvent.TYPE.MESSAGE)
        LiveDataBus.get().use(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event)
    }


    companion object {
        private val TAG = ChatPresenter::class.java.simpleName
        private var instance: ChatPresenter? = null
        fun getInstance(): ChatPresenter {
            if (instance == null) {
                synchronized(ChatPresenter::class.java) {
                    if (instance == null) {
                        instance = ChatPresenter()
                    }
                }
            }
            return instance!!
        }
    }

    init {
        //添加网络连接状态监听
        SdkHelper.instance.eMClient.addConnectionListener(connectionListener)
        //添加多端登录监听
        SdkHelper.instance.eMClient.addMultiDeviceListener(multiDeviceListener)
        //添加群组监听
        SdkHelper.instance.groupManager.addGroupChangeListener(groupListener)
        //添加联系人监听
        SdkHelper.instance.contactManager.setContactListener(contactListener)
        //添加聊天室监听
        SdkHelper.instance.chatroomManager.addChatRoomChangeListener(chatRoomListener)
        //添加对会话的监听（监听已读回执）
        SdkHelper.instance.chatManager.addConversationListener(conversationListener)
    }
}