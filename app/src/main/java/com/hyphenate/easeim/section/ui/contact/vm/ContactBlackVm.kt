package com.hyphenate.easeim.section.ui.contact.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMValueCallBack
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.repositories.EMContactManagerRepository
import com.hyphenate.easeui.domain.EaseUser

/**
 * @author LXJDBXJ
 * @date 2022/10/13
 * @desc 黑名单操作
 */
class ContactBlackVm(application: Application) : AndroidViewModel(application) {
    private val repository: EMContactManagerRepository = EMContactManagerRepository()

    val black: LiveData<List<EaseUser>?> get() = _black
    private val _black = MutableLiveData<List<EaseUser>?>()

    val result: LiveData<Boolean> get() = _result
    private val _result = MutableLiveData<Boolean>()


    fun getBlackList() {
        repository.getBlackUserList(false, object : ResultCallBack<List<EaseUser>>() {
            override fun onError(error: Int, errorMsg: String?) {
                _black.postValue(null)
            }

            override fun onSuccess(value: List<EaseUser>?) {
                _black.postValue(value)

            }

        })
    }

    fun removeUserFromBlackList(username: String?) {
        repository.removeUserFromBlackList(username, object : EMValueCallBack<Boolean> {
            override fun onSuccess(value: Boolean) {
                _result.postValue(value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                _result.postValue(false)
            }
        })
    }

}