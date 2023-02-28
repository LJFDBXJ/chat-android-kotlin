package com.hyphenate.easeim.section.ui.group.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMCallBack
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMGroup
import com.hyphenate.chat.EMPushConfigs
import com.hyphenate.easeim.SdkHelper.Companion.instance
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.livedatas.SingleSourceLiveData
import com.hyphenate.easeim.common.net.Resource
import com.hyphenate.easeim.common.repositories.EMChatManagerRepository
import com.hyphenate.easeim.common.repositories.EMGroupManagerRepository
import com.hyphenate.easeim.common.repositories.EMPushManagerRepository
import com.hyphenate.easeui.manager.EaseThreadManager
import com.hyphenate.exceptions.HyphenateException

class GroupDetailVm(application: Application) : AndroidViewModel(application) {
    private val repository = EMGroupManagerRepository()
    private val chatRepository = EMChatManagerRepository()

    val group: LiveData<EMGroup?> get() = _group
    private val _group = MutableLiveData<EMGroup?>()


    val announcement: LiveData<String?> get() = _announcement
    private val _announcement = MutableLiveData<String?>()

    val refresh: LiveData<String?> get() = _refresh
    private val _refresh = MutableLiveData<String?>()

    val leaveGroup: LiveData<Boolean> get() = _leaveGroup
    private val _leaveGroup = MutableLiveData<Boolean>()

    val blockGroupMessage get() = _blockGroupMessage
    private val _blockGroupMessage = MutableLiveData<Resource<Boolean>>()


    val unblockGroupMessage get() = _unblockGroupMessage
    private val _unblockGroupMessage = MutableLiveData<Resource<Boolean>>()

    val clearHistory: LiveData<Boolean> get() = _clearHistory
    private val _clearHistory = MutableLiveData<Boolean>()

    private val offPushObservable = SingleSourceLiveData<Boolean>()

    fun getConfig() {
        EMPushManagerRepository().pushConfigsFromServer(callBack = object :
            EMValueCallBack<EMPushConfigs> {
            override fun onSuccess(value: EMPushConfigs?) {

            }

            override fun onError(error: Int, errorMsg: String?) {

            }

        })
    }

    fun getGroup(groupId: String?) {
        repository.getGroupFromServer(groupId, object : EMValueCallBack<EMGroup> {
            override fun onSuccess(value: EMGroup) {
                _group.postValue(value)
            }

            override fun onError(error: Int, errorMsg: String) {
                _group.postValue(null)
            }
        })
    }


    fun getGroupAnnouncement(groupId: String) {
        repository.getGroupAnnouncement(false, groupId,
            object : EMValueCallBack<String> {
                override fun onSuccess(value: String) {
                    _announcement.postValue(value)
                }

                override fun onError(error: Int, errorMsg: String) {
                    _announcement.postValue(null)

                }
            })
    }

    fun setGroupName(groupId: String, groupName: String) {
        repository.setGroupName(groupId, groupName, object : EMCallBack {
            override fun onSuccess() {
                _refresh.postValue(groupName)
            }

            override fun onError(code: Int, error: String) {
                _refresh.postValue(null)

            }

            override fun onProgress(progress: Int, status: String) {}
        })
    }

    fun setGroupAnnouncement(groupId: String, announcement: String) {
        repository.setGroupAnnouncement(groupId, announcement,
            object : EMCallBack {
                override fun onSuccess() {
                    _refresh.postValue(announcement)

                }

                override fun onError(code: Int, error: String) {
                    _refresh.postValue(null)

                }

                override fun onProgress(progress: Int, status: String) {}
            })
    }

    fun setGroupDescription(groupId: String, description: String) {
        repository.setGroupDescription(groupId, description, object : EMCallBack {
            override fun onSuccess() {
                _refresh.postValue(description)

            }

            override fun onError(code: Int, error: String) {
                _refresh.postValue(null)

            }

            override fun onProgress(progress: Int, status: String) {}
        })
    }

    fun leaveGroup(groupId: String) {
        repository.leaveGroup(groupId, object : EMCallBack {
            override fun onSuccess() {
                _leaveGroup.postValue(true)

            }

            override fun onError(code: Int, error: String) {
                _leaveGroup.postValue(false)
            }

            override fun onProgress(progress: Int, status: String) {}
        })

    }

    fun destroyGroup(groupId: String) {
        repository.destroyGroup(groupId, object : EMCallBack {
            override fun onSuccess() {
                _leaveGroup.postValue(true)
            }

            override fun onError(code: Int, error: String) {
                _leaveGroup.postValue(false)

            }

            override fun onProgress(progress: Int, status: String) {}
        })

    }

    fun blockGroupMessage(groupId: String?) {
        repository.blockGroupMessage(groupId!!, object : ResultCallBack<Boolean>() {
            override fun onError(error: Int, errorMsg: String?) {
                _blockGroupMessage.postValue(Resource.error(error, errorMsg, false))

            }

            override fun onSuccess(value: Boolean) {
                _blockGroupMessage.postValue(Resource.success(value))
            }
        })
    }


    fun unblockGroupMessage(groupId: String?) {
        repository.unblockGroupMessage(groupId!!,object : ResultCallBack<Boolean>(){
            override fun onError(error: Int, errorMsg: String?) {
                _unblockGroupMessage.postValue(Resource.error(error,errorMsg,false))
            }

            override fun onSuccess(value: Boolean) {
                _unblockGroupMessage.postValue(Resource.success(value))
            }
        })
    }

    fun offPushObservable(): LiveData<Boolean> {
        return offPushObservable
    }

    fun updatePushServiceForGroup(groupId: String, noPush: Boolean) {
        EaseThreadManager.getInstance().runOnIOThread {
            val onPushList: MutableList<String> = ArrayList()
            onPushList.add(groupId)
            try {
                instance.pushManager.updatePushServiceForGroup(onPushList, noPush)
            } catch (e: HyphenateException) {
                e.printStackTrace()
                offPushObservable.postValue(true)
            }
            offPushObservable.postValue(true)
        }
    }

    fun clearHistory(conversationId: String?) {
        chatRepository.deleteConversationById(conversationId!!, object : EMValueCallBack<Boolean> {
            override fun onSuccess(value: Boolean?) {
                _clearHistory.postValue(true)
            }

            override fun onError(error: Int, errorMsg: String?) {
                _clearHistory.postValue(false)

            }

        })
    }

}