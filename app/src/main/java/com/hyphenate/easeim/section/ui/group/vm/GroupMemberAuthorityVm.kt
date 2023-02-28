package com.hyphenate.easeim.section.ui.group.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMGroup
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.livedatas.SingleSourceLiveData
import com.hyphenate.easeim.common.net.Resource
import com.hyphenate.easeim.common.repositories.EMGroupManagerRepository
import com.hyphenate.easeui.domain.EaseUser

class GroupMemberAuthorityVm(application: Application) : AndroidViewModel(application) {
    val repository = EMGroupManagerRepository()

    val admin: LiveData<EMGroup?> get() = _admin
    private val _admin = SingleSourceLiveData<EMGroup?>()


    val members: LiveData<List<EaseUser>?> get() = _members
    private val _members = MutableLiveData<List<EaseUser>?>()


    val muteMembers get() = _muteMembers
    private val _muteMembers = MutableLiveData<Resource<Map<String, Long>?>>()

    val blackMembers get() = _blackMembers
    private val _blackMembers = MutableLiveData<Resource<List<String>?>>()


    val refresh get() = _refresh
    private val _refresh = MutableLiveData<Resource<String>>()


    val transferOwner get() = _transferOwner
    private val _transferOwner = MutableLiveData<Resource<Boolean>>()


    fun getGroup(groupId: String?) {
        repository.getGroupFromServer(
            groupId = groupId,
            callBack = object : EMValueCallBack<EMGroup> {
                override fun onSuccess(value: EMGroup) {
                    _admin.postValue(value)

                }

                override fun onError(error: Int, errorMsg: String) {
                    _admin.postValue(null)
                }
            })
    }


    fun getMembers(groupId: String?) {
        repository.getGroupMembers(
            groupId = groupId,
            callBack = object : EMValueCallBack<ArrayList<EaseUser>> {
                override fun onSuccess(value: ArrayList<EaseUser>?) {
                    _members.postValue(value)

                }

                override fun onError(error: Int, errorMsg: String?) {
                    _members.postValue(null)
                }

            })
    }


    fun getMuteMembers(groupId: String?) {
        repository.getGroupMuteMap(
            groupId = groupId,
            callBack = object : ResultCallBack<Map<String, Long>>() {
                override fun onError(error: Int, errorMsg: String?) {
                    _muteMembers.postValue(Resource.error(error, errorMsg, null))

                }

                override fun onSuccess(value: Map<String, Long>) {
                    _muteMembers.postValue(Resource.success(value))
                }
            })
    }

    fun getBlackMembers(groupId: String) {
        repository.getGroupBlackList(
            groupId = groupId,
            callBack = object : ResultCallBack<List<String>>() {
                override fun onError(error: Int, errorMsg: String?) {
                    _blackMembers.postValue(Resource.error(error, errorMsg, null))
                }

                override fun onSuccess(value: List<String>?) {
                    _blackMembers.postValue(Resource.success(value))
                }
            })
    }


    fun changeOwner(groupId: String?, username: String?) {
        repository.changeOwner(
            groupId = groupId,
            username = username,
            callBack = object : ResultCallBack<Boolean>() {
                override fun onError(error: Int, errorMsg: String?) {
                    _transferOwner.postValue(Resource.error(error, errorMsg, false))

                }

                override fun onSuccess(value: Boolean) {
                    _transferOwner.postValue(Resource.success(value))
                }

            })

    }

    fun addGroupAdmin(groupId: String?, username: String?) {
        repository.addGroupAdmin(
            groupId = groupId,
            username = username,
            callBack = object : ResultCallBack<String>() {
                override fun onError(error: Int, errorMsg: String?) {
                    _refresh.postValue(Resource.error(error, errorMsg, ""))
                }

                override fun onSuccess(value: String) {
                    _refresh.postValue(Resource.success(value))
                }
            })
    }

    fun removeGroupAdmin(groupId: String?, username: String?) {
        repository.removeGroupAdmin(
            groupId = groupId,
            username = username,
            callBack = object : ResultCallBack<String>() {
                override fun onError(error: Int, errorMsg: String) {
                    _refresh.postValue(Resource.error(error, errorMsg, ""))
                }

                override fun onSuccess(value: String) {
                    _refresh.postValue(Resource.success(value))
                }
            })

    }

    fun removeUserFromGroup(groupId: String?, username: String?) {
        repository.removeUserFromGroup(
            groupId = groupId,
            username = username,
            callBack = object : ResultCallBack<String>() {
                override fun onError(error: Int, errorMsg: String?) {
                    _refresh.postValue(Resource.error(error, errorMsg, ""))
                }

                override fun onSuccess(value: String) {
                    _refresh.postValue(Resource.success(value))
                }
            })

    }

    fun blockUser(groupId: String?, username: String?) {
        repository.blockUser(
            groupId = groupId,
            username = username,
            callBack = object : ResultCallBack<String>() {
                override fun onError(error: Int, errorMsg: String?) {
                    _refresh.postValue(Resource.error(error, errorMsg, ""))
                }

                override fun onSuccess(value: String) {
                    _refresh.postValue(Resource.success(value))
                }
            })
    }

    fun unblockUser(groupId: String?, username: String) {
        repository.unblockUser(groupId, username, object : ResultCallBack<String>() {
            override fun onError(error: Int, errorMsg: String?) {
                _refresh.postValue(Resource.error(error, errorMsg, ""))
            }

            override fun onSuccess(value: String) {
                _refresh.postValue(Resource.success(value))
            }
        })
    }

    fun muteGroupMembers(groupId: String?, usernames: List<String>, duration: Long) {
        repository.muteGroupMembers(
            groupId,
            usernames,
            duration,
            object : ResultCallBack<String>() {
                override fun onError(error: Int, errorMsg: String?) {
                    _refresh.postValue(Resource.error(error, errorMsg, ""))
                }

                override fun onSuccess(value: String) {
                    _refresh.postValue(Resource.success(value))
                }
            })
    }

    fun unMuteGroupMembers(groupId: String?, usernames: List<String>) {
        repository.unMuteGroupMembers(groupId, usernames, object : ResultCallBack<String>() {
            override fun onError(error: Int, errorMsg: String?) {
                _refresh.postValue(Resource.error(error, errorMsg, ""))
            }

            override fun onSuccess(value: String) {
                _refresh.postValue(Resource.success(value))
            }
        })
    }

}