package com.hyphenate.easeim.common.repositories

import android.text.TextUtils
import androidx.lifecycle.LiveData
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.net.Resource
import com.hyphenate.easeim.section.conference.ContactState
import com.hyphenate.easeui.manager.EaseThreadManager
import com.hyphenate.exceptions.HyphenateException
import com.hyphenate.util.EasyUtils

class EMConferenceManagerRepository : BaseEMRepository() {
    fun getConferenceMembers(
        groupId: String?,
        existMember: Array<String>?,
        callBack: EMValueCallBack<ArrayList<ContactState>>
    ) {
        runOnIOThread {
            var contactList = ArrayList<String>()
            if (TextUtils.isEmpty(groupId)) {
                //从本地加载好友联系人
                val result = userDao?.loadContactUsers()
                if (!result.isNullOrEmpty()) {
                    contactList.addAll(result)
                }
            } else {
                // 根据groupId获取群组中所有成员
                contactList = EMGroupManagerRepository().getAllGroupMemberByServer(groupId)
            }
            //获取管理员列表
            try {
                val group =
                    EMClient.getInstance().groupManager().getGroupFromServer(groupId, true)
                if (group != null) {
                    if (group.adminList != null) {
                        contactList.addAll(group.adminList)
                    }
                    contactList.add(group.owner)
                }
            } catch (e: HyphenateException) {
                e.printStackTrace()
            }
            val contacts = ArrayList<ContactState>()
            for (it in contactList) {
                if (it != DemoConstant.NEW_FRIENDS_USERNAME
                    && it != DemoConstant.GROUP_USERNAME
                    && it != DemoConstant.CHAT_ROOM
                    && it != DemoConstant.CHAT_ROBOT
                    && it != currentUser
                ) {
                    if (memberContains(existMember, it)) {
                        contacts.add(ContactState(it, true))
                    } else {
                        contacts.add(ContactState(it, false))
                    }
                }
            }
            callBack.onSuccess(contacts)
        }
    }

    private fun memberContains(existMember: Array<String>?, name: String): Boolean {
        if (!existMember.isNullOrEmpty()) {
            for (userId in existMember) {
                if (TextUtils.equals(EasyUtils.useridFromJid(userId), name)) {
                    return true
                }
            }
        }
        return false
    }
}