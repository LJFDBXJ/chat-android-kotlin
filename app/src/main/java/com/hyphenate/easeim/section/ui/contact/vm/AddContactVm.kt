package com.hyphenate.easeim.section.ui.contact.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMValueCallBack
import com.hyphenate.easeim.common.repositories.EMContactManagerRepository

/**
 * 添加联系人
 */
class AddContactVm(application: Application) : AndroidViewModel(application) {
    private val mRepository = EMContactManagerRepository()


    val addContact: LiveData<Boolean> get() = _addContact
    private val _addContact = MutableLiveData<Boolean>()


    fun addContact(username: String, reason: String?) {
        mRepository.addContact(
            userName = username,
            reason = reason,
            callBack = object : EMValueCallBack<Boolean> {
                override fun onSuccess(value: Boolean) {
                    _addContact.postValue(value)
                }

                override fun onError(error: Int, errorMsg: String?) {
                    _addContact.postValue(false)
                }
            })

    }

    fun searchUser(searchName: String) {

    }

}