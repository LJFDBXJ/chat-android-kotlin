package com.hyphenate.easeim.section.ui.contact.fragment

import androidx.fragment.app.viewModels
import com.hyphenate.chat.EMGroup
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.section.ui.chat.ChatActivity
import com.hyphenate.easeim.section.ui.contact.adapter.GroupContactAdapter
import com.hyphenate.easeim.section.ui.contact.vm.GroupContactVm
import com.hyphenate.easeui.model.EaseEvent

/**
 * @author LXJDBXJ
 * @date 2022/10/9
 * @desc 群管理
 */
class GroupContactManageFragment : GroupPublicContactManageFragment() {
    private val mViewModel by viewModels<GroupContactVm>()
    private val adapter = GroupContactAdapter()
    private var pageIndex = 0
    override fun initViewModel() {
        super.initViewModel()
        mViewModel.group.observe(this) {
            adapter.setList(it)
            binding.srlRefresh.finishRefresh()

        }
        mViewModel.moreGroup.observe(this) {
            if (it != null) {
                adapter.addData(it)
            }
            binding.srlRefresh.finishLoadMore()

        }
        DemoConstant.GROUP_CHANGE.obs<EaseEvent>(this) { event ->
            event ?: return@obs
            if (event.isGroupChange || event.isGroupLeave) {
                getRefreshData()
            }
        }
    }

    override fun initData() {
        binding.rvList.adapter = adapter
        adapter.setOnItemClickListener { _, _, position ->
            //跳转到群聊页面
            val group: EMGroup = adapter.getItem(position)
            ChatActivity.actionStart(mContext, group.groupId, DemoConstant.CHATTYPE_GROUP)
        }
        getRefreshData()
    }

    override fun initListener() {
        super.initListener()
        binding.srlRefresh.setOnLoadMoreListener {
            pageIndex += PAGE_SIZE
            mViewModel.loadMoreGroupListFromServer(pageIndex, PAGE_SIZE)
        }
    }

    private fun getRefreshData() {
        pageIndex = 0
        mViewModel.loadGroupListFromServer(pageIndex, PAGE_SIZE)
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}