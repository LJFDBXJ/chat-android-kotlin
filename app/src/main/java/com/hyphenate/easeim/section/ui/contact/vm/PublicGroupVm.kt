package com.hyphenate.easeim.section.ui.contact.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMGroup
import com.hyphenate.easeim.common.repositories.EMGroupManagerRepository

class PublicGroupVm(application: Application) : AndroidViewModel(application) {
    private val repository: EMGroupManagerRepository = EMGroupManagerRepository()


    val group: LiveData<EMGroup?> get() = _group
    private val _group = MutableLiveData<EMGroup?>()

    val join: LiveData<Boolean> get() = _join
    private val _join = MutableLiveData<Boolean>()

    fun getGroup(groupId: String?) {
        repository.getGroupFromServer(
            groupId = groupId,
            callBack = object : EMValueCallBack<EMGroup> {
                override fun onSuccess(value: EMGroup) {
                    _group.postValue(value)

                }

                override fun onError(error: Int, errorMsg: String) {
                    _group.postValue(null)
                }
            })
    }


    fun joinGroup(emGroup: EMGroup, reason: String?) {
        repository.joinGroup(
            group = emGroup,
            reason = reason,
            callBack = object : EMValueCallBack<Boolean> {
                override fun onSuccess(value: Boolean?) {
                    _join.postValue(true)
                }

                override fun onError(error: Int, errorMsg: String?) {
                    _join.postValue(false)

                }
            })

    }

}