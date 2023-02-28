package com.hyphenate.easeim.section.ui.contact.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMCallBack
import com.hyphenate.EMValueCallBack
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.common.repositories.EMContactManagerRepository
import com.hyphenate.easeui.domain.EaseUser

class ContactDetailVm(application: Application) : AndroidViewModel(application) {
    private val repository: EMContactManagerRepository = EMContactManagerRepository()


    val delete: LiveData<Boolean> get() = _delete
    private val _delete = MutableLiveData<Boolean>()


    val black: LiveData<Boolean> get() = _black
    private val _black = MutableLiveData<Boolean>()


    val userInfo: LiveData<EaseUser?> get() = _userInfo
    private val _userInfo = MutableLiveData<EaseUser?>()


    fun deleteContact(username: String) {
        repository.deleteContact(
            userName = username,
            callBack = object : EMCallBack {
                override fun onSuccess() {
                    SdkHelper.instance.deleteContact(username = username)
                    _delete.postValue(true)
                }

                override fun onError(code: Int, error: String) {
                    _delete.postValue(false)
                }

                override fun onProgress(progress: Int, status: String) {}
            })
    }

    fun addUserToBlackList(username: String, both: Boolean) {
        repository.addUserToBlackList(
            userName = username,
            both = both,
            callBack = object : EMValueCallBack<Boolean> {
                override fun onSuccess(value: Boolean) {
                    _black.postValue(true)
                }

                override fun onError(error: Int, errorMsg: String?) {
                    _black.postValue(false)
                }

            })
    }

    fun getUserInfoById(userName: String, mIsFriend: Boolean) {
        repository.getUserInfoById(
            isFromServer = true,
            userName = userName,
            mIsFriend = mIsFriend,
            callBack = object : EMValueCallBack<EaseUser> {
                override fun onSuccess(value: EaseUser?) {
                    _userInfo.postValue(value)
                }

                override fun onError(error: Int, errorMsg: String?) {
                    _userInfo.postValue(null)
                }

            })
    }

}