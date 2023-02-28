package com.hyphenate.easeim.section.ui.group.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.section.ui.group.GroupHelper
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent

class GroupAdminAuthorityActivity : GroupMemberAuthorityActivity() {
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.titleBar.setTitle(getString(R.string.em_authority_menu_admin_list))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        return false
    }

    override val data: Unit
        get() {
            viewModel.refresh.observe(this) { response ->
                parseResource(
                    response = response,
                    callback = object : OnResourceParseCallback<String>() {
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
            viewModel.admin.observe(this) {
                it ?: return@observe
                var adminList = group?.adminList
                if (adminList == null) {
                    adminList = ArrayList()
                }
                adminList.add(it.owner)
                val users = ArrayList<EaseUser>()
                if (adminList.isNotEmpty()) {
                    for (i in adminList.indices) {
                        val user = SdkHelper.instance.getUserInfo(adminList[i])
                        if (user != null) {
                            users.add(user)
                        } else {
                            users.add(EaseUser(adminList[i]!!))
                        }
                    }
                }
                adapter.setList(users)
                finishRefresh()

            }
            DemoConstant.GROUP_CHANGE.obs<EaseEvent>(this) { event ->
                if (event == null) {
                    return@obs
                }
                if (event.isGroupChange) {
                    refreshData()
                } else if (event.isGroupLeave && TextUtils.equals(groupId, event.message)) {
                    finish()
                }
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
            refreshData()
        }

    override fun refreshData() {
        viewModel.getGroup(groupId)
    }

    override fun onItemLongClick(m: BaseQuickAdapter<*, *>, view: View, position: Int): Boolean {
        val username = adapter.getItem(position).username
        //不能操作群主
        if (TextUtils.equals(group!!.owner, username)) {
            return false
        }
        //管理员不能操作
        return if (GroupHelper.isAdmin(group = group)) {
            false
        } else
            super.onItemLongClick(m, view, position)
    }

    companion object {
        fun actionStart(context: Context, groupId: String?) {
            val starter = Intent(context, GroupAdminAuthorityActivity::class.java)
            starter.putExtra("groupId", groupId)
            context.startActivity(starter)
        }
    }
}