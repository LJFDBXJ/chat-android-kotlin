package com.hyphenate.easeim.section.ui.chat.vm

import com.hyphenate.easeim.section.ui.conversation.vm.ConversationListVm
import com.hyphenate.easeim.common.repositories.EMChatRoomManagerRepository
import com.hyphenate.easeim.common.repositories.EMChatManagerRepository
import com.hyphenate.chat.EMChatRoom
import androidx.lifecycle.LiveData
import com.hyphenate.chat.EMClient
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMValueCallBack

class ChatVm() : ConversationListVm() {
    private val chatRoomManagerRepository = EMChatRoomManagerRepository()
    private val chatManagerRepository = EMChatManagerRepository()


    val chatRoom: LiveData<EMChatRoom?> get() = _chatRoom
    private val _chatRoom = MutableLiveData<EMChatRoom?>()

    val makeConversationRead: LiveData<Boolean> get() = _makeConversationRead
    private val _makeConversationRead = MutableLiveData<Boolean>()

    val noPushUsers: LiveData<List<String>> get() = _noPushUsers
    private val _noPushUsers = MutableLiveData<List<String>>()

    val setNoPushUsers: LiveData<Boolean> get() = _setNoPushUsers
    private val _setNoPushUsers = MutableLiveData<Boolean>()


    fun getChatRoom(roomId: String) {
        val room = EMClient.getInstance().chatroomManager().getChatRoom(roomId)
        if (room != null) {
            _chatRoom.postValue(room)
        } else {
            chatRoomManagerRepository.getChatRoomById(
                roomId = roomId,
                callBack = object : EMValueCallBack<EMChatRoom> {
                    override fun onSuccess(value: EMChatRoom) {
                        _chatRoom.postValue(value)
                    }

                    override fun onError(error: Int, errorMsg: String) {
                        _chatRoom.postValue(null)

                    }
                })
        }
    }

    fun makeConversationReadByAck(conversationId: String?) {
        chatManagerRepository.makeConversationReadByAck(
            conversationId = conversationId,
            callBack = object : EMValueCallBack<Boolean> {
                override fun onSuccess(value: Boolean?) {
                    _makeConversationRead.postValue(true)
                }

                override fun onError(error: Int, errorMsg: String?) {

                }
            }
        )
    }

    /**
     * 设置单聊用户聊天免打扰
     *
     * @param userId 用户名
     * @param noPush 是否免打扰
     */
    fun setUserNotDisturb(userId: String, noPush: Boolean) {
        chatManagerRepository.setUserNotDisturb(
            userId = userId,
            noPush = noPush,
            callBack = object : EMValueCallBack<Boolean> {
                override fun onSuccess(value: Boolean?) {
                    _setNoPushUsers.postValue(true)
                }

                override fun onError(error: Int, errorMsg: String?) {
                    _setNoPushUsers.postValue(false)
                }

            })
    }

    /**
     * 获取聊天免打扰用户
     */
    fun noPushUsers() {
        chatManagerRepository.noPushUsers {
            _noPushUsers.postValue(it)
        }
    }


}