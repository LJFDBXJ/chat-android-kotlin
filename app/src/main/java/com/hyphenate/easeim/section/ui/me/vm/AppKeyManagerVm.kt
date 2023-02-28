package com.hyphenate.easeim.section.ui.me.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.livedatas.SingleSourceLiveData
import com.hyphenate.easeim.common.net.Resource
import com.hyphenate.easeim.common.repositories.EMClientRepository

class AppKeyManagerVm(application: Application) : AndroidViewModel(application) {
    private val repository = EMClientRepository()

    val logoutObservable: MutableLiveData<Resource<Boolean>> get() = _logoutObservable
    private val _logoutObservable = MutableLiveData<Resource<Boolean>>()


    fun logout(unbindDeviceToken: Boolean) {
        repository.logout(
            unbindDeviceToken = unbindDeviceToken,
            callBack = object : ResultCallBack<Boolean>() {
                override fun onError(error: Int, errorMsg: String?) {
                    _logoutObservable.postValue(Resource.error(error, errorMsg, false))
                }

                override fun onSuccess(value: Boolean) {
                    _logoutObservable.postValue(Resource.success(value))
                }
            })
    }
}