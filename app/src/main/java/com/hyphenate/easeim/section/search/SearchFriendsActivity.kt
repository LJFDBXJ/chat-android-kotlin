package com.hyphenate.easeim.section.search

import android.content.Context
import android.os.Bundle
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.db.DbHelper
import com.hyphenate.easeim.common.ktx.jump
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.section.ui.contact.activity.ContactDetailActivity
import com.hyphenate.easeim.section.ui.contact.adapter.ContactListAdapter
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.manager.EaseThreadManager

open class SearchFriendsActivity : SearchActivity() {
    var mData: List<EaseUser>? = null

    val adapter by lazy { ContactListAdapter() }
    var result = ArrayList<EaseUser>()
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.titleBar.setTitle(getString(R.string.em_search_contact))
        adapter.setOnItemClickListener { _, _, position ->
            val item = adapter.getItem(position)
            ContactDetailActivity.actionStart(this, item)
            toast(R.string.long_press_entry_to_remove_blacklist)
        }
    }


    override fun searchMessages(search: String) {
        if (mData.isNullOrEmpty() || search.isEmpty()) {
            return
        }
        EaseThreadManager.getInstance().runOnIOThread {
            result.clear()
            mData?.forEach { user ->
                if (user.username.contains(search) || user.nickname.contains(search)) {
                    result.add(user)
                }
            }
            adapter.setList(result)
        }
    }

    override fun initData() {
        super.initData()
        result.clear()
        mData = DbHelper.dbHelper().userDao?.loadAllContactUsers()
    }

    companion object {
        fun actionStart(context: Context) {
            context.jump<SearchFriendsActivity>()
        }
    }
}