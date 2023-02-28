package com.hyphenate.easeim.section.ui.contact.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.ktx.jump
import com.hyphenate.easeim.databinding.ActivityFriendsGroupContactManageBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.group.activity.GroupPrePickActivity
import com.hyphenate.easeim.section.search.SearchGroupActivity
import com.hyphenate.easeim.section.search.SearchPublicGroupActivity
import com.hyphenate.easeim.section.ui.contact.fragment.GroupContactManageFragment
import com.hyphenate.easeim.section.ui.contact.fragment.GroupPublicContactManageFragment
import com.hyphenate.easeim.section.ui.contact.vm.GroupContactVm

/**
 * @author LXJDBXJ
 * @date 2022/10/10/ 22:20
 * @desc 首页 消息 ->群列表
 */
class GroupContactManageActivity(override val layoutId: Int = R.layout.activity_friends_group_contact_manage) :
    BaseInitActivityKtx<ActivityFriendsGroupContactManageBinding>(), View.OnClickListener {

    /**
     * 是否是公开群
     * @return
     */
    private var isShowPublic = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        isShowPublic = intent.getBooleanExtra("showPublic", false)

    }

    override fun initListener() {
        super.initListener()
        binding.titleBarGroupContact.setOnBackPressListener {
            onBackPressed()
        }
        binding.titleBarGroupContact.setOnRightClickListener {
            GroupPrePickActivity.actionStart(this)
        }
        binding.searchGroup.setOnClickListener(this)
    }

    override fun initData() {
        super.initData()
        val viewModel by viewModels<GroupContactVm>()
        switchTab()
        if (isShowPublic) {
            viewModel.loadAllGroups()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.search_group -> if (isShowPublic) {
                //搜索公开群
                SearchPublicGroupActivity.actionStart(context = this)
            } else {
                //搜索已加入的群
                SearchGroupActivity.actionStart(context = this)
            }
        }
    }

    private fun switchTab() {
        if (isShowPublic) {
            //公开群
            showGroup(tag = "Public")
            binding.titleBarGroupContact.setTitle(getString(R.string.em_friends_group_public))

        } else {
            //已加入的群
            showGroup(tag = "group")
            binding.titleBarGroupContact.setTitle(getString(R.string.em_friends_group_join))
        }
    }

    private fun showGroup(tag: String) {
        var joinGroupFragment = supportFragmentManager.findFragmentByTag(tag)
        val transaction = supportFragmentManager.beginTransaction()
        if (joinGroupFragment != null && joinGroupFragment.isAdded) {
            transaction.show(joinGroupFragment)
        } else {
            joinGroupFragment = if (tag == "Public")
                GroupPublicContactManageFragment()
            else
                GroupContactManageFragment()
            transaction.replace(R.id.fl_fragment, joinGroupFragment, tag).commit()
        }
    }

    companion object {
        fun actionStart(context: Context) {
            context.jump<GroupContactManageActivity>()
        }

        fun actionStart(context: Context, showPublic: Boolean) {
            val intent = Intent(context, GroupContactManageActivity::class.java)
            intent.putExtra("showPublic", showPublic)
            context.startActivity(intent)
        }
    }
}