package com.hyphenate.easeim.section.ui.contact.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMChatRoom
import com.hyphenate.easeim.common.repositories.EMChatRoomManagerRepository

class NewChatRoomVm(application: Application) : AndroidViewModel(application) {
    private val repository: EMChatRoomManagerRepository = EMChatRoomManagerRepository()

    val chatRoom: LiveData<EMChatRoom?> get() = _chatRoom
    private val _chatRoom = MutableLiveData<EMChatRoom?>()

    fun createChatRoom(
        subject: String?,
        description: String?,
        welcomeMessage: String?,
        maxUserCount: Int, members: List<String>?
    ) {
        repository.createChatRoom(
            subject = subject,
            description = description,
            welcomeMessage = welcomeMessage,
            maxUserCount = maxUserCount,
            members = members,
            object : EMValueCallBack<EMChatRoom> {
                override fun onSuccess(value: EMChatRoom?) {
                    _chatRoom.postValue(value)
                }

                override fun onError(error: Int, errorMsg: String?) {
                    _chatRoom.postValue(null)

                }
            }
        )
    }

}