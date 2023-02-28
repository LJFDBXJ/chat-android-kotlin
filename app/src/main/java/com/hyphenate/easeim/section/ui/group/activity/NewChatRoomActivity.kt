package com.hyphenate.easeim.section.ui.group.activity

import android.text.TextUtils
import androidx.activity.viewModels
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.livedatas.LiveDataBus.Companion.get
import com.hyphenate.easeim.databinding.ActivityNewChatRoomBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.dialog_ktx.SimpleDialogFragment
import com.hyphenate.easeim.section.ui.contact.vm.NewChatRoomVm
import com.hyphenate.easeui.model.EaseEvent

class NewChatRoomActivity(override val layoutId: Int = R.layout.activity_new_chat_room) :
    BaseInitActivityKtx<ActivityNewChatRoomBinding>() {
    private val viewModel by viewModels<NewChatRoomVm>()

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener {
            onBackPressed()
        }
        binding.titleBar.setOnRightClickListener {
            createChatRoom()
        }
    }

    override fun initData() {
        super.initData()
        viewModel.chatRoom.observe(this) {
            if (it != null) {
                get().use(DemoConstant.CHAT_ROOM_CHANGE).postValue(
                    EaseEvent.create(
                        DemoConstant.CHAT_ROOM_CHANGE,
                        EaseEvent.TYPE.CHAT_ROOM
                    )
                )
                finish()
            } else
                toast("创建失败")

        }
    }

    private fun createChatRoom() {
        val name = binding.etGroupName.text.toString()
        if (TextUtils.isEmpty(name)) {
            SimpleDialogFragment.Builder(this)
                .setTitle(R.string.em_chat_room_new_name_cannot_be_empty)
                .show()
            return
        }
        val desc = binding.etGroupIntroduction.text.toString()
        val welcome = binding.welcomeDesc.text.toString()
        viewModel.createChatRoom(
            subject = name,
            description = desc,
            welcomeMessage = welcome,
            500, null
        )
    }
}