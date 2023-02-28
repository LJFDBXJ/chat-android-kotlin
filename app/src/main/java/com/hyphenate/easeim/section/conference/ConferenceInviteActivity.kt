package com.hyphenate.easeim.section.conference

import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.hyphenate.easecallkit.EaseCallKit
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.databinding.ActivityConferenceInviteBinding
import com.hyphenate.easeim.databinding.EaseSearchBarBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.chat.vm.ConferenceInviteVm
import com.hyphenate.easeui.model.EaseEvent

typealias IFilterCallback = Function1<List<ContactState>, Unit>

class ConferenceInviteActivity(override val layoutId: Int = R.layout.activity_conference_invite) :
    BaseInitActivityKtx<ActivityConferenceInviteBinding>(), View.OnClickListener {
    private var existMember: Array<String>? = null
    private var groupId: String? = null

    private val contactsAdapter by lazy {
        ContactsAdapter() {
            binding.btnStart.text =
                String.format(getString(R.string.button_start_video_conference), it.size)
        }
    }


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val group = intent.getStringExtra(DemoConstant.EXTRA_CONFERENCE_GROUP_ID)
        if (group != null) {
            groupId = group
            existMember =
                intent.getStringArrayExtra(DemoConstant.EXTRA_CONFERENCE_GROUP_EXIST_MEMBERS)
        }
        binding.btnStart.text = String.format(getString(R.string.button_start_video_conference), 0)
        binding.contactRecycler.adapter = contactsAdapter
        addHeader()
    }

    override fun initListener() {
        super.initListener()
        binding.btnStart.setOnClickListener(this)
        binding.titleBar.setOnBackPressListener {
            setResult(RESULT_CANCELED)
            onBackPressed()
            EaseCallKit.getInstance().startInviteMultipleCall(null, null)
            finish()
        }
    }

    override fun initData() {
        super.initData()
        val viewModel by viewModels<ConferenceInviteVm>()
        viewModel.conferenceInvite.observe(this) {
            contactsAdapter.setData(it)
        }
        arrayOf(
            DemoConstant.CONTACT_ADD,
            DemoConstant.CONTACT_CHANGE,
            DemoConstant.CONTACT_UPDATE,
        ).obs<EaseEvent>(this) { key, event ->
            event ?: return@obs
            when (key) {
                DemoConstant.CONTACT_ADD,
                DemoConstant.CONTACT_CHANGE,
                DemoConstant.CONTACT_UPDATE -> {
                    if (event.isContactChange) {
                        contactsAdapter.dataSetChanged()
                    }
                }
            }
        }
        viewModel.getConferenceMembers(groupId, existMember)
    }


    private fun addHeader() {
        val bind = EaseSearchBarBinding.inflate(LayoutInflater.from(this))
        bind.query.doOnTextChanged { text, _, _, _ ->
            contactsAdapter.filter(text)
            bind.searchClear.visibility = if (!text.isNullOrEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        bind.searchClear.setOnClickListener {
            bind.query.text?.clear()
            hideKeyboard()
        }
        contactsAdapter.addHeaderView(bind.root)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnStart -> {
                val members = contactsAdapter.selectMembers.toTypedArray()
                if (members.isEmpty()) {
                    toast(R.string.tips_select_contacts_first)
                    return
                }
                //用户自定义扩展字段
                val params = HashMap<String, Any?>()
                params["groupId"] = groupId
                //开始邀请人员
                EaseCallKit.getInstance().startInviteMultipleCall(members, params)
                finish()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            EaseCallKit.getInstance().startInviteMultipleCall(null, null)
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }
}

