package com.hyphenate.easeim.section.ui.group.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.hyphenate.chat.EMGroupManager.EMGroupStyle
import com.hyphenate.chat.EMGroupOptions
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.widget.SwitchItemView
import com.hyphenate.easeim.databinding.ActivityNewGroupBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.chat.ChatActivity
import com.hyphenate.easeim.section.dialog_ktx.EditTextDialogFragment
import com.hyphenate.easeim.section.dialog_ktx.SimpleDialogFragment
import com.hyphenate.easeim.section.ui.group.fragment.GroupEditFragment
import com.hyphenate.easeim.section.ui.contact.vm.NewGroupVm
import com.hyphenate.easeui.model.EaseEvent

class NewGroupActivity(override val layoutId: Int = R.layout.activity_new_group) :
    BaseInitActivityKtx<ActivityNewGroupBinding>(), View.OnClickListener,
    SwitchItemView.OnCheckedChangeListener {
    private var maxUsers = 200
    private val viewModel by viewModels<NewGroupVm>()
    private var newmembers: Array<String>? = null
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        newmembers = intent.getStringArrayExtra("newMembers")
        binding.itemGroupName.tvContent.hint = getString(R.string.em_group_new_name_hint)
        binding.itemGroupProfile.tvContent.hint = getString(R.string.em_group_new_profile_hint)
        binding.itemGroupMaxUsers.tvContent.text = maxUsers.toString()
        binding.titleBar.rightText
            .setTextColor(ContextCompat.getColor(this, R.color.em_color_brand))
    }

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener {
            onBackPressed()
        }
        binding.titleBar.setOnRightClickListener {
            checkGroupInfo()
        }
        binding.itemGroupName.setOnClickListener(this)
        binding.itemGroupProfile.setOnClickListener(this)
        binding.itemGroupMaxUsers.setOnClickListener(this)
        binding.itemGroupMembers.setOnClickListener(this)
        binding.itemSwitchPublic.setOnCheckedChangeListener(this)
        binding.itemSwitchInvite.setOnCheckedChangeListener(this)
    }

    override fun initData() {
        super.initData()
        viewModel.group.observe(this) { group ->
            group ?: return@observe
            toast(R.string.em_group_new_success)
            LiveDataBus.get().use(DemoConstant.GROUP_CHANGE).postValue(
                EaseEvent.create(
                    DemoConstant.GROUP_CHANGE,
                    EaseEvent.TYPE.GROUP
                )
            )
            //跳转到群组聊天页面
            ChatActivity.actionStart(
                context = this@NewGroupActivity,
                conversationId = group.groupId,
                chatType = DemoConstant.CHATTYPE_GROUP
            )
            finish()
            dismissLoading()
        }
        if (newmembers != null) {
            setGroupMembersNum(newmembers?.size.toString() + "")
        } else {
            setGroupMembersNum("0")
        }
    }

    private fun checkGroupInfo() {
        val groupName = binding.itemGroupName.tvContent.text.toString().trim { it <= ' ' }
        if (newmembers.isNullOrEmpty()) {
            toast("请选择群成员")
            return
        }
        if (TextUtils.isEmpty(groupName)) {
            SimpleDialogFragment.Builder(this)
                .setTitle(R.string.em_group_new_name_cannot_be_empty)
                .show()
            return
        }
        if (maxUsers < MIN_GROUP_USERS || maxUsers > MAX_GROUP_USERS) {
            toast(R.string.em_group_new_member_limit)
            return
        }
        val desc = binding.itemGroupProfile.tvContent.text.toString()
        val option = EMGroupOptions().also {
            it.maxUsers = maxUsers
            it.inviteNeedConfirm = binding.itemNeedUserConfirm.switch.isChecked
            it.style = if (binding.itemSwitchPublic.switch.isChecked) {
                if (binding.itemSwitchInvite.switch.isChecked)
                    EMGroupStyle.EMGroupStylePublicJoinNeedApproval
                else
                    EMGroupStyle.EMGroupStylePublicOpenJoin
            } else {
                if (binding.itemSwitchInvite.switch.isChecked)
                    EMGroupStyle.EMGroupStylePrivateMemberCanInvite
                else
                    EMGroupStyle.EMGroupStylePrivateOnlyOwnerInvite
            }
        }
        val reason = getString(
            R.string.em_group_new_invite_join_group,
            SdkHelper.instance.currentUser,
            groupName
        )

        showLoading(getString(R.string.request))
        viewModel.createGroup(groupName, desc, newmembers!!, reason, option)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == ADD_NEW_CONTACTS) {
                if (data == null) {
                    setGroupMembersNum("0")
                    newmembers = null
                    return
                }
                newmembers = data.getStringArrayExtra("newMembers")
                if (newmembers != null) {
                    setGroupMembersNum(newmembers?.size.toString() + "")
                } else {
                    setGroupMembersNum("0")
                }
            }
        }
    }

    private fun setGroupMembersNum(num: String) {
        binding.itemGroupMembers.tvContent.text = num
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.item_group_name -> showGroupNameDialog()
            R.id.itemGroupProfile -> showProfileDialog()
            R.id.itemGroupMaxUsers -> setGroupMaxUsersDialog()
            R.id.item_group_members -> GroupPickContactsActivity.actionStartForResult(
                this,
                newmembers,
                ADD_NEW_CONTACTS
            )
        }
    }

    private fun showGroupNameDialog() {
        EditTextDialogFragment.Builder(this)
            .iniFragment()
            .setContent(binding.itemGroupName.tvContent.text.toString().trim { it <= ' ' })
            .setConfirmClickListener { _, content ->
                if (content.isNullOrEmpty()) {
                    toast("请输入有效的组名称")
                } else
                    binding.itemGroupName.tvContent.text = content

            }
            .setTitle(R.string.em_group_new_name_hint)
            .show()
    }

    private fun showProfileDialog() {
        GroupEditFragment.showDialog(
            this,
            getString(R.string.em_group_new_profile),
            binding.itemGroupProfile.tvContent.text.toString().trim { it <= ' ' },
            getString(R.string.em_group_new_profile_hint)
        ) { view, content -> //群简介
            if (!content.isNullOrEmpty()) {
                binding.itemGroupProfile.tvContent.text = content
            }
        }
    }

    private fun setGroupMaxUsersDialog() {
        EditTextDialogFragment.Builder(this)
            .iniFragment()
            .setContent(binding.itemGroupMaxUsers.tvContent.text.toString().trim { it <= ' ' })
            .setContentInputType(InputType.TYPE_CLASS_NUMBER)
            .setConfirmClickListener { _, content ->
                if (!TextUtils.isEmpty(content)) {
                    maxUsers = Integer.valueOf(content)
                    if (maxUsers > MAX_GROUP_USERS) {
                        maxUsers = Integer.valueOf(
                            binding.itemGroupMaxUsers.tvContent.text.toString()
                                .trim { it <= ' ' })
                        toast(R.string.maximum_of_group)
                        return@setConfirmClickListener
                    }
                    binding.itemGroupMaxUsers.tvContent.text = content
                }
            }
            .setTitle(R.string.em_group_set_max_users)
            .show()
    }

    override fun onCheckedChanged(buttonView: SwitchItemView, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.item_switch_public -> {
                //操作则将switch_invite按钮还原
                binding.itemSwitchInvite.switch.isChecked = false

                if (isChecked) {
                    binding.itemSwitchPublic.bind?.tvHint?.setText(R.string.em_group_new_if_public_check_hint)
                    binding.itemSwitchPublic.bind?.tvTitle?.setText(R.string.em_group_new_need_owner_approval_public)
                    binding.itemSwitchPublic.bind?.tvHint?.setText(R.string.em_group_new_need_owner_approval_uncheck_hint)
                } else {
                    binding.itemSwitchPublic.bind?.tvHint?.setText(R.string.em_group_new_if_public_uncheck_hint)
                    binding.itemSwitchPublic.bind?.tvTitle?.setText(R.string.em_group_new_open_invite)
                    binding.itemSwitchPublic.bind?.tvHint?.setText(R.string.em_group_new_open_invite_uncheck_hint)
                }

            }
            R.id.item_switch_invite -> {
                val textRes = if (binding.itemSwitchPublic.switch.isChecked) {
                    if (isChecked)
                        R.string.em_group_new_need_owner_approval_check_hint
                    else
                        R.string.em_group_new_need_owner_approval_uncheck_hint
                } else {
                    if (isChecked)
                        R.string.em_group_new_open_invite_check_hint
                    else
                        R.string.em_group_new_open_invite_uncheck_hint
                }
                binding.itemSwitchPublic.bind?.tvHint?.setText(textRes)
            }
        }
    }

    companion object {
        private const val ADD_NEW_CONTACTS = 10
        private const val MAX_GROUP_USERS = 3000
        private const val MIN_GROUP_USERS = 3
        fun actionStart(context: Context) {
            val starter = Intent(context, NewGroupActivity::class.java)
            context.startActivity(starter)
        }

        @JvmStatic
        fun actionStart(context: Context, newMembers: Array<String>?) {
            val intent = Intent(context, NewGroupActivity::class.java)
            intent.putExtra("newMembers", newMembers)
            context.startActivity(intent)
        }
    }
}