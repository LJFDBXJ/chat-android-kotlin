package com.hyphenate.easeim.section.ui.contact.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.hyphenate.easeim.common.repositories.EMContactManagerRepository
import com.hyphenate.easeui.domain.EaseUser
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hyphenate.EMCallBack
import com.hyphenate.EMValueCallBack
import com.hyphenate.easeim.R
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.common.db.DbHelper
import com.hyphenate.easeim.common.db.entity.EmUserEntity
import com.hyphenate.easeim.common.entity.ContactItemEntity
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeui.modules.contact.EaseContactListLayout

class ContactsVm(application: Application) : AndroidViewModel(application) {
    private val mRepository = EMContactManagerRepository()

    val contact: LiveData<List<EaseUser>?> get() = _contact
    private val _contact = MutableLiveData<List<EaseUser>?>()

    val blackUser = MediatorLiveData<List<EaseUser>?>()
    private val _blackUser = MediatorLiveData<List<EaseUser>?>()

    val blackResult: LiveData<Boolean> get() = _blackResult
    private val _blackResult = MutableLiveData<Boolean>()


    val deleteUser: LiveData<Boolean> get() = _deleteUser
    private val _deleteUser = MutableLiveData<Boolean>()


    private val headItem = arrayOf(
        ContactItemEntity(
            clickId = R.id.contact_header_item_new_chat,
            iconRes = R.drawable.em_friends_new_chat,
            titleRes = application.getString(R.string.em_friends_new_chat)
        ),
        ContactItemEntity(
            clickId = R.id.contact_header_item_group_list,
            iconRes = R.drawable.em_friends_group_chat,
            titleRes = application.getString(R.string.em_friends_group_chat)
        ),
        ContactItemEntity(
            clickId = R.id.contact_header_item_chat_room_list,
            iconRes = R.drawable.em_friends_chat_room,
            titleRes = application.getString(R.string.em_friends_chat_room)
        ),
    )

    fun initHeadItem(contactList: EaseContactListLayout) {
        headItem.forEach {
            contactList.addCustomItem(
                it.clickId,
                it.iconRes,
                it.titleRes
            )
        }
    }

    fun blackList() {
        mRepository.getBlackUserList(
            isFromServer = false,
            callBack = object : ResultCallBack<List<EaseUser>>() {
                override fun onError(error: Int, errorMsg: String?) {
                    _blackUser.postValue(null)
                }

                override fun onSuccess(value: List<EaseUser>?) {
                    _blackUser.postValue(value)
                }
            })
    }

    fun loadContactList(fetchServer: Boolean) {
        mRepository.getContactList(
            fetchServer = fetchServer,
            callBack = object : ResultCallBack<List<EaseUser>>() {
                override fun onSuccess(value: List<EaseUser>) {
                    val userDao = DbHelper.dbHelper().userDao
                    val userList = EmUserEntity.parseList(users = value)
                    userDao?.clearUsers()
                    userDao?.insert(users = userList)
                    _contact.postValue(userList)
                }

                override fun onError(error: Int, errorMsg: String) {
                    _contact.postValue(null)
                }
            })
    }

    fun deleteContact(userName: String) {
        mRepository.deleteContact(
            userName = userName,
            callBack = object : EMCallBack {
                override fun onSuccess() {
                    SdkHelper.instance.deleteContact(username = userName)
                    _deleteUser.postValue(true)
                }

                override fun onError(code: Int, error: String) {
                    _deleteUser.postValue(false)
                }

                override fun onProgress(progress: Int, status: String) {}
            })
    }

    fun addUserToBlackList(userName: String?, both: Boolean) {
        mRepository.addUserToBlackList(
            userName = userName,
            both = both,
            callBack = object : EMValueCallBack<Boolean> {
                override fun onSuccess(value: Boolean) {
                    _blackResult.postValue(true)
                }

                override fun onError(error: Int, errorMsg: String?) {
                    _blackResult.postValue(false)
                }

            })
    }

}