package com.hyphenate.easeim.section.ui.group.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMValueCallBack
import com.hyphenate.easeim.common.db.DbHelper
import com.hyphenate.easeim.common.db.entity.EmUserEntity
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.repositories.EMContactManagerRepository
import com.hyphenate.easeim.common.repositories.EMGroupManagerRepository
import com.hyphenate.easeui.domain.EaseUser

/**
 * @author LXJDBXJ
 * @date 2022/10/10
 * @desc 挑选成员列表 创建群
 */
class GroupPickContactsVm(application: Application) : AndroidViewModel(application) {
    private val repository: EMGroupManagerRepository = EMGroupManagerRepository()
    private val contactRepository: EMContactManagerRepository = EMContactManagerRepository()

    val groupMembers: LiveData<List<String>?> get() = _groupMembers
    private val _groupMembers = MutableLiveData<List<String>?>()


    val contacts: LiveData<List<EaseUser>?> get() = _contacts
    private val _contacts = MutableLiveData<List<EaseUser>?>()

    val addMembers: LiveData<Boolean> get() = _addMembers
    private val _addMembers = MutableLiveData<Boolean>()

    val searchContacts: LiveData<List<EaseUser>> get() = _searchContacts
    private val _searchContacts = MutableLiveData<List<EaseUser>>()


    fun getGroupMembers(groupId: String?) {
        repository.getGroupMembersByName(
            groupId = groupId,
            callBack = object : EMValueCallBack<List<String>> {
                override fun onSuccess(value: List<String>) {
                    _groupMembers.postValue(value)
                }

                override fun onError(error: Int, errorMsg: String?) {
                    _groupMembers.postValue(null)
                }

            })
    }


    val allContacts: Unit
        get() {
            contactRepository.getContactList(
                callBack = object : ResultCallBack<List<EaseUser>>() {
                    override fun onSuccess(value: List<EaseUser>) {
                        val userDao = DbHelper.dbHelper().userDao
                        if (userDao != null) {
                            userDao.clearUsers()
                            userDao.insert(EmUserEntity.parseList(value))
                        }
                        _contacts.postValue(value)
                    }

                    override fun onError(error: Int, errorMsg: String) {
                        _contacts.postValue(null)

                    }
                })
        }

    fun addGroupMembers(isOwner: Boolean, groupId: String?, members: Array<String>?) {
        repository.addMembers(
            isOwner = isOwner,
            groupId = groupId,
            members = members,
            callBack = object : EMValueCallBack<Boolean> {
                override fun onSuccess(value: Boolean) {
                    _addMembers.postValue(value)
                }

                override fun onError(error: Int, errorMsg: String?) {
                    _addMembers.postValue(false)
                }
            })
    }


    fun getSearchContacts(keyword: String?) {
        contactRepository.getSearchContacts(keyword) {
            _searchContacts.postValue(it)
        }
    }

}