package com.hyphenate.easeim.login.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.hyphenate.easeim.common.repositories.EMClientRepository
import androidx.lifecycle.MediatorLiveData
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeim.common.net.Resource

class LoginFragmentVm(application: Application) : AndroidViewModel(application) {
    private val mRepository: EMClientRepository = EMClientRepository()

    val loginObservable: LiveData<Resource<EaseUser?>> get() = _loginObservable
    private val _loginObservable = MediatorLiveData<Resource<EaseUser?>>()

    /**
     * 登录环信
     * @param userName
     * @param pwd
     * @param isTokenFlag
     */
    fun login(userName: String, pwd: String, isTokenFlag: Boolean) {
        mRepository.loginToServer(
            userName,
            pwd,
            isTokenFlag, object : ResultCallBack<EaseUser>() {
                override fun onError(error: Int, errorMsg: String?) {
                    _loginObservable.postValue(Resource.error(error, errorMsg, null))
                }

                override fun onSuccess(value: EaseUser?) {
                    _loginObservable.postValue(Resource.success(value))
                }
            }
        )
    }
}