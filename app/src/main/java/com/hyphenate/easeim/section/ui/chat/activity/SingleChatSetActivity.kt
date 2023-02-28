package com.hyphenate.easeim.section.ui.chat.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.viewModels
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.widget.SwitchItemView
import com.hyphenate.easeim.databinding.ActivitySingleChatSetBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.chat.vm.ChatVm
import com.hyphenate.easeim.section.dialog_ktx.SimpleDialogFragment
import com.hyphenate.easeim.section.search.SearchSingleChatActivity
import com.hyphenate.easeim.section.ui.contact.activity.ContactDetailActivity.Companion.actionStart
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener

class SingleChatSetActivity(override val layoutId: Int = R.layout.activity_single_chat_set) :
    BaseInitActivityKtx<ActivitySingleChatSetBinding>(), OnBackPressListener,
    View.OnClickListener,
    SwitchItemView.OnCheckedChangeListener {
    private val viewModel by viewModels<ChatVm>()
    private var toChatUsername: String = ""
    private var conversation: EMConversation? = null


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        toChatUsername = intent.getStringExtra("toChatUsername") ?: ""
    }

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener(this)
        binding.itemUserInfo.setOnClickListener(this)
        binding.itemSearchHistory.setOnClickListener(this)
        binding.itemClearHistory.setOnClickListener(this)
        binding.itemSwitchTop.setOnCheckedChangeListener(this)
        binding.itemUserNotDisturb.setOnCheckedChangeListener(this)
    }

    override fun initData() {
        super.initData()
        conversation = EMClient.getInstance()
            .chatManager()
            .getConversation(
                toChatUsername,
                EaseCommonUtils.getConversationType(EaseConstant.CHATTYPE_SINGLE),
                true
            )
        binding.itemUserInfo.avatar.setShapeType(1)
        binding.itemUserInfo.tvTitle.text = toChatUsername
        binding.itemSwitchTop.switch.isChecked = !TextUtils.isEmpty(conversation?.extField)
        viewModel.deleteConversation.observe(this) { response ->
            parseResource(
                response = response,
                callback = object : OnResourceParseCallback<Boolean>() {
                    override fun onSuccess(data: Boolean?) {
                        LiveDataBus.get().use(DemoConstant.CONVERSATION_DELETE).postValue(
                            EaseEvent(
                                DemoConstant.CONTACT_DECLINE,
                                EaseEvent.TYPE.MESSAGE
                            )
                        )
                        finish()
                    }
                }
            )

        }
        viewModel.noPushUsers.observe(this) {
            binding.itemUserNotDisturb.switch.isChecked =
                it.contains(toChatUsername)

        }
        viewModel.setNoPushUsers.observe(this) {
            if (it) {
                //设置免打扰成功

            } else {
                //可根据需求做出提示
                //ToastUtils.showFailToast("设置用户免打扰失败");

            }

        }
        viewModel.noPushUsers()
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.item_user_info -> {
                val user = EaseUser()
                user.username = toChatUsername
                actionStart(this, user)
            }
            R.id.item_search_history -> SearchSingleChatActivity.actionStart(
                this,
                toChatUsername
            )
            R.id.item_clear_history -> clearHistory()
        }
    }

    private fun clearHistory() {
        // 是否删除会话
        SimpleDialogFragment.Builder(this)
            .setTitle(R.string.em_chat_delete_conversation)
            .setOnConfirmClickListener {
                viewModel.deleteConversationById(
                    conversation!!.conversationId()
                )
            }
            .showCancelButton(true)
            .show()
    }

    override fun onCheckedChanged(buttonView: SwitchItemView, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.item_switch_top -> {
                conversation?.extField =
                    if (isChecked)
                        System.currentTimeMillis().toString()
                    else
                        ""
                LiveDataBus.get().use(DemoConstant.MESSAGE_CHANGE_CHANGE)
                    .postValue(
                        EaseEvent(
                            DemoConstant.MESSAGE_CHANGE_CHANGE,
                            EaseEvent.TYPE.MESSAGE
                        )
                    )
            }
            R.id.item_user_not_disturb -> viewModel.setUserNotDisturb(
                userId = toChatUsername,
                noPush = isChecked
            )
        }
    }

    companion object {
        @JvmStatic
        fun actionStart(context: Context, toChatUsername: String?) {
            val intent = Intent(context, SingleChatSetActivity::class.java)
            intent.putExtra("toChatUsername", toChatUsername)
            context.startActivity(intent)
        }
    }


}