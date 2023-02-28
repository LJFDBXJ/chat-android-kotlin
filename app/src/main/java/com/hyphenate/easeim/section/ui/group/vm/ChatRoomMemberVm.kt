package com.hyphenate.easeim.section.ui.group.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.hyphenate.easeim.common.repositories.EMChatRoomManagerRepository
import com.hyphenate.chat.EMChatRoom
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMCallBack
import com.hyphenate.EMValueCallBack
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.net.Resource

class ChatRoomMemberVm(application: Application) : AndroidViewModel(application) {
    private val repository = EMChatRoomManagerRepository()

    val chatRoomObservable: LiveData<EMChatRoom?> get() = _chatRoomObservable
    private val _chatRoomObservable = MutableLiveData<EMChatRoom?>()


    val destroyGroup: LiveData<Boolean> get() = _destroyGroup
    private val _destroyGroup = MutableLiveData<Boolean>()


    val black: LiveData<List<String>?> get() = _black
    private val _black = MutableLiveData<List<String>?>()

    val muteMap: LiveData<Resource<Map<String, Long>?>> get() = _muteMap
    private val _muteMap = MutableLiveData<Resource<Map<String, Long>?>>()


    val members: LiveData<Resource<List<String>?>> get() = _members
    private val _members = MutableLiveData<Resource<List<String>?>>()


    fun getGroupMuteMap(roomId: String) {
        repository.getChatRoomMuteMap(
            groupId = roomId,
            callBack = object : ResultCallBack<Map<String, Long>>() {
                override fun onError(error: Int, errorMsg: String) {
                    _muteMap.postValue(
                        Resource.error(error, null)
                    )
                }

                override fun onSuccess(value: Map<String, Long>) {
                    _muteMap.postValue(
                        Resource.success(value)
                    )
                }

            })
    }

    fun getGroupBlackList(roomId: String?) {
        repository.getChatRoomBlackList(
            groupId = roomId,
            object : EMValueCallBack<ArrayList<String>> {
                override fun onSuccess(value: ArrayList<String>) {
                    _black.postValue(value)
                }

                override fun onError(error: Int, errorMsg: String?) {
                    _black.postValue(null)
                }
            })

    }

    fun getMembersList(roomId: String?) {
        repository.loadMembers(roomId, object : ResultCallBack<Resource<List<String>?>>() {
            override fun onError(error: Int, errorMsg: String?) {

            }

            override fun onSuccess(value: Resource<List<String>?>) {
                _members.postValue(value)

            }
        })
    }

    private val callBack = object : EMValueCallBack<EMChatRoom> {
        override fun onSuccess(value: EMChatRoom) {
            _chatRoomObservable.postValue(value)
        }

        override fun onError(error: Int, errorMsg: String) {
            _chatRoomObservable.postValue(null)

        }
    }

    fun getChatRoom(roomId: String) {
        repository.getChatRoomById(
            roomId = roomId,
            callBack = callBack
        )
    }

    fun addGroupAdmin(roomId: String?, username: String) {
        repository.addChatRoomAdmin(
            groupId = roomId,
            username = username,
            callBack = callBack
        )
    }

    fun removeGroupAdmin(roomId: String?, username: String) {
        repository.removeChatRoomAdmin(
            groupId = roomId,
            username = username,
            callBack = callBack
        )
    }

    fun changeOwner(roomId: String?, username: String) {
        repository.changeOwner(
            groupId = roomId,
            username = username,
            callBack = callBack
        )
    }

    fun removeUserFromGroup(roomId: String?, usernames: List<String>) {
        repository.removeUserFromChatRoom(
            groupId = roomId,
            usernames = usernames,
            callBack = callBack
        )
    }


    fun unblockUser(roomId: String?, username: List<String>) {
        repository.unblockUser(
            groupId = roomId,
            username = username,
            callBack = callBack
        )
    }

    fun muteGroupMembers(roomId: String?, usernames: List<String>, duration: Long) {
        repository.muteChatRoomMembers(
            groupId = roomId,
            usernames = usernames,
            duration = duration,
            callBack = callBack
        )
    }

    fun unMuteGroupMembers(roomId: String?, usernames: List<String>) {
        repository.unMuteChatRoomMembers(
            groupId = roomId,
            usernames = usernames,
            callBack = callBack
        )
    }

    fun blockUser(roomId: String?, username: List<String>) {
        repository.blockUser(
            groupId = roomId,
            username = username,
            callBack = object : EMValueCallBack<EMChatRoom> {
                override fun onSuccess(value: EMChatRoom) {
                    repository.removeUserFromChatRoom(
                        roomId,
                        username,
                        callBack
                    )
                }

                override fun onError(error: Int, errorMsg: String) {
                    _chatRoomObservable.postValue(null)
                }
            }
        )
    }

    fun destroyGroup(roomId: String) {
        repository.destroyChatRoom(roomId, object : EMCallBack {
            override fun onSuccess() {
                _destroyGroup.postValue(true)

            }

            override fun onError(code: Int, error: String?) {
                _destroyGroup.postValue(false)
            }
        })
    }

}