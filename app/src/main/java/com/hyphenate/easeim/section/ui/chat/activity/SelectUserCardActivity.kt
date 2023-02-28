package com.hyphenate.easeim.section.ui.chat.activity

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.databinding.ActivityConferenceInviteBinding
import com.hyphenate.easeim.databinding.EaseSearchBarBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.conference.ContactState
import com.hyphenate.easeim.section.ui.chat.adapter.SelectUserContactsAdapter
import com.hyphenate.easeim.section.ui.chat.vm.ConferenceInviteVm
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener

typealias  FilterCall = Function1<List<ContactState>, Unit>

class SelectUserCardActivity(override val layoutId: Int = R.layout.activity_conference_invite) :
    BaseInitActivityKtx<ActivityConferenceInviteBinding>(),
    OnBackPressListener {
    private val contactsAdapter by lazy { SelectUserContactsAdapter() }
    private val existMember: Array<String>? = null
    private val viewModel by viewModels<ConferenceInviteVm>()
    private var toUser: String? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        toUser = intent.getStringExtra("toUser")
        binding.contactRecycler.adapter = contactsAdapter
        binding.btnStart.visibility = View.GONE
        addSearchHeader()
    }

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener(this)
    }

    override fun initData() {
        super.initData()
        viewModel.conferenceInvite.observe(this) {
            if (!it.isNullOrEmpty()) {
                contactsAdapter.setList(it)
            }
        }
        arrayOf(
            DemoConstant.CONTACT_ADD,
            DemoConstant.CONTACT_UPDATE,
            DemoConstant.CONTACT_CHANGE,
        ).obs<EaseEvent>(this) { key, event ->
            event ?: return@obs
            when (key) {
                DemoConstant.CONTACT_ADD,
                DemoConstant.CONTACT_UPDATE,
                DemoConstant.CONTACT_CHANGE -> {
                    if (event.isContactChange) {
                        contactsAdapter.notifyDataSetChanged()
                    }
                }
            }

        }
        viewModel.getConferenceMembers(groupId, existMember)
    }

    override fun onBackPress(view: View) {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
        finish()
    }

    private fun addSearchHeader() {
        val bind = EaseSearchBarBinding.inflate(LayoutInflater.from(this))
        bind.query.doOnTextChanged { text, _, _, _ ->
            contactsAdapter.filter(text)
            bind.searchClear.visibility = if (!text.isNullOrEmpty()) {
                View.VISIBLE
            } else
                View.GONE
        }
        bind.searchClear.setOnClickListener {
            bind.query.text?.clear()
            hideKeyboard()
        }
        contactsAdapter.addHeaderView(bind.root)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }


    companion object {
        private val groupId: String? = null
    }
}