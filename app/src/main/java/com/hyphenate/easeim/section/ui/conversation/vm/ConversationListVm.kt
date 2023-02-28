package com.hyphenate.easeim.section.ui.conversation.vm

import com.hyphenate.easeim.common.repositories.EMChatManagerRepository
import com.hyphenate.easeim.common.livedatas.SingleSourceLiveData
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hyphenate.EMValueCallBack
import com.hyphenate.easeim.common.db.entity.MsgTypeManageEntity
import com.hyphenate.easeim.common.db.DbHelper
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.net.ErrorCode
import com.hyphenate.easeim.common.net.Resource
import java.lang.Exception

/**
 * 会话列表 vm
 */
open class ConversationListVm() : ViewModel() {
    private val repository = EMChatManagerRepository()


    val conversation get() = _conversation
    private val _conversation = MutableLiveData<Resource<List<Any>?>>()


    val conversationList get() = _conversationList
    private val _conversationList = MutableLiveData<List<EaseConversationInfo>?>()


    val deleteConversation: LiveData<Resource<Boolean>> get() = _deleteConversation
    private val _deleteConversation = SingleSourceLiveData<Resource<Boolean>>()


    val readConversation get() = _readConversation
    private val _readConversation = MutableLiveData<Boolean>()

    /**
     * 获取聊天列表
     */
    fun loadConversationList() {
        repository.loadConversationList(
            result = object : ResultCallBack<List<Any>>() {
                override fun onError(error: Int, errorMsg: String?) {
                    _conversation.postValue(Resource.error(error, errorMsg, null))
                }

                override fun onSuccess(value: List<Any>?) {
                    _conversation.postValue(Resource.success(value))
                }
            })
    }

    /**
     * 从服务器获取聊天列表
     */
    fun fetchConversationsFromServer() {
        repository.fetchConversationsFromServer(object :
            ResultCallBack<List<EaseConversationInfo>>() {
            override fun onError(error: Int, errorMsg: String?) {
                _conversationList.postValue(null)
            }

            override fun onSuccess(value: List<EaseConversationInfo>?) {
                _conversationList.postValue(value)
            }
        })
    }


    /**
     * 删除对话
     * @param conversationId
     */
    fun deleteConversationById(conversationId: String) {
        repository.deleteConversationById(conversationId, object : EMValueCallBack<Boolean> {
            override fun onSuccess(value: Boolean?) {
                _deleteConversation.postValue(Resource.success(true))
            }

            override fun onError(error: Int, errorMsg: String?) {
                _deleteConversation.postValue(Resource.error(error, errorMsg, false))

            }

        })
    }

    /**
     * 将会话置为已读
     * @param conversationId
     */
    fun makeConversationRead(conversationId: String) {
        repository.makeConversationRead(conversationId = conversationId,
            object : ResultCallBack<Boolean>() {
                override fun onError(error: Int, errorMsg: String?) {
                    _readConversation.postValue(false)
                }

                override fun onSuccess(value: Boolean?) {
                    _readConversation.postValue(true)
                }
            })
    }


    /**
     * 删除系统消息
     * @param msg
     */
    fun deleteSystemMsg(msg: MsgTypeManageEntity) {
        try {
            val dbHelper = DbHelper.dbHelper()
            dbHelper.inviteMessageDao?.delete("type", msg.type)
            dbHelper.msgTypeManageDao?.delete(msg)
            _deleteConversation.postValue(Resource.success(true))
        } catch (e: Exception) {
            e.printStackTrace()
            _deleteConversation.postValue(
                Resource.error(
                    ErrorCode.EM_DELETE_SYS_MSG_ERROR,
                    e.message ?: "错误",
                    false
                )
            )
        }
    }


}