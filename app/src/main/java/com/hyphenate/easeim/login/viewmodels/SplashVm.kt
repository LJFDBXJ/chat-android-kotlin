package com.hyphenate.easeim.login.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.hyphenate.easeim.common.repositories.EMClientRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.net.Resource

/**
 * 登录
 */
class SplashVm(application: Application) : AndroidViewModel(application) {
    private val mRepository: EMClientRepository = EMClientRepository()
    val loginData: LiveData<Resource<Boolean>> get() = _loginData
    private val _loginData = MutableLiveData<Resource<Boolean>>()

    fun login() {
        mRepository.loadAllInfoFromHX(object : ResultCallBack<Boolean>() {
            override fun onSuccess(value: Boolean?) {
                _loginData.postValue(Resource.success(false))
            }
            override fun onError(error: Int, errorMsg: String?) {
                _loginData.postValue(Resource.error(error, errorMsg, false))
            }
        })
    }


}