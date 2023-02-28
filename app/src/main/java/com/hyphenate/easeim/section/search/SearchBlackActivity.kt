package com.hyphenate.easeim.section.search

import android.content.Context
import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.ktx.jump
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.section.ui.contact.vm.ContactBlackVm
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.widget.EaseRecyclerView.RecyclerViewContextMenuInfo

class SearchBlackActivity : SearchFriendsActivity() {
    private val viewModel by viewModels<ContactBlackVm>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.titleBar.setTitle(getString(R.string.em_search_black))
        registerForContextMenu(binding.rvList)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.menu_black_list_menu, menu)
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {
        val position = (item.menuInfo as RecyclerViewContextMenuInfo).position
        val user = adapter.getItem(position) as EaseUser
        when (item.itemId) {
            R.id.action_friend_unblock -> unBlock(user)
        }
        return super.onContextItemSelected(item)
    }

    override fun initData() {
        super.initData()
        result = ArrayList()
        viewModel.black.observe(this) { response ->
            mData = response
            val search = binding.query.text.toString()
            searchMessages(search)
        }
        viewModel.result.observe(this) {
            if (it) {
                viewModel.getBlackList()
                LiveDataBus.get().use(DemoConstant.CONTACT_CHANGE)
                    .postValue(
                        EaseEvent.create(
                            DemoConstant.CONTACT_CHANGE,
                            EaseEvent.TYPE.CONTACT
                        )
                    )
            }
        }
        viewModel.getBlackList()
    }


    private fun unBlock(user: EaseUser) {
        viewModel.removeUserFromBlackList(user.username)
    }

    companion object {
        fun actionStart(context: Context) {
            context.jump<SearchBlackActivity>()
        }
    }
}