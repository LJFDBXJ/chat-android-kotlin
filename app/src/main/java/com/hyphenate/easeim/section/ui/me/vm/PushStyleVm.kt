package com.hyphenate.easeim.section.ui.me.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMPushManager.DisplayStyle
import com.hyphenate.easeim.common.repositories.EMPushManagerRepository

class PushStyleVm(application: Application) : AndroidViewModel(application) {
    private val repository = EMPushManagerRepository()
    val pushStyleObservable: LiveData<Boolean> get() = _pushStyleObservable
    private val _pushStyleObservable = MutableLiveData<Boolean>()


    fun updateStyle(style: DisplayStyle?) {
        repository.updatePushStyle(style, object : EMValueCallBack<Boolean> {
            override fun onSuccess(value: Boolean) {
                _pushStyleObservable.postValue(value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                _pushStyleObservable.postValue(false)

            }

        })
    }

}