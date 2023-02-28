package com.hyphenate.easeim.section.ui.group.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMCallBack
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMChatRoom
import com.hyphenate.easeim.common.enums.Status
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.net.Resource
import com.hyphenate.easeim.common.repositories.EMChatRoomManagerRepository

class ChatRoomDetailVm(application: Application) : AndroidViewModel(application) {
    private val repository = EMChatRoomManagerRepository()

    val chatRoom: LiveData<EMChatRoom?> get() = _chatRoom
    private val _chatRoom = MutableLiveData<EMChatRoom?>()


    val members: LiveData<Resource<List<String>?>> get() = _members
    private val _members = MutableLiveData<Resource<List<String>?>>()


    val announcement get() = _announcement
    private val _announcement = MutableLiveData<Resource<String>>()


    val destroyGroup: LiveData<Boolean> get() = _destroyGroup
    private val _destroyGroup = MutableLiveData<Boolean>()


    private val callBack = object : EMValueCallBack<EMChatRoom> {
        override fun onSuccess(value: EMChatRoom) {
            _chatRoom.postValue(value)
        }

        override fun onError(error: Int, errorMsg: String) {
            _chatRoom.postValue(null)

        }
    }


    fun getChatRoomFromServer(roomId: String) {
        repository.getChatRoomById(
            roomId = roomId,
            callBack = callBack
        )
    }

    fun changeChatRoomSubject(roomId: String?, newSubject: String?) {
        repository.changeChatRoomSubject(
            roomId = roomId,
            newSubject = newSubject,
            callBack = callBack
        )
    }


    fun changeChatroomDescription(roomId: String?, newDescription: String?) {
        repository.changeChatroomDescription(
            roomId = roomId,
            newDescription = newDescription,
            callBack = callBack
        )
    }

    fun getChatRoomMembers(roomId: String?) {
        repository.loadMembers(
            roomId = roomId,
            callBack = object : ResultCallBack<Resource<List<String>?>>() {
                override fun onError(error: Int, errorMsg: String) {
                    val resource = Resource<List<String>?>(Status.SUCCESS, null, error, errorMsg)
                    _members.postValue(resource)

                }

                override fun onSuccess(value: Resource<List<String>?>) {
                    _members.postValue(value)
                }

            })
    }

    fun updateAnnouncement(roomId: String?, announcement: String) {
        val result = repository.updateAnnouncement(
            roomId = roomId,
            announcement = announcement,
            object : ResultCallBack<String>() {
                override fun onError(error: Int, errorMsg: String) {
                    _announcement.postValue(Resource.error(error, errorMsg))

                }

                override fun onSuccess(value: String) {
                    _announcement.postValue(Resource.success(value))
                }
            })
    }

    fun fetchChatRoomAnnouncement(roomId: String?) {
        repository.fetchChatRoomAnnouncement(
            roomId = roomId,
            callBack = object : ResultCallBack<String>() {
                override fun onError(error: Int, errorMsg: String) {
                    _announcement.postValue(Resource.error(error, errorMsg))
                }

                override fun onSuccess(value: String) {
                    _announcement.postValue(Resource.success(value))
                }
            })

    }

    fun destroyGroup(roomId: String) {
        repository.destroyChatRoom(
            groupId = roomId,
            callBack = object : EMCallBack {
                override fun onSuccess() {
                    _destroyGroup.postValue(true)
                }

                override fun onError(code: Int, error: String) {
                    callBack.onError(code, error)
                    _destroyGroup.postValue(false)

                }

                override fun onProgress(progress: Int, status: String) {}
            })
    }

}