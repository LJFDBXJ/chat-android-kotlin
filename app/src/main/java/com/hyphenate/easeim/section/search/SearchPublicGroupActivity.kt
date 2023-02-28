package com.hyphenate.easeim.section.search

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.chat.EMGroup
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.ktx.jump
import com.hyphenate.easeim.databinding.ItemWidgetContactBinding
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter
import com.hyphenate.easeim.section.ui.chat.ChatActivity
import com.hyphenate.easeim.section.ui.group.GroupHelper
import com.hyphenate.easeim.section.ui.group.activity.GroupSimpleDetailActivity
import com.hyphenate.easeim.section.ui.contact.vm.GroupContactVm
import com.hyphenate.easeim.section.ui.contact.vm.PublicGroupVm

class SearchPublicGroupActivity : SearchActivity() {
    private val viewModel by viewModels<PublicGroupVm>()
    private var allJoinedGroups: List<EMGroup>? = null
    private val adapter by lazy { SearchPublicGroupContactAdapter() }
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.titleBar.setTitle(getString(R.string.em_search_group_public))
        binding.rvList.adapter
        adapter.setOnItemClickListener { _, _, position ->
            val group = adapter.getItem(position)
            val isJoinGroup = GroupHelper.isJoinedGroup(
                allJoinGroups = allJoinedGroups,
                groupId = group.groupId
            )
            if (isJoinGroup) {
                ChatActivity.actionStart(
                    context = this,
                    conversationId = group.groupId,
                    chatType = DemoConstant.CHATTYPE_GROUP
                )
            } else {
                GroupSimpleDetailActivity.actionStart(context = this, groupId = group.groupId)
            }
        }
        binding.query.hint = getString(R.string.em_search_group_public_hint)
    }

    override fun initData() {
        super.initData()
        viewModel.group.observe(this) {
            it ?: return@observe
            adapter.addData(it)
        }
        val groupViewModel by viewModels<GroupContactVm>()
        groupViewModel.allGroup.observe(this) { response ->
            allJoinedGroups = response
        }
        groupViewModel.loadAllGroups()
    }

    override fun searchMessages(search: String) {
        if (search.isNotEmpty()) {
            viewModel.getGroup(groupId = search)
        }
    }

    private inner class SearchPublicGroupContactAdapter :
        BaseBindAdapter<EMGroup, ItemWidgetContactBinding>(layoutResId = R.layout.item_widget_contact) {
        override fun convert(
            holder: BaseDataBindingHolder<ItemWidgetContactBinding>,
            item: EMGroup
        ) {
            super.convert(holder, item)
            holder.dataBinding?.let { bind ->
                bind.avatar.setImageResource(R.drawable.ease_group_icon)
                bind.name.text = item.groupName
                bind.signature.text = item.groupId
                bind.signature.visibility = View.VISIBLE
            }
        }

    }

    companion object {
        @JvmStatic
        fun actionStart(context: Context) {
            context.jump<SearchPublicGroupActivity>()
        }
    }
}