package com.hyphenate.easeim.section.ui.contact.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMChatRoom
import com.hyphenate.easeim.common.repositories.EMChatRoomManagerRepository

class ChatRoomContactVm(application: Application) : AndroidViewModel(application) {
    private val mRepository: EMChatRoomManagerRepository = EMChatRoomManagerRepository()

    val load: LiveData<List<EMChatRoom>?> get() = _load
    private val _load = MutableLiveData<List<EMChatRoom>?>()


    val loadMore: LiveData<List<EMChatRoom>?> get() = _loadMore
    private val _loadMore = MutableLiveData<List<EMChatRoom>?>()


    fun loadChatRooms(pageNum: Int, pageSize: Int) {
        mRepository.loadChatRoomsFromServer(pageNum, pageSize, object :
            EMValueCallBack<List<EMChatRoom>> {
            override fun onSuccess(value: List<EMChatRoom>?) {
                _load.postValue(value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                _load.postValue(null)
            }
        })
    }

    fun setLoadMoreChatRooms(pageNum: Int, pageSize: Int) {
        mRepository.loadChatRoomsFromServer(pageNum, pageSize, object :
            EMValueCallBack<List<EMChatRoom>> {
            override fun onSuccess(value: List<EMChatRoom>?) {
                _loadMore.postValue(value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                _loadMore.postValue(null)
            }
        })
    }

}