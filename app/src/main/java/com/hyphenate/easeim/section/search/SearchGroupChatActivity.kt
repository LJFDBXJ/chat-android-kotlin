package com.hyphenate.easeim.section.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeim.R
import com.hyphenate.easeim.section.ui.chat.activity.ChatHistoryActivity
import com.hyphenate.easeim.section.search.adapter.SearchMessageAdapter
import com.hyphenate.easeui.constants.EaseConstant

class SearchGroupChatActivity : SearchActivity() {
    private var toUsername: String = ""
    private var conversation: EMConversation? = null
    private val adapter by lazy { SearchMessageAdapter() }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        toUsername = intent.getStringExtra("toUsername") ?: ""

        binding.titleBar.setTitle(getString(R.string.em_search_group_chat))
        adapter.setOnItemClickListener { _, _, position ->
            val item = adapter.getItem(position)
            ChatHistoryActivity.actionStart(
                context = this,
                userId = toUsername,
                chatType = EaseConstant.CHATTYPE_GROUP,
                historyMsgId = item.msgId
            )
        }
    }

    override fun initData() {
        super.initData()
        conversation = EMClient.getInstance().chatManager()
            .getConversation(toUsername, EMConversation.EMConversationType.GroupChat, true)
    }

    override fun searchMessages(search: String) {
        val mData = conversation?.searchMsgFromDB(
            search,
            System.currentTimeMillis(),
            100,
            null,
            EMConversation.EMSearchDirection.UP
        )
        adapter.setKeyword(keyword = search)
        adapter.setList(mData)
    }


    companion object {
        fun actionStart(context: Context, toUsername: String?) {
            val intent = Intent(context, SearchGroupChatActivity::class.java)
            intent.putExtra("toUsername", toUsername)
            context.startActivity(intent)
        }
    }
}