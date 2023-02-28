package com.hyphenate.easeim.login.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hyphenate.EMValueCallBack
import com.hyphenate.easeim.common.net.Resource
import com.hyphenate.easeim.common.repositories.EMClientRepository

class LoginVm() : ViewModel() {
    private val mRepository = EMClientRepository()

    val register: LiveData<Resource<String>> get() = _register
    private val _register = MutableLiveData<Resource<String>>()

    /**
     * 获取页面跳转的observable
     * @return
     */
    val pageSelect: LiveData<Int> get() = _pageSelect
    private val _pageSelect = MutableLiveData<Int>()


    /**
     * 设置跳转的页面
     * @param page
     */
    fun setPageSelect(page: Int) {
        _pageSelect.value=page
    }

    /**
     * 注册环信账号
     * @param userName
     * @param pwd
     * @return
     */
    fun register(userName: String, pwd: String) {
        mRepository.registerToHx(userName, pwd, object : EMValueCallBack<String> {
            override fun onSuccess(value: String) {
                _register.postValue(Resource.success(value))

            }

            override fun onError(error: Int, errorMsg: String?) {
                _register.postValue(Resource.error(error,errorMsg+""))
            }

        })
    }


    /**
     * 清理注册信息
     */
    fun clearRegisterInfo() {
        _register.postValue(Resource.error(0,""))
    }

}