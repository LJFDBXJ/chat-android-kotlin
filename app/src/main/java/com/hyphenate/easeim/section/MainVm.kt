package com.hyphenate.easeim.section

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.common.db.DbHelper

class MainVm(application: Application) : AndroidViewModel(application) {
    private val inviteMessageDao= DbHelper.dbHelper().inviteMessageDao

    val homeUnReadObservable: LiveData<String> get() = _homeUnReadObservable
    private val _homeUnReadObservable = MutableLiveData<String>()

    fun checkUnreadMsg() {
        var unreadCount = 0
        if (inviteMessageDao != null) {
            unreadCount = inviteMessageDao.queryUnreadCount()
        }
        val unreadMessageCount = SdkHelper.instance.chatManager.unreadMessageCount
        val count = getUnreadCount(unreadCount + unreadMessageCount)
        _homeUnReadObservable.postValue(count)
    }

    /**
     * 获取未读消息数目
     * @param count
     * @return
     */
    private fun getUnreadCount(count: Int): String {
        if (count <= 0) {
            return ""
        }
        return if (count > 99) {
            "99+"
        } else count.toString()
    }

}