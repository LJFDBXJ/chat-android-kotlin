package com.hyphenate.easeim.section.ui.group.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMGroup
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.databinding.ActivityGroupSimleDetailsBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.chat.ChatActivity
import com.hyphenate.easeim.section.ui.contact.vm.PublicGroupVm
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener

class GroupSimpleDetailActivity(override val layoutId: Int = R.layout.activity_group_simle_details) :
    BaseInitActivityKtx<ActivityGroupSimleDetailsBinding>(), View.OnClickListener,
    OnBackPressListener {
    private var groupId: String? = null
    private var group: EMGroup? = null
    private val viewModel by viewModels<PublicGroupVm>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        groupId = intent.getStringExtra("groupId")
    }

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener(this)
        binding.btnAddToGroup.setOnClickListener(this)
    }

    override fun initData() {
        super.initData()

        viewModel.group.observe(this) {
            group = it
            it?.let {
                setGroupInfo(it)
            }
        }
        viewModel.join.observe(this) {
            group ?: return@observe
            if (it) {
                if (group?.isMemberOnly == true) {
                    toast(R.string.send_the_request_is)
                } else {
                    toast(R.string.Join_the_group_chat)
                    ChatActivity.actionStart(
                        context = this@GroupSimpleDetailActivity,
                        conversationId = group!!.groupId,
                        chatType = DemoConstant.CHATTYPE_GROUP
                    )
                }
                finish()
            }
        }
        if (group != null) {
            viewModel.getGroup(groupId = groupId)
            group = SdkHelper.instance.groupManager.getGroup(groupId)
            setGroupInfo(group = group!!)
        }
    }

    private fun setGroupInfo(group: EMGroup) {
        binding.itemGroupName.tvContent.text = group.groupName
        binding.itemGroupOwner.tvContent.text = group.owner
        binding.tvGroupIntroduction.text = group.description
        if (!group.members.contains(EMClient.getInstance().currentUser)) {
            binding.btnAddToGroup.isEnabled = true
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_add_to_group ->
                addToGroup()
        }
    }

    private fun addToGroup() {
        var reason = binding.etReason.text.toString()
        if (reason.isEmpty()) {
            reason = getString(
                R.string.demo_group_listener_onRequestToJoinReceived,
                SdkHelper.instance.currentUser,
                group?.groupName
            )
        }
        group?.let {
            viewModel.joinGroup(emGroup = it, reason = reason)
        }
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    companion object {
        @JvmStatic
        fun actionStart(context: Context, groupId: String?) {
            val starter = Intent(context, GroupSimpleDetailActivity::class.java)
            starter.putExtra("groupId", groupId)
            context.startActivity(starter)
        }
    }
}