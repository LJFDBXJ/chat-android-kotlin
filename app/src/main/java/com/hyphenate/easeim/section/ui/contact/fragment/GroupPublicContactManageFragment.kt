package com.hyphenate.easeim.section.ui.contact.fragment

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.hyphenate.chat.EMGroup
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.databinding.FragmentGroupPublicContactManageBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitFragmentKtx
import com.hyphenate.easeim.section.ui.chat.ChatActivity
import com.hyphenate.easeim.section.ui.group.GroupHelper
import com.hyphenate.easeim.section.ui.group.activity.GroupSimpleDetailActivity
import com.hyphenate.easeim.section.ui.contact.adapter.PublicGroupContactAdapter
import com.hyphenate.easeim.section.ui.contact.vm.GroupContactVm

open class GroupPublicContactManageFragment(override val layoutId: Int = R.layout.fragment_group_public_contact_manage) :
    BaseInitFragmentKtx<FragmentGroupPublicContactManageBinding>() {
    private val mAdapter by lazy { PublicGroupContactAdapter() }
    private val pageSize = 20
    private var cursor: String? = null
    private val viewModel by viewModels<GroupContactVm>()
    private var allJoinGroups: List<EMGroup>? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.rvList.adapter = mAdapter

    }

    override fun initListener() {
        super.initListener()
        binding.srlRefresh.setOnLoadMoreListener {
            if (cursor != null) {
                viewModel.getMorePublicGroups(pageSize = pageSize, cursor = cursor)
            }
        }
        binding.srlRefresh.setOnRefreshListener {
            viewModel.getPublicGroups(pageSize = pageSize)
        }
        mAdapter.setOnItemClickListener { _, _, position ->
            val item = mAdapter.getItem(position)
            if (GroupHelper.isJoinedGroup(allJoinGroups = allJoinGroups, groupId = item.groupId)) {
                ChatActivity.actionStart(
                    context = mContext,
                    conversationId = item.groupId,
                    chatType = DemoConstant.CHATTYPE_GROUP
                )
            } else {
                GroupSimpleDetailActivity.actionStart(
                    context = mContext,
                    groupId = item.groupId
                )
            }
        }
    }


    override fun initViewModel() {
        viewModel.publicGroup.observe(this) {
            it ?: return@observe
            cursor = it.cursor
            mAdapter.setList(it.data)
            binding.srlRefresh.finishRefresh()

        }
        viewModel.morePublicGroup.observe(this) {
            it ?: return@observe
            cursor = it.cursor
            val groups = it.data
            mAdapter.addData(groups)
            binding.srlRefresh.finishLoadMore()

        }
        viewModel.allGroup.observe(this) {
            allJoinGroups = it
            //获取完加入的群组信息，再请求数据
            viewModel.getPublicGroups(pageSize)
//            //请求出错后，再请求数据
//            viewModel.getPublicGroups(pageSize)
        }
    }


}