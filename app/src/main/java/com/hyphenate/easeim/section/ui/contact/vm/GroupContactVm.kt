package com.hyphenate.easeim.section.ui.contact.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.hyphenate.easeim.common.repositories.EMGroupManagerRepository
import com.hyphenate.chat.EMGroup
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.chat.EMCursorResult
import com.hyphenate.chat.EMGroupInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMValueCallBack
import com.hyphenate.easeim.SdkHelper

class GroupContactVm(application: Application) : AndroidViewModel(application) {
    private val currentUser: String = SdkHelper.instance.currentUser
    private val mRepository = EMGroupManagerRepository()


    val allGroup: LiveData<MutableList<EMGroup>?> get() = _allGroup
    private val _allGroup = MutableLiveData<MutableList<EMGroup>?>()


    val groupMember: LiveData<List<EaseUser>?> get() = _groupMember
    private val _groupMember = MutableLiveData<List<EaseUser>?>()


    val publicGroup: LiveData<EMCursorResult<EMGroupInfo>?> get() = _publicGroup
    private val _publicGroup = MutableLiveData<EMCursorResult<EMGroupInfo>?>()


    val morePublicGroup: LiveData<EMCursorResult<EMGroupInfo>?> get() = _morePublicGroup
    private val _morePublicGroup = MutableLiveData<EMCursorResult<EMGroupInfo>?>()


    val group: LiveData<List<EMGroup>?> get() = _group
    private val _group = MutableLiveData<List<EMGroup>?>()


    val moreGroup: LiveData<List<EMGroup>?> get() = _moreGroup
    private val _moreGroup = MutableLiveData<List<EMGroup>?>()

    fun loadAllGroups() {
        mRepository.allGroups(
            callBack = object : EMValueCallBack<MutableList<EMGroup>> {
                override fun onSuccess(value: MutableList<EMGroup>) {
                    _allGroup.postValue(value)

                }

                override fun onError(error: Int, errorMsg: String) {
                    _allGroup.postValue(null)
                }
            })
    }

    fun getManageGroups(allGroups: List<EMGroup>?): List<EMGroup> {
        return mRepository.getAllManageGroups(allGroups)
    }

    fun getJoinGroups(allGroups: List<EMGroup>?): List<EMGroup> {
        return mRepository.getAllJoinGroups(allGroups)
    }

    fun getGroupMembers(groupId: String?) {
        mRepository.getGroupAllMembers(
            groupId = groupId,
            callBack = object : EMValueCallBack<List<EaseUser>> {
                override fun onSuccess(value: List<EaseUser>?) {
                    _groupMember.postValue(value)

                }

                override fun onError(error: Int, errorMsg: String?) {
                    _groupMember.postValue(null)
                }
            })
    }


    private val groupCall = object : EMValueCallBack<EMCursorResult<EMGroupInfo>> {
        override fun onSuccess(value: EMCursorResult<EMGroupInfo>) {
            _publicGroup.postValue(value)
        }

        override fun onError(error: Int, errorMsg: String) {
            _publicGroup.postValue(null)

        }
    }

    fun getPublicGroups(pageSize: Int) {
        mRepository.getPublicGroupFromServer(pageSize, null, groupCall)
    }


    fun getMorePublicGroups(pageSize: Int, cursor: String?) {
        mRepository.getPublicGroupFromServer(
            pageSize = pageSize,
            cursor = cursor,
            callBack = object : EMValueCallBack<EMCursorResult<EMGroupInfo>> {
                override fun onSuccess(value: EMCursorResult<EMGroupInfo>) {
                    _morePublicGroup.postValue(value)
                }

                override fun onError(error: Int, errorMsg: String) {
                    _publicGroup.postValue(null)

                }
            })
    }


    fun loadGroupListFromServer(pageIndex: Int, pageSize: Int) {
        mRepository.getGroupListFromServer(
            pageIndex = pageIndex,
            pageSize = pageSize,
            callBack = object : EMValueCallBack<List<EMGroup>> {
                override fun onSuccess(value: List<EMGroup>) {
                    _group.postValue(value)
                }

                override fun onError(error: Int, errorMsg: String) {
                    _group.postValue(null)
                }
            })
    }


    fun loadMoreGroupListFromServer(pageIndex: Int, pageSize: Int) {
        mRepository.getGroupListFromServer(
            pageIndex = pageIndex,
            pageSize = pageSize,
            callBack = object : EMValueCallBack<List<EMGroup>> {
                override fun onSuccess(value: List<EMGroup>) {
                    _moreGroup.postValue(value)
                }

                override fun onError(error: Int, errorMsg: String) {
                    _moreGroup.postValue(null)

                }
            })
    }

}