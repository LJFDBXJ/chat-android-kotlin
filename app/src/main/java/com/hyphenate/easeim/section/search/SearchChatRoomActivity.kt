package com.hyphenate.easeim.section.search

import android.content.Context
import android.os.Bundle
import com.hyphenate.chat.EMChatRoom
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.ktx.jump
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.section.ui.contact.adapter.ChatRoomContactAdapter
import com.hyphenate.easeui.manager.EaseThreadManager

class SearchChatRoomActivity : SearchActivity() {
    private var mData: List<EMChatRoom>? = null
    private var result = ArrayList<EMChatRoom>()
    private val adapter by lazy { ChatRoomContactAdapter() }
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.titleBar.setTitle(getString(R.string.em_search_chat_room))
        binding.rvList.adapter = adapter
    }

    override fun initData() {
        super.initData()
        mData = SpDbModel.instance.chatRooms
    }

    override fun searchMessages(search: String) {
        if (mData.isNullOrEmpty()) {
            return
        }
        result.clear()
        mData?.forEach { room ->
            if (room.name.contains(search) || room.id.contains(search)) {
                result.add(room)
            }
        }
        adapter.setList(result)
    }


    companion object {
        @JvmStatic
        fun actionStart(context: Context) {
            context.jump<SearchChatRoomActivity>()
        }
    }
}