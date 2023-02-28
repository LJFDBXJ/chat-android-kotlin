package com.hyphenate.easeim.section.ui.group.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.chad.library.adapter.base.BaseQuickAdapter
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent

class GroupTransferActivity : GroupMemberAuthorityActivity() {
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.titleBar.setTitle(getString(R.string.em_chat_group_authority_transfer))
    }

    override val data: Unit
        get() {
            viewModel.admin.observe(this) {
                var adminList = it?.adminList
                if (adminList == null) {
                    adminList = ArrayList()
                }
                adapter.data = EaseUser.parse(adminList)
                viewModel.getMembers(groupId)
                finishRefresh()

            }
            viewModel.transferOwner.observe(this) { response ->
                parseResource<Boolean>(
                    response!!, object : OnResourceParseCallback<Boolean>() {
                        override fun onSuccess(data: Boolean?) {
                            LiveDataBus.get().use(DemoConstant.GROUP_CHANGE).postValue(
                                EaseEvent.create(
                                    DemoConstant.GROUP_OWNER_TRANSFER,
                                    EaseEvent.TYPE.GROUP
                                )
                            )
                            finish()
                        }
                    })
            }
            viewModel.refresh.observe(this) { response ->
                parseResource<String>(
                    response!!, object : OnResourceParseCallback<String>() {
                        override fun onSuccess(message: String?) {
                            refreshData()
                            LiveDataBus.get().use(DemoConstant.GROUP_CHANGE).postValue(
                                EaseEvent.create(
                                    DemoConstant.GROUP_CHANGE,
                                    EaseEvent.TYPE.GROUP
                                )
                            )
                        }
                    })
            }
            viewModel.members.observe(this) {
                adapter.setList(it)
                finishRefresh()
            }
            DemoConstant.GROUP_CHANGE.obs<EaseEvent>(this) { event: EaseEvent? ->
                event ?: return@obs
                if (event.isGroupChange) {
                    refreshData()
                } else if (event.isGroupLeave && groupId == event.message) {
                    finish()
                }
            }
            refreshData()
        }

    override fun refreshData() {
        viewModel.getGroup(groupId)
    }

    override fun onItemLongClick(m: BaseQuickAdapter<*, *>, view: View, position: Int): Boolean {
        if (isMember) {
            return false
        }
        val menu = PopupMenu(this, view)
        menu.gravity = Gravity.CENTER_HORIZONTAL
        menu.menuInflater.inflate(R.menu.demo_group_member_authority_item_menu, menu.menu)
//        val menuPopupHelper = MenuPopupHelper(this, (menu.menu as MenuBuilder), view)
//        menuPopupHelper.setForceShowIcon(true)
//        menuPopupHelper.gravity = Gravity.CENTER_HORIZONTAL
        val item = adapter.getItem(position) ?: return false
        val username = item.username
        setMenuInfo(menu.menu)
        if (isInAdminList(username)) {
            setMenuItemVisible(menu.menu, R.id.action_group_remove_admin)
            setMenuItemVisible(menu.menu, R.id.action_group_transfer_owner)
        } else {
            menu.menu.findItem(R.id.action_group_add_admin).isVisible = isOwner
            setMenuItemVisible(menu.menu, R.id.action_group_transfer_owner)
        }
        menu.show()
//        menuPopupHelper.show()
        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_group_add_admin -> addToAdmins(username)
                R.id.action_group_remove_admin -> removeFromAdmins(username)
                R.id.action_group_transfer_owner -> transferOwner(username)
            }
            false
        }
        return true
    }

    companion object {
        fun actionStart(context: Context, groupId: String?) {
            val starter = Intent(context, GroupTransferActivity::class.java)
            starter.putExtra("groupId", groupId)
            context.startActivity(starter)
        }
    }
}