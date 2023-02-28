package com.hyphenate.easeim.section.search

import android.content.Context
import android.os.Bundle
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMGroup
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.ktx.jump
import com.hyphenate.easeim.section.ui.chat.ChatActivity
import com.hyphenate.easeim.section.ui.contact.adapter.GroupContactAdapter
import com.hyphenate.easeui.manager.EaseThreadManager

class SearchGroupActivity : SearchActivity() {
    private var mData: List<EMGroup>? = null
    private var result = ArrayList<EMGroup>()
    private val adapter = GroupContactAdapter()
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.titleBar.setTitle(getString(R.string.em_search_group))
        binding.rvList.adapter = adapter
    }


    override fun initData() {
        super.initData()

        mData = EMClient.getInstance().groupManager().allGroups
        adapter.setOnItemClickListener { _, _, position ->
            val item = adapter.getItem(position)
            ChatActivity.actionStart(
                context = this,
                conversationId = item.groupId,
                chatType = DemoConstant.CHATTYPE_GROUP
            )
        }
    }

    override fun searchMessages(search: String) {
        if (mData.isNullOrEmpty()) {
            return
        }
        result.clear()
        mData?.forEach { group ->
            if (group.groupName.contains(search) || group.groupId.contains(search)) {
                result.add(group)
            }
        }
        adapter.setList(result)
    }

    companion object {
        @JvmStatic
        fun actionStart(context: Context) {
            context.jump<SearchGroupActivity>()
        }
    }
}