package com.hyphenate.easeim.section.ui.contact.fragment

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.ktx.jump
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.databinding.FragmentCommonListBinding
import com.hyphenate.easeim.databinding.ItemWidgetContactBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitFragmentKtx
import com.hyphenate.easeim.section.ui.chat.ChatActivity
import com.hyphenate.easeim.section.ui.group.activity.NewChatRoomActivity
import com.hyphenate.easeim.section.ui.contact.adapter.ChatRoomContactAdapter
import com.hyphenate.easeim.section.ui.contact.vm.ChatRoomContactVm
import com.hyphenate.easeui.interfaces.EaseChatRoomListener
import com.hyphenate.easeui.model.EaseEvent
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener

/**
 * @author LXJDBXJ
 * @date 2022/10/10/ 22:20
 * @desc 首页 消息 ->聊天室
 */
class ChatRoomContactManageFragment(override val layoutId: Int = R.layout.fragment_common_list) :
    BaseInitFragmentKtx<FragmentCommonListBinding>(),
    OnRefreshLoadMoreListener {
    private var pageNum = 1
    private val mAdapter by lazy { ChatRoomContactAdapter() }
    private val mViewModel by viewModels<ChatRoomContactVm>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.rvCommonList.adapter = mAdapter
        addHeaderView()
    }

    override fun initViewModel() {
        super.initViewModel()

        mViewModel.load.observe(this) {
            mAdapter.setList(it)
            SpDbModel.instance.chatRooms = mAdapter.data
            binding.srlCommonRefresh.finishRefresh()
        }
        mViewModel.loadMore.observe(this) {
            mAdapter.setList(it)
            SpDbModel.instance.chatRooms = mAdapter.data
            binding.srlCommonRefresh.finishLoadMore()

        }
        DemoConstant.CHAT_ROOM_CHANGE.obs<EaseEvent>(this) { event ->
            event ?: return@obs
            if (event.isChatRoomLeave || event.type == EaseEvent.TYPE.CHAT_ROOM) {
                pageNum = 1
                mViewModel.loadChatRooms(pageNum = pageNum, pageSize = PAGE_SIZE)
            }
        }
    }

    override fun initData() {
        super.initData()
        mViewModel.loadChatRooms(pageNum = pageNum, pageSize = PAGE_SIZE)
    }

    override fun initListener() {
        super.initListener()
        binding.srlCommonRefresh.setOnRefreshLoadMoreListener(this)
        mAdapter.setOnItemClickListener { _, _, position ->
            val item = mAdapter.getItem(position)
            ChatActivity.actionStart(
                context = mContext,
                conversationId = item.id,
                chatType = DemoConstant.CHATTYPE_CHATROOM
            )
        }
        SdkHelper.instance.chatroomManager.addChatRoomChangeListener(ChatRoomChangeListener())
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        pageNum = 1
        mViewModel.loadChatRooms(pageNum = pageNum, pageSize = PAGE_SIZE)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        pageNum += 1
        mViewModel.setLoadMoreChatRooms(pageNum = pageNum, pageSize = PAGE_SIZE)
    }


    private fun addHeaderView() {
        val bind = ItemWidgetContactBinding.inflate(layoutInflater)
        bind.avatar.setImageResource(R.drawable.em_create_group)
        bind.name.setText(R.string.em_friends_chat_room_create)
        bind.root.setOnClickListener {
            context.jump<NewChatRoomActivity>()
        }
        mAdapter.addHeaderView(bind.root)
    }

    private inner class ChatRoomChangeListener : EaseChatRoomListener() {
        override fun onChatRoomDestroyed(roomId: String, roomName: String) {
            pageNum = 1
            mViewModel.loadChatRooms(pageNum = pageNum, pageSize = PAGE_SIZE)
        }

        override fun onRemovedFromChatRoom(
            reason: Int,
            roomId: String,
            roomName: String,
            participant: String
        ) {
        }

        override fun onMemberJoined(roomId: String, participant: String) {}
        override fun onMemberExited(roomId: String, roomName: String, participant: String) {}
    }

    companion object {
        private const val PAGE_SIZE = 50
    }
}