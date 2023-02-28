package com.hyphenate.easeim.section.ui.group.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.section.ui.group.GroupHelper
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent

class ChatRoomAdminAuthorityActivity : ChatRoomMemberAuthorityActivity() {
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.titleBar.setTitle(getString(R.string.em_authority_menu_admin_list))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        return false
    }

    //adapter.setData(EmUserEntity.parse(adminList));
    override val data: Unit
        get() {
            roomViewModel.chatRoomObservable.observe(this) { response ->
                response ?: return@observe
                var adminList = response.adminList
                if (adminList == null) {
                    adminList = ArrayList()
                }
                adminList.add(response.owner)
                //adapter.setData(EmUserEntity.parse(adminList));
                val users = ArrayList<EaseUser>()
                if (adminList.isNotEmpty()) {
                    for (i in adminList.indices) {
                        val user = SdkHelper.instance.getUserInfo(username = adminList[i])
                        if (user != null) {
                            users.add(user)
                        } else {
                            users.add(EaseUser(adminList[i]))
                        }
                    }
                }
                adapter.data.clear()
                adapter.addData(users)
            }
            DemoConstant.CHAT_ROOM_CHANGE.obs<EaseEvent>(this) { event ->
                event ?: return@obs
                if (event.type == EaseEvent.TYPE.CHAT_ROOM) {
                    refreshData()
                }
                if (event.isChatRoomLeave && roomId == event.message) {
                    finish()
                }
            }
            refreshData()
        }

    override fun refreshData() {
        roomViewModel.getChatRoom(roomId)
    }

    override fun onItemLongClick(m: BaseQuickAdapter<*, *>, view: View, position: Int): Boolean {
        val username = adapter.getItem(position).username
        //不能操作群主
        if ((chatRoom?.owner == username)) {
            return false
        }
        //管理员不能操作
        return if (GroupHelper.isAdmin(chatRoom)) {
            false
        } else
            super.onItemLongClick(m, view, position)
    }

    companion object {
        fun actionStart(context: Context, roomId: String?) {
            val starter = Intent(context, ChatRoomAdminAuthorityActivity::class.java)
            starter.putExtra("roomId", roomId)
            context.startActivity(starter)
        }
    }
}