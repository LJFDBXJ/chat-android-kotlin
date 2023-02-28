package com.hyphenate.easeim.section.ui.group.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.databinding.ActivityGroupManageBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.group.GroupHelper
import com.hyphenate.easeim.section.ui.group.activity.GroupMemberAuthorityActivity
import com.hyphenate.easeim.section.ui.group.vm.GroupMemberAuthorityVm
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener

class GroupManageIndexActivity(override val layoutId: Int = R.layout.activity_group_manage) :
    BaseInitActivityKtx<ActivityGroupManageBinding>(), View.OnClickListener,
    OnBackPressListener {
    private var groupId: String = ""


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        groupId = intent.getStringExtra("groupId") ?: ""
        val isOwner = GroupHelper.isOwner(SdkHelper.instance.groupManager.getGroup(groupId))
        binding.btnTransfer.visibility = if (isOwner)
            View.VISIBLE
        else
            View.GONE
    }

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener(this)
        binding.itemBlackManager.setOnClickListener(this)
        binding.itemMuteManage.setOnClickListener(this)
        binding.btnTransfer.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.item_black_manager ->
                GroupMemberAuthorityActivity.actionStart(
                    context = this,
                    groupId = groupId,
                    type = GroupMemberAuthorityActivity.TYPE_BLACK
                )
            R.id.item_mute_manage ->
                GroupMemberAuthorityActivity.actionStart(
                    context = this,
                    groupId = groupId,
                    type = GroupMemberAuthorityActivity.TYPE_MUTE
                )
            R.id.btn_transfer ->
                GroupTransferActivity.actionStart(
                    context = this,
                    groupId = groupId
                )
        }
    }

    override fun initData() {
        super.initData()
        val viewModel by viewModels<GroupMemberAuthorityVm>()
        viewModel.muteMembers.observe(this) { response ->
            parseResource(
                response = response,
                callback = object : OnResourceParseCallback<Map<String, Long>?>() {
                    override fun onSuccess(data: Map<String, Long>?) {
                        binding.itemMuteManage.tvContent.text = data!!.size.toString() + "个"
                    }
                })
        }
        viewModel.blackMembers.observe(this) { response ->
            parseResource(
                response = response,
                callback = object : OnResourceParseCallback<List<String>?>() {
                    override fun onSuccess(data: List<String>?) {
                        binding.itemBlackManager.tvContent.text = data?.size.toString() + "个"
                    }
                })
        }
        DemoConstant.GROUP_CHANGE.obs<EaseEvent>(this) { event ->
            event ?: return@obs
            if (event.event == DemoConstant.GROUP_OWNER_TRANSFER) {
                finish()
                return@obs
            }
            if (event.isGroupChange) {
                viewModel.getBlackMembers(groupId = groupId)
                viewModel.getMuteMembers(groupId = groupId)
            }
        }
        viewModel.getBlackMembers(groupId = groupId)
        viewModel.getMuteMembers(groupId = groupId)
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    companion object {
        fun actionStart(context: Context, groupId: String?) {
            val intent = Intent(context, GroupManageIndexActivity::class.java)
            intent.putExtra("groupId", groupId)
            context.startActivity(intent)
        }
    }
}