package com.hyphenate.easeim.common.repositories

import android.text.TextUtils
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import com.hyphenate.easeim.common.db.entity.InviteMessage
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.net.ErrorCode
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.hyphenate.exceptions.HyphenateException
import java.util.*
import kotlin.collections.ArrayList

/**
 * 处理与chat相关的逻辑
 */
class EMChatManagerRepository : BaseEMRepository() {
    /**
     * 获取会话列表
     * @return
     */
    fun loadConversationList(result: ResultCallBack<List<Any>>) {
        val emConversations = loadConversationListFromCache()
        result.onSuccess(emConversations)
    }

    /**
     * load conversation list
     *
     * @return
     * +
     */
    fun loadConversationListFromCache(): List<Any> {
        // get all conversations
        val conversations = chatManager.allConversations
        val sortList = ArrayList<Pair<Long, Any>>()
        val topSortList = ArrayList<Pair<Long, Any>>()
        /*
         * lastMsgTime will change if there is new message during sorting
         * so use synchronized to make sure timestamp of last message won't change.
         */
        synchronized(conversations) {
            for (conversation in conversations.values) {
                if (conversation.allMessages.size != 0) {
                    val extField = conversation.extField
                    if (!TextUtils.isEmpty(extField) && EaseCommonUtils.isTimestamp(extField)) {
                        topSortList.add(Pair(java.lang.Long.valueOf(extField), conversation))
                    } else {
                        sortList.add(Pair(conversation.lastMessage.msgTime, conversation))
                    }
                }
            }
        }
        val manageEntities = msgTypeManageDao?.loadAllMsgTypeManage()
        if (!manageEntities.isNullOrEmpty()) {
            synchronized(EMChatManagerRepository::class.java) {
                for (manage in manageEntities) {
                    val extField = manage!!.extField
                    if (!extField.isNullOrEmpty() && EaseCommonUtils.isTimestamp(extField)) {
                        topSortList.add(extField.toLong() to manage)
                    } else {
                        val lastMsg = manage.lastMsg
                        if (lastMsg is InviteMessage) {
                            val time = lastMsg.time
                            sortList.add(Pair(time, manage))
                        }
                    }
                }
            }
        }
        try {
            // Internal is TimSort algorithm, has bug
            if (topSortList.size > 0) {
                sortConversationByLastChatTime(topSortList)
            }
            sortConversationByLastChatTime(sortList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        sortList.addAll(0, topSortList)
        val list: MutableList<Any> = ArrayList()
        for (sortItem in sortList) {
            list.add(sortItem.second)
        }
        return list
    }

    /**
     * sort conversations according time stamp of last message
     *
     * @param conversationList
     */
    private fun sortConversationByLastChatTime(conversationList: List<Pair<Long, Any>>) {
        Collections.sort(conversationList) { con1, con2 ->
            if (con1.first == con2.first) {
                0
            } else if (con2.first.toLong() > con1.first.toLong()) {
                1
            } else {
                -1
            }
        }
    }

    fun deleteConversationById(conversationId: String, callBack: EMValueCallBack<Boolean>) {
        val isDelete = chatManager.deleteConversation(conversationId, true)
        if (isDelete) {
            callBack.onSuccess(true)
        } else {
            callBack.onError(ErrorCode.EM_DELETE_CONVERSATION_ERROR, "删除失败")
        }
    }

    /**
     * 将会话置为已读
     * @param conversationId
     * @return
     */
    fun makeConversationRead(conversationId: String, callBack: ResultCallBack<Boolean>) {
        val conversation = chatManager.getConversation(conversationId)
        if (conversation == null) {
            callBack.onError(ErrorCode.EM_DELETE_CONVERSATION_ERROR)
        } else {
            conversation.markAllMessagesAsRead()
            callBack.onSuccess(true)
        }
    }

    /**
     * 获取会话列表
     * @return
     */
    fun fetchConversationsFromServer(callBack: ResultCallBack<List<EaseConversationInfo>>) {
        EMClient.getInstance().chatManager().asyncFetchConversationsFromServer(object :
            EMValueCallBack<Map<String?, EMConversation>> {
            override fun onSuccess(value: Map<String?, EMConversation>) {
                val conversations = ArrayList(value.values)
                val infoList = ArrayList<EaseConversationInfo>()
                if (conversations.isNotEmpty()) {
                    conversations.forEach { conversation ->
                        val info = EaseConversationInfo()
                        info.info = conversation
                        info.timestamp = conversation.lastMessage.msgTime
                        infoList.add(info)
                    }
                }
                callBack.onSuccess(infoList)
            }

            override fun onError(error: Int, errorMsg: String) {
                callBack.onError(error, errorMsg)
            }
        })
    }

    /**
     * 调用api请求将会话置为已读
     * @param conversationId
     * @return
     */
    fun makeConversationReadByAck(conversationId: String?, callBack: EMValueCallBack<Boolean>) {
        try {
            chatManager.ackConversationRead(conversationId)
            callBack.onSuccess(true)
        } catch (e: HyphenateException) {
            e.printStackTrace()
            callBack.onError(e.errorCode, e.description)
        }
    }

    /**
     * 设置单聊用户聊天免打扰
     *
     * @param userId 用户名
     * @param noPush 是否免打扰
     */
    fun setUserNotDisturb(userId: String, noPush: Boolean, callBack: EMValueCallBack<Boolean>) {
        runOnIOThread {
            val onPushList: MutableList<String> = ArrayList()
            onPushList.add(userId)
            try {
                pushManager.updatePushServiceForUsers(onPushList, noPush)
                callBack.onSuccess(true)
            } catch (e: HyphenateException) {
                e.printStackTrace()
                callBack.onError(e.errorCode, e.description)
            }
        }
    }

    /**
     * 获取聊天免打扰用户
     */
    fun noPushUsers(action: Function1<List<String>, Unit>) {
        runOnIOThread {
            val noPushUsers = pushManager.noPushUsers
            if (noPushUsers != null && noPushUsers.size != 0) {
                action.invoke(noPushUsers)
            }
        }
    }

}