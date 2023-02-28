package com.hyphenate.easeim.section.ui.contact.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMGroup
import com.hyphenate.chat.EMGroupOptions
import com.hyphenate.easeim.common.repositories.EMGroupManagerRepository

class NewGroupVm(application: Application) : AndroidViewModel(application) {
    private val repository = EMGroupManagerRepository()

    val group: LiveData<EMGroup?> get() = _group
    private val _group = MutableLiveData<EMGroup?>()


    fun createGroup(
        groupName: String?,
        desc: String?,
        allMembers: Array<String>,
        reason: String?,
        option: EMGroupOptions?
    ) {
        repository.createGroup(
            groupName = groupName,
            desc = desc,
            allMembers = allMembers,
            reason = reason,
            option = option,
            object : EMValueCallBack<EMGroup> {
                override fun onSuccess(value: EMGroup) {
                    _group.postValue(value)
                }

                override fun onError(error: Int, errorMsg: String) {
                    _group.postValue(null)
                }
            }
        )

    }

}