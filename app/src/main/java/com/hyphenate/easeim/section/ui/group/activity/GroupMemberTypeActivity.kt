package com.hyphenate.easeim.section.ui.group.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.hyphenate.chat.EMGroup
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.databinding.ActivityChatGroupMemberTypeBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.group.activity.GroupMemberAuthorityActivity.Companion.actionStart
import com.hyphenate.easeim.section.ui.group.vm.GroupMemberAuthorityVm
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener

/**
 * @author LXJDBXJ
 * @date 2022/10/9
 * 群主成员管理
 */
class GroupMemberTypeActivity(override val layoutId: Int = R.layout.activity_chat_group_member_type) :
    BaseInitActivityKtx<ActivityChatGroupMemberTypeBinding>(),
    OnBackPressListener, View.OnClickListener {
    private var groupId: String? = null
    private var group: EMGroup? = null
    private var isOwner = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        groupId = intent.getStringExtra("groupId")
        isOwner = intent.getBooleanExtra("isOwner", false)
        group = SdkHelper.instance.groupManager.getGroup(groupId)
        initGroupData(group)
    }

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener(this)
        binding.itemAdmin.setOnClickListener(this)
        binding.itemMember.setOnClickListener(this)
    }

    override fun initData() {
        super.initData()
        val viewModel by viewModels<GroupMemberAuthorityVm>()
        viewModel.admin.observe(this) {
            initGroupData(it)
        }
        viewModel.members.observe(this) {
            setMemberCount(it?.size ?: 0)
        }

        DemoConstant.GROUP_CHANGE.obs<EaseEvent>(this) { event ->
            event ?: return@obs
            if (event.isGroupChange) {
                viewModel.getGroup(groupId)
                viewModel.getMembers(groupId)
            } else if (event.isGroupLeave && groupId == event.message) {
                finish()
            }
        }

        viewModel.getMembers(groupId)
    }

    private fun initGroupData(group: EMGroup?) {
        group ?: return
        setAdminCount(group.adminList.size + 1)
        setMemberCount(group.members.size)
    }

    private fun setAdminCount(count: Int) {
        binding.itemAdmin.tvContent.text =
            getString(R.string.em_group_member_type_member_num, count)
    }

    private fun setMemberCount(count: Int) {
        binding.itemMember.tvContent.text =
            getString(R.string.em_group_member_type_member_num, count)
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemAdmin -> GroupAdminAuthorityActivity.actionStart(
                context = this,
                groupId = groupId
            )
            R.id.itemMember -> actionStart(context = this, groupId = groupId)
        }
    }

    companion object {
        fun actionStart(context: Context, groupId: String?, owner: Boolean) {
            val starter = Intent(context, GroupMemberTypeActivity::class.java)
            starter.putExtra("groupId", groupId)
            starter.putExtra("isOwner", owner)
            context.startActivity(starter)
        }
    }
}