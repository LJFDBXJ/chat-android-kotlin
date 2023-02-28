package com.hyphenate.easeim.section.ui.contact.activity

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
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.databinding.ActivityContactBlackListBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.search.SearchBlackActivity
import com.hyphenate.easeim.section.ui.contact.adapter.ContactListAdapter
import com.hyphenate.easeim.section.ui.contact.vm.ContactBlackVm
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.widget.EaseRecyclerView.RecyclerViewContextMenuInfo

class ContactBlackListActivity(override val layoutId: Int = R.layout.activity_contact_black_list) :
    BaseInitActivityKtx<ActivityContactBlackListBinding>() {
    private val adapter by lazy { ContactListAdapter() }
    private val viewModel by viewModels<ContactBlackVm>()
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.rvList.adapter = adapter
        registerForContextMenu(binding.rvList)

        adapter.setOnItemClickListener { _, _, position ->
            val user = adapter.getItem(position)
            ContactDetailActivity.actionStart(
                context = this,
                user = user,
                isFriend = SpDbModel.instance.isContact(user.username)
            )
        }
        adapter.setOnItemLongClickListener { _, _, _ ->
            return@setOnItemLongClickListener false
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.menu_black_list_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        super.onContextItemSelected(item)
        val position = (item.menuInfo as RecyclerViewContextMenuInfo).position
        val user = adapter.getItem(position)
        when (item.itemId) {
            R.id.action_friend_unblock -> unBlock(user)
        }
        return true
    }

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener {
            onBackPressed()
        }
        binding.srlRefresh.setOnRefreshListener {
            viewModel.getBlackList()
        }
        binding.searchBlack.setOnClickListener {
            SearchBlackActivity.actionStart(this)
        }
        viewModel.black.observe(this) {
            adapter.setList(it)
            binding.srlRefresh.finishRefresh()
        }

        viewModel.result.observe(this) {
            if (it) {
                toast(R.string.em_friends_move_out_blacklist_success)
                LiveDataBus.get().use(DemoConstant.REMOVE_BLACK).postValue(
                    EaseEvent.create(
                        DemoConstant.REMOVE_BLACK,
                        EaseEvent.TYPE.CONTACT
                    )
                )
            }
        }

        arrayOf(
            DemoConstant.REMOVE_BLACK,
            DemoConstant.CONTACT_CHANGE
        ).obs<EaseEvent>(this) { _, event ->
            event ?: return@obs
            if (event.isContactChange) {
                viewModel.getBlackList()
            }
        }

    }

    override fun initData() {
        super.initData()
        viewModel.getBlackList()
    }

    private fun unBlock(user: EaseUser) {
        viewModel.removeUserFromBlackList(user.username)
    }

    companion object {
        fun actionStart(context: Context) {
            context.jump<ContactBlackListActivity>()
        }
    }
}