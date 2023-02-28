package com.hyphenate.easeim.section.ui.conversation

import com.hyphenate.easeim.section.ui.conversation.vm.ConversationListVm
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.activityViewModels
import com.hyphenate.easeim.R
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo
import com.hyphenate.chat.EMConversation
import com.hyphenate.easeim.section.dialog_ktx.SimpleDialogFragment
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.chat.EMClient
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.custom.EaseConversationListFragment
import com.hyphenate.easeim.databinding.LayoutSearchBinding
import com.hyphenate.easeim.section.base_ktx.BaseActivityKtx
import com.hyphenate.easeim.section.search.SearchConversationActivity
import com.hyphenate.easeui.manager.EaseSystemMsgManager
import com.hyphenate.easeim.section.ui.message.SystemMsgsActivity
import com.hyphenate.easeim.section.ui.chat.ChatActivity
import com.hyphenate.easeui.utils.EaseCommonUtils

/**
 * @author LXJDBXJ
 * 会话列表
 */
class ConversationListFragment : EaseConversationListFragment(), View.OnClickListener {
    private val mViewModel by activityViewModels<ConversationListVm>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //添加搜索会话布局
        val searchBinding = LayoutSearchBinding.inflate(layoutInflater)
        llRoot.addView(searchBinding.root, 0)
        searchBinding.search.setOnClickListener(this)
    }

    override fun onMenuItemClick(item: MenuItem, position: Int): Boolean {
        val info = conversationListLayout.getItem(position)
        if (info.info is EMConversation) {
            when (item.itemId) {
                // 置顶
                R.id.action_con_make_top -> {
                    conversationListLayout.makeConversationTop(position, info)
                    return true
                }
                //取消置顶
                R.id.action_con_cancel_top -> {
                    conversationListLayout.cancelConversationTop(position, info)
                    return true
                }
                //删除
                R.id.action_con_delete -> {
                    showDeleteDialog(position, info)
                    return true
                }
            }
        }
        return super.onMenuItemClick(item, position)
    }

    private fun showDeleteDialog(position: Int, info: EaseConversationInfo) {
        SimpleDialogFragment.Builder(mContext as BaseActivityKtx)
            .setTitle(R.string.delete_conversation)
            .setOnConfirmClickListener(R.string.delete) {
                conversationListLayout.deleteConversation(position, info)
                LiveDataBus.get().use(DemoConstant.CONVERSATION_DELETE)
                    .postValue(EaseEvent(DemoConstant.CONVERSATION_DELETE, EaseEvent.TYPE.MESSAGE))
            }
            .showCancelButton(true)
            .show()
    }


    override fun initData() {
        //需要两个条件，判断是否触发从服务器拉取会话列表的时机，
        // 1 是否第一次安装，
        // 2 本地数据库没有会话列表数据
        if (SdkHelper.instance.isFirstInstall &&
            EMClient.getInstance().chatManager().allConversations.isEmpty()
        ) {
            mViewModel.fetchConversationsFromServer()
        } else {
            super.initData()
        }
    }

    override fun initListener() {
        super.initListener()
        mViewModel.deleteConversation.observe(this) {
            if (it.isSuccess()) {
                LiveDataBus.get().use(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(
                    EaseEvent(
                        DemoConstant.MESSAGE_CHANGE_CHANGE,
                        EaseEvent.TYPE.MESSAGE
                    )
                )
                //mViewModel.loadConversationList();
                conversationListLayout.loadDefaultData()
            }
        }
        mViewModel.readConversation.observe(this) {
            LiveDataBus.get().use(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(
                EaseEvent(
                    DemoConstant.MESSAGE_CHANGE_CHANGE,
                    EaseEvent.TYPE.MESSAGE
                )
            )
            conversationListLayout.loadDefaultData()
        }

        mViewModel.conversationList.observe(this) { response ->
            conversationListLayout.setData(response)
        }
        arrayOf(
            DemoConstant.NOTIFY_CHANGE,
            DemoConstant.MESSAGE_CHANGE_CHANGE,
            DemoConstant.GROUP_CHANGE,
            DemoConstant.CHAT_ROOM_CHANGE,
            DemoConstant.CONVERSATION_DELETE,
            DemoConstant.CONVERSATION_READ,
            DemoConstant.CONTACT_CHANGE,
            DemoConstant.CONTACT_ADD,
            DemoConstant.CONTACT_UPDATE
        ).obs<EaseEvent>(this) { _, event ->
            loadList(change = event)
        }
        arrayOf(
            DemoConstant.MESSAGE_CALL_SAVE,
            DemoConstant.MESSAGE_NOT_SEND
        ).obs<Boolean>(this) { _, event ->
            refreshList(event = event)
        }
    }


    private fun refreshList(event: Boolean?) {
        event ?: return
        if (event) {
            conversationListLayout.loadDefaultData()
        }
    }

    private fun loadList(change: EaseEvent?) {
        change ?: return
        if (change.isMessageChange || change.isNotifyChange
            || change.isGroupLeave || change.isChatRoomLeave
            || change.isContactChange
            || change.type == EaseEvent.TYPE.CHAT_ROOM || change.isGroupChange
        ) {
            conversationListLayout.loadDefaultData()
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.search ->
                SearchConversationActivity.actionStart(context = requireContext())
        }
    }

    // 会话列表点击 item
    override fun onItemClick(view: View, position: Int) {
        super.onItemClick(view, position)
        val item = conversationListLayout.getItem(position).info
        if (item !is EMConversation)
            return
        if (EaseSystemMsgManager.getInstance().isSystemConversation(item)) {
            // 系统会话
            SystemMsgsActivity.actionStart(context = requireContext())
        } else {
            // 普通会话
            ChatActivity.actionStart(
                context = mContext,
                conversationId = item.conversationId(),
                chatType = EaseCommonUtils.getChatType(item)
            )
        }

    }

    override fun notifyItemChange(position: Int) {
        super.notifyItemChange(position)
        LiveDataBus.get().use(DemoConstant.MESSAGE_CHANGE_CHANGE)
            .postValue(EaseEvent(DemoConstant.MESSAGE_CHANGE_CHANGE, EaseEvent.TYPE.MESSAGE))
    }

}