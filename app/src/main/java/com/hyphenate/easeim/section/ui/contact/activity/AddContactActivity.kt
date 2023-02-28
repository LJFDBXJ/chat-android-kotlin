package com.hyphenate.easeim.section.ui.contact.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.db.DbHelper
import com.hyphenate.easeim.common.enums.SearchType
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.section.search.SearchActivity
import com.hyphenate.easeim.section.ui.contact.adapter.AddContactAdapter
import com.hyphenate.easeim.section.ui.contact.vm.AddContactVm
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener

class AddContactActivity : SearchActivity(), OnBackPressListener {
    private val mViewModel by viewModels<AddContactVm>()
    private var mType: SearchType? = null

    private val adapter by lazy { AddContactAdapter(model = mViewModel) }
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.rvList.adapter = adapter
        mType = intent.getSerializableExtra("type") as SearchType?
        binding.titleBar.setTitle(getString(R.string.em_search_add_contact))
        binding.query.hint = getString(R.string.em_search_add_contact_hint)
    }

    override fun initData() {
        super.initData()
        mViewModel.addContact.observe(this) {
            toast(R.string.em_add_contact_send_successful)
        }
        //获取本地的好友列表
        val localUsers = DbHelper.dbHelper().userDao?.loadContactUsers()
        adapter.addLocalContacts(contacts = localUsers)
    }


    override fun searchMessages(search: String) {
        // you can search the user from your app server here.
        if (search.isNotEmpty()) {
            adapter.data.clear()
            adapter.addData(search)
        }
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    companion object {
        fun startAction(context: Context, type: SearchType) {
            val intent = Intent(context, AddContactActivity::class.java)
            intent.putExtra("type", type)
            context.startActivity(intent)
        }
    }


}