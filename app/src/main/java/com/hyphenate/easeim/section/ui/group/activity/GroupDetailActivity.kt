package com.hyphenate.easeim.section.ui.group.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMGroup
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.common.widget.SwitchItemView
import com.hyphenate.easeim.databinding.ActivityChatGroupDetailBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.dialog_ktx.EditTextDialogFragment
import com.hyphenate.easeim.section.dialog_ktx.SimpleDialogFragment
import com.hyphenate.easeim.section.ui.group.GroupHelper
import com.hyphenate.easeim.section.ui.group.fragment.GroupEditFragment.Companion.showDialog
import com.hyphenate.easeim.section.ui.group.vm.GroupDetailVm
import com.hyphenate.easeim.section.search.SearchGroupChatActivity
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener

/**
 * @author LXJDBXJ
 * @desc 群 详细设置
 * @date 2022/10/10 22:31
 */
class GroupDetailActivity(override val layoutId: Int = R.layout.activity_chat_group_detail) :
    BaseInitActivityKtx<ActivityChatGroupDetailBinding>(), OnBackPressListener,
    View.OnClickListener,
    SwitchItemView.OnCheckedChangeListener {
    private var groupId: String = ""
    private var group: EMGroup? = null
    private val viewModel by viewModels<GroupDetailVm>()
    private var conversation: EMConversation? = null

    private var launch: ActivityResultLauncher<Intent>? = null
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        groupId = intent.getStringExtra("groupId") ?: ""
        group = SdkHelper.instance.groupManager.getGroup(groupId)
        initGroupView()
        launch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            requestCode = GroupMemberAuthorityActivity.REQUEST_CODE_ADD_USER,

            if (it.resultCode == RESULT_OK) {
                loadGroup()
            }
        }
    }

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener(this)
        binding.tvGroupMemberTitle.setOnClickListener(this)
        binding.tvGroupMemberNum.setOnClickListener(this)
        binding.tvGroupInvite.setOnClickListener(this)
        binding.itemGroupName.setOnClickListener(this)
        binding.itemGroupShareFile.setOnClickListener(this)
        binding.itemGroupNotice.setOnClickListener(this)
        binding.itemGroupIntroduction.setOnClickListener(this)
        binding.itemGroupHistory.setOnClickListener(this)
        binding.itemGroupClearHistory.setOnClickListener(this)
        binding.itemGroupNotDisturb.setOnCheckedChangeListener(this)
        binding.itemGroupOffPush.setOnCheckedChangeListener(this)
        binding.itemGroupTop.setOnCheckedChangeListener(this)
        binding.tvGroupRefund.setOnClickListener(this)
        binding.itemGroupMemberManage.setOnClickListener(this)

        viewModel.group.observe(this) {
            group = it
            initGroupView()
        }
        viewModel.announcement.observe(this) {
            binding.itemGroupNotice.tvContent.text = it

        }
        viewModel.refresh.observe(this) {
            loadGroup()
        }
        DemoConstant.GROUP_CHANGE.obs<EaseEvent>(this) { event ->
            event ?: return@obs
            if (event.isGroupLeave && groupId == event.message) {
                finish()
                return@obs
            }
            if (event.isGroupChange) {
                loadGroup()
            }
        }

        viewModel.leaveGroup.observe(this) {
            finish()
            LiveDataBus.get().use(DemoConstant.GROUP_CHANGE).postValue(
                EaseEvent.create(
                    DemoConstant.GROUP_LEAVE,
                    EaseEvent.TYPE.GROUP,
                    groupId
                )
            )
        }
        viewModel.blockGroupMessage.observe(this) {
            //itemGroupNotDisturb.getSwitch().setChecked(true);

        }
        viewModel.unblockGroupMessage.observe(this) {
            //itemGroupNotDisturb.getSwitch().setChecked(false);
        }
        viewModel.offPushObservable().observe(this) {
            if (it) {
                loadGroup()
            }
        }
        viewModel.clearHistory.observe(this) {
            LiveDataBus.get().use(DemoConstant.CONVERSATION_DELETE)
                .postValue(EaseEvent(DemoConstant.CONTACT_DECLINE, EaseEvent.TYPE.MESSAGE))
        }
    }

    private fun initGroupView() {
        if (group == null) {
            finish()
            return
        }
        binding.tvGroupName.text = group!!.groupName
        binding.itemGroupName.tvContent.text = group!!.groupName
        binding.tvGroupMemberNum.text =
            getString(R.string.em_chat_group_detail_member_num, group!!.memberCount)
        binding.tvGroupRefund.text =
            resources.getString(if (isOwner) R.string.em_chat_group_detail_dissolve else R.string.em_chat_group_detail_refund)
        binding.tvGroupIntroduction.text = group!!.description
        //itemGroupNotDisturb.getSwitch().setChecked(group.isMsgBlocked());
        conversation = SdkHelper.instance
            .getConversation(groupId, EMConversation.EMConversationType.GroupChat, true)
        val extField = conversation?.extField
        binding.itemGroupTop.switch.isChecked =
            !TextUtils.isEmpty(extField) && EaseCommonUtils.isTimestamp(extField)


        binding.tvGroupInvite.visibility = if (group!!.memberCount <= 0)
            View.VISIBLE else View.GONE

        binding.tvGroupInvite.visibility =
            if (isCanInvite) View.VISIBLE else View.GONE
        //itemGroupNotDisturb.getSwitch().setChecked(group.isMsgBlocked());
        binding.itemGroupMemberManage.visibility =
            if (isOwner || isAdmin) View.VISIBLE else View.GONE
        binding.itemGroupIntroduction.tvContent.text = group!!.description
        makeTextSingleLine(binding.itemGroupNotice.tvContent)
        makeTextSingleLine(binding.itemGroupIntroduction.tvContent)
        val disabledIds = SdkHelper.instance.pushManager.noPushGroups
        binding.itemGroupNotDisturb.switch.isChecked =
            disabledIds != null && disabledIds.contains(groupId)
    }


    override fun initData() {
        super.initData()
        loadGroup()
    }

    private fun loadGroup() {
        viewModel.getGroup(groupId)
        viewModel.getGroupAnnouncement(groupId)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvGroupMemberTitle ->
                GroupMemberTypeActivity.actionStart(
                    context = this,
                    groupId = groupId,
                    owner = isOwner
                )
            R.id.tv_group_invite ->
                GroupPickContactsActivity.actionStartForResult(
                    context = this,
                    groupId = groupId,
                    owner = isOwner,
                    launch
                )
            R.id.item_group_name -> showGroupNameDialog()
            R.id.item_group_share_file -> GroupSharedFilesActivity.actionStart(this, groupId)
            R.id.item_group_notice -> showAnnouncementDialog()
            R.id.item_group_introduction -> showIntroductionDialog()
            R.id.item_group_history -> SearchGroupChatActivity.actionStart(this, groupId)
            R.id.item_group_clear_history -> showClearConfirmDialog()
            R.id.tv_group_refund -> showConfirmDialog()
            R.id.item_group_member_manage -> GroupManageIndexActivity.actionStart(this, groupId)
        }
    }

    private fun showClearConfirmDialog() {
        SimpleDialogFragment.Builder(this)
            .setTitle(R.string.em_chat_group_detail_clear_history_warning)
            .setOnConfirmClickListener {
                viewModel.clearHistory(groupId)
            }
            .showCancelButton(true)
            .show()
    }

    private fun showConfirmDialog() {
        SimpleDialogFragment.Builder(this)
            .setTitle(if (isOwner) R.string.em_chat_group_detail_dissolve else R.string.em_chat_group_detail_refund)
            .setOnConfirmClickListener {
                if (isOwner) {
                    viewModel.destroyGroup(groupId)
                } else {
                    viewModel.leaveGroup(groupId)
                }
            }
            .showCancelButton(true)
            .show()
    }

    override fun onCheckedChanged(buttonView: SwitchItemView, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.item_group_not_disturb -> viewModel.updatePushServiceForGroup(groupId, isChecked)
            R.id.item_group_off_push -> viewModel.updatePushServiceForGroup(groupId, isChecked)
            R.id.item_group_top -> {
                if (isChecked) {
                    conversation?.extField = System.currentTimeMillis().toString() + ""
                } else {
                    conversation?.extField = ""
                }
                LiveDataBus.get().use(DemoConstant.GROUP_CHANGE)
                    .postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP))
            }
        }
    }

    private fun showGroupNameDialog() {
        EditTextDialogFragment.Builder(this)
            .setContent(group!!.groupName)
            .setConfirmClickListener { _, content ->
                if (!TextUtils.isEmpty(content)) {
                    viewModel.setGroupName(groupId, content ?: "")
                }
            }
            .setTitle(R.string.em_chat_group_detail_name)
            .show()
    }

    private fun showAnnouncementDialog() {
        showDialog(
            this,
            getString(R.string.em_chat_group_detail_announcement),
            group!!.announcement,
            getString(R.string.em_chat_group_detail_announcement_hint),
            GroupHelper.isAdmin(group) || GroupHelper.isOwner(group),
        ) { _, content ->
            //修改群公告
            viewModel.setGroupAnnouncement(groupId, content ?: "")
        }
    }

    private fun showIntroductionDialog() {
        showDialog(
            this,
            getString(R.string.em_chat_group_detail_introduction),
            group!!.description,
            getString(R.string.em_chat_group_detail_introduction_hint),
            GroupHelper.isAdmin(group) || GroupHelper.isOwner(group),
        ) { _, content ->
            //修改群介绍
            viewModel.setGroupDescription(groupId, content ?: "")
        }
    }


    private fun makeTextSingleLine(tv: TextView) {
        tv.maxLines = 1
        tv.ellipsize = TextUtils.TruncateAt.END
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    /**
     * 是否有邀请权限
     * @return
     */
    private val isCanInvite: Boolean
        get() = GroupHelper.isCanInvite(group)

    /**
     * 是否是管理员
     * @return
     */
    private val isAdmin: Boolean
        get() = GroupHelper.isAdmin(group)

    /**
     * 是否是群主
     * @return
     */
    private val isOwner: Boolean
        get() = GroupHelper.isOwner(group)

    companion object {
        private const val REQUEST_CODE_ADD_USER = 0

        fun actionStart(context: Context, groupId: String?) {
            val intent = Intent(context, GroupDetailActivity::class.java)
            intent.putExtra("groupId", groupId)
            context.startActivity(intent)
        }
    }
}