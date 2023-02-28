package com.hyphenate.easeim.section.ui.contact.adapter

import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.ItemSearchListBinding
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter
import com.hyphenate.easeim.section.ui.contact.activity.ContactDetailActivity
import com.hyphenate.easeim.section.ui.contact.vm.AddContactVm
import com.hyphenate.easeui.domain.EaseUser

class AddContactAdapter(val model: AddContactVm) :
    BaseBindAdapter<String, ItemSearchListBinding>(layoutResId = R.layout.item_search_list) {
    private var mContacts: List<String>? = null

    init {
        setOnItemClickListener { _, _, position ->
            // 跳转到好友页面
            val user = EaseUser(getItem(position))
            ContactDetailActivity.actionStart(context, user, false)
        }
    }

    override fun convert(holder: BaseDataBindingHolder<ItemSearchListBinding>, item: String) {
        super.convert(holder, item)
        holder.dataBinding?.let { bind ->
            bind.btnSearchAdd.setOnClickListener {
                bind.btnSearchAdd.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.demo_button_unenable_shape
                )
                bind.btnSearchAdd.setText(R.string.em_add_contact_item_button_text_added)
                bind.btnSearchAdd.isEnabled = false
                // 添加好友
                model.addContact(item, context.getString(R.string.em_add_contact_add_a_friend))
            }
            if (item.isEmpty()) {
                bind.tvSearchName.text = ""
                return
            }
            bind.tvSearchName.text = item
            bind.btnSearchAdd.background = if (mContacts != null && mContacts!!.contains(item)) {
                bind.btnSearchAdd.setText(R.string.em_add_contact_item_button_text_added)
                bind.btnSearchAdd.isEnabled = false
                ContextCompat.getDrawable(context, R.drawable.demo_button_unenable_shape)
            } else {
                bind.btnSearchAdd.setText(R.string.em_add_contact_item_button_text)
                bind.btnSearchAdd.isEnabled = true
                ContextCompat.getDrawable(context, R.drawable.demo_add_contact_button_bg)
            }

        }
    }

    fun addLocalContacts(contacts: List<String>?) {
        mContacts = contacts
    }
}