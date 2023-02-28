package com.hyphenate.easeim.section.ui.contact.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.databinding.FragmentFriendsGroupManageBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitFragmentKtx
import com.hyphenate.easeim.section.ui.chat.ChatActivity
import com.hyphenate.easeim.section.ui.contact.adapter.GroupContactAdapter
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.hyphenate.easeui.widget.EaseSidebar.OnTouchEventListener

class ContactManageFragment(override val layoutId: Int = R.layout.fragment_friends_group_manage) :
    BaseInitFragmentKtx<FragmentFriendsGroupManageBinding>(),
    OnTouchEventListener {
    private val mAdapter by lazy { GroupContactAdapter() }
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.rvGroupList.adapter = mAdapter
    }

    override fun initListener() {
        super.initListener()
        binding.sideBarGroup.setOnTouchEventListener(this)
        mAdapter.setOnItemClickListener { _, _, position ->
            //跳转到群聊页面
            val group = mAdapter.getItem(position)
            ChatActivity.actionStart(
                context = mContext,
                conversationId = group.groupId,
                chatType = DemoConstant.CHATTYPE_GROUP
            )
        }
    }

    override fun onActionDown(event: MotionEvent, pointer: String) {
        showFloatingHeader(pointer = pointer)
        moveToRecyclerItem(pointer = pointer)
    }

    override fun onActionMove(event: MotionEvent, pointer: String) {
        showFloatingHeader(pointer = pointer)
        moveToRecyclerItem(pointer = pointer)
    }

    override fun onActionUp(event: MotionEvent) {
        hideFloatingHeader()
    }

    private fun moveToRecyclerItem(pointer: String) {
        val data = mAdapter.data
        if (data.isEmpty()) {
            return
        }
        data.forEachIndexed { index, group ->
            if (EaseCommonUtils.getLetter(group.groupName) == pointer) {
                val manager = binding.rvGroupList.layoutManager as LinearLayoutManager
                manager.scrollToPositionWithOffset(index, 0)
            }
        }
    }

    /**
     * 展示滑动的字符
     * @param pointer
     */
    private fun showFloatingHeader(pointer: String) {
        if (pointer.isEmpty()) {
            hideFloatingHeader()
            return
        }
        binding.floatingHeader.text = pointer
        binding.floatingHeader.visibility = View.VISIBLE
    }

    private fun hideFloatingHeader() {
        binding.floatingHeader.visibility = View.GONE
    }

}