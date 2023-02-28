package com.hyphenate.easeim.section.search

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import com.hyphenate.chat.EMConversation
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback
import com.hyphenate.easeim.common.ktx.jump
import com.hyphenate.easeim.section.ui.conversation.adapter.HomeAdapter
import com.hyphenate.easeim.section.ui.conversation.vm.ConversationListVm

/**
 * 搜索 成员
 */
class SearchConversationActivity : SearchActivity() {
    private var mData: List<Any>? = null
    private var result = ArrayList<Any>()
    private val adapter by lazy { HomeAdapter() }
    private val viewModel by viewModels<ConversationListVm>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.titleBar.setTitle(getString(R.string.em_search_conversation))
        binding.rvList.adapter = adapter
    }

    override fun initData() {
        super.initData()
        viewModel.conversation.observe(this) { response ->
            parseResource(
                response = response,
                callback = object : OnResourceParseCallback<List<Any>?>() {
                    override fun onSuccess(data: List<Any>?) {
                        mData = data
                    }
                })
        }
        viewModel.loadConversationList()
    }

    override fun searchMessages(search: String) {
        searchResult(search = search)
    }

    private fun searchResult(search: String) {
        if (mData.isNullOrEmpty()) {
            return
        }
        result.clear()

        mData?.forEach { item ->
            if (item is EMConversation) {
                val username = item.conversationId()
                when (item.type) {
                    EMConversation.EMConversationType.GroupChat -> {
                        val group = SdkHelper.instance.groupManager.getGroup(username)
                        if (group?.groupName?.contains(search) == true) {
                            result.add(item)
                        } else if (username.contains(search)) {
                            result.add(item)
                        }
                    }
                    EMConversation.EMConversationType.ChatRoom -> {
                        val chatRoom =
                            SdkHelper.instance.chatroomManager.getChatRoom(username)
                        if (chatRoom?.name?.contains(search) == true) {
                            result.add(item)
                        } else if (username.contains(search)) {
                            result.add(item)
                        }
                    }
                    else -> {
                        if (username.contains(search))
                            result.add(item)
                    }
                }

            }
        }
        adapter.setList(result)
    }

    companion object {
        fun actionStart(context: Context) {
            context.jump<SearchConversationActivity>()
        }
    }
}