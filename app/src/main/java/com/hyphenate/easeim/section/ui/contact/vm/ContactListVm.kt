package com.hyphenate.easeim.section.ui.contact.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.easeim.common.db.DbHelper
import com.hyphenate.easeim.common.db.entity.EmUserEntity
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.repositories.EMContactManagerRepository
import com.hyphenate.easeui.domain.EaseUser

class ContactListVm(application: Application) : AndroidViewModel(application) {
    private val mRepository = EMContactManagerRepository()


    val contactList: LiveData<List<EaseUser>> get() = _contactList
    private val _contactList = MutableLiveData<List<EaseUser>>()


    fun contactList() {
        mRepository.getContactList(
            callBack = object : ResultCallBack<List<EaseUser>>() {
                override fun onSuccess(value: List<EaseUser>) {
                    val userDao = DbHelper.dbHelper().userDao
                    userDao?.clearUsers()
                    userDao?.insert(EmUserEntity.parseList(value))
                    _contactList.postValue(value)
                }

                override fun onError(error: Int, errorMsg: String) {}
            })
    }

}