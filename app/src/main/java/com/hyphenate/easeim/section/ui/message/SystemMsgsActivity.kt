package com.hyphenate.easeim.section.ui.message

import android.content.Context
import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.db.entity.InviteMessageStatus
import com.hyphenate.easeim.common.ktx.jump
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.databinding.ActivitySystemMsgsBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.message.delegates.AgreeMsgDelegate
import com.hyphenate.easeim.section.ui.message.delegates.InviteMsgDelegate
import com.hyphenate.easeim.section.ui.message.delegates.InviteMsgDelegate.OnInviteListener
import com.hyphenate.easeim.section.ui.message.delegates.OtherMsgDelegate
import com.hyphenate.easeim.section.ui.message.vm.NewFriendsVm
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.widget.EaseRecyclerView.RecyclerViewContextMenuInfo
import com.hyphenate.exceptions.HyphenateException
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener

/**
 * @author LXJDBXJ
 * 系统消息通知
 */
class SystemMsgsActivity(override val layoutId: Int = R.layout.activity_system_msgs) :
    BaseInitActivityKtx<ActivitySystemMsgsBinding>(),
    OnRefreshLoadMoreListener, OnInviteListener {
    private var offset = 0
    private val adapter by lazy { NewFriendsMsgAdapter() }
    private val viewModel by viewModels<NewFriendsVm>()


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val msgDelegate = InviteMsgDelegate()
        msgDelegate.setOnInviteListener(this)

        adapter.addDelegate(AgreeMsgDelegate())
            .addDelegate(msgDelegate)
            .addDelegate(OtherMsgDelegate())

        binding.rvList.adapter = adapter
        registerForContextMenu(binding.rvList)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.demo_invite_list_menu, menu)
        val position = (menuInfo as RecyclerViewContextMenuInfo).position
        val item = adapter.getItem(position)
        var statusParams: String? = null
        try {
            statusParams = item.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS)
        } catch (e: HyphenateException) {
            e.printStackTrace()
        }
        if (statusParams == null) {
            return
        }
        val statusEnum = InviteMessageStatus.valueOf(statusParams)
        if (statusEnum == InviteMessageStatus.BEINVITEED ||
            statusEnum == InviteMessageStatus.BEAPPLYED ||
            statusEnum == InviteMessageStatus.GROUPINVITATION
        ) {
            menu.findItem(R.id.action_invite_agree).isVisible = true
            menu.findItem(R.id.action_invite_refuse).isVisible = true
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val position = (item.menuInfo as RecyclerViewContextMenuInfo).position
        val message = adapter.getItem(position)
        when (item.itemId) {
            R.id.action_invite_agree -> viewModel.agreeInvite(emMessage = message)
            R.id.action_invite_refuse -> viewModel.refuseInvite(emMessage = message)
            R.id.action_invite_delete -> viewModel.deleteMsg(emMessage = message)
        }
        return super.onContextItemSelected(item)
    }

    override fun initListener() {
        super.initListener()
        binding.srlRefresh.setOnRefreshLoadMoreListener(this)
        binding.titleBar.setOnBackPressListener {
            onBackPressed()
        }
    }

    override fun initData() {
        super.initData()
        viewModel.inviteMsg.observe(this) {
            finishRefresh()
            if (it == null) {
                return@observe
            }
            adapter.data = it
        }
        viewModel.moreInviteMsg.observe(this) { response ->
            finishLoadMore()
            if (response == null) {
                return@observe
            }
            adapter.addData(response)
        }
        viewModel.deleteResult.observe(this) {
            if (it) {
                viewModel.loadMessages(limit = limit)
                val event = EaseEvent.create(DemoConstant.CONTACT_CHANGE, EaseEvent.TYPE.CONTACT)
                LiveDataBus.get().use(DemoConstant.CONTACT_CHANGE).postValue(event)
            }
        }
        viewModel.agreeResult.observe(this) {
            viewModel.loadMessages(limit = limit)
            val event = EaseEvent.create(DemoConstant.CONTACT_CHANGE, EaseEvent.TYPE.CONTACT)
            LiveDataBus.get()
                .use(DemoConstant.CONTACT_CHANGE)
                .postValue(event)
        }
        viewModel.refuseObservable.observe(this) {
            viewModel.loadMessages(limit = limit)
            val event =
                EaseEvent.create(DemoConstant.CONTACT_CHANGE, EaseEvent.TYPE.CONTACT)
            LiveDataBus.get().use(DemoConstant.CONTACT_CHANGE).postValue(event)
        }
        arrayOf(
            DemoConstant.NOTIFY_CHANGE,
            DemoConstant.MESSAGE_CHANGE_CHANGE,
            DemoConstant.GROUP_CHANGE,
            DemoConstant.CHAT_ROOM_CHANGE,
            DemoConstant.CONTACT_CHANGE
        ).obs<EaseEvent>(this) { key, event ->
            event ?: return@obs
            loadData(event)
        }

        viewModel.makeAllMsgRead()
        viewModel.loadMessages(limit = limit)
    }

    private fun loadData(easeEvent: EaseEvent) {
        viewModel.loadMessages(limit = limit)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        offset += limit
        val message = adapter.data[adapter.data.size - 1]
        viewModel.loadMoreMessages(message.msgId, limit = limit)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        viewModel.loadMessages(limit = limit)
    }

    private fun finishRefresh() {
        binding.srlRefresh.finishRefresh()
    }

    private fun finishLoadMore() {
        binding.srlRefresh.finishLoadMore()
    }

    // 同意好友请求
    override fun onInviteAgree(view: View, msg: EMMessage) {
        viewModel.agreeInvite(emMessage = msg)
    }

    // 拒绝好友请求
    override fun onInviteRefuse(view: View, msg: EMMessage) {
        viewModel.refuseInvite(emMessage = msg)
    }

    companion object {
        private const val limit = 10
        fun actionStart(context: Context) {
            context.jump<SystemMsgsActivity>()
        }
    }
}