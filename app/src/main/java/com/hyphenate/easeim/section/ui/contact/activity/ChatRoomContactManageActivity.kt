package com.hyphenate.easeim.section.ui.contact.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.ktx.jump
import com.hyphenate.easeim.databinding.ActivityFriendsChatRoomContactManageBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.search.SearchChatRoomActivity
import com.hyphenate.easeim.section.ui.contact.fragment.ChatRoomContactManageFragment
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener
/**
 * @author LXJDBXJ
 * @date 2022/10/10/ 22:20
 * @desc 首页 消息 ->聊天室 host
 */
class ChatRoomContactManageActivity(override val layoutId: Int = R.layout.activity_friends_chat_room_contact_manage) :
    BaseInitActivityKtx<ActivityFriendsChatRoomContactManageBinding>(), OnBackPressListener,
    View.OnClickListener {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container_fragment, ChatRoomContactManageFragment(), "chat_room_contact")
            .commit()
    }

    override fun initListener() {
        super.initListener()
        binding.titleBarChatRoomContact.setOnBackPressListener(this)
        binding.searchChatRoom.setOnClickListener(this)
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.search_chat_room ->
                SearchChatRoomActivity.actionStart(this)
        }
    }

    companion object {
        fun actionStart(context: Context) {
            context.jump<ChatRoomContactManageActivity>()
        }
    }
}