package com.hyphenate.easeim.section.ui.group.adapter

import android.text.TextUtils
import com.hyphenate.easeui.domain.EaseUser
import android.view.View
import com.hyphenate.easeim.R
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.databinding.ItemPickContactWithCheckboxLayoutBinding
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter
import java.util.ArrayList

//import com.bumptech.glide.Glide;
class GroupPickContactsAdapter() :
    BaseBindAdapter<EaseUser, ItemPickContactWithCheckboxLayoutBinding>(layoutResId = R.layout.item_pick_contact_with_checkbox_layout) {
    private var existMembers: List<String>? = null
    var selectedMembers = ArrayList<String>()
    private var listener: OnSelectListener? = null
    var isCreateGroup: Boolean = false

    fun setExistMember(existMembers: List<String>) {
        this.existMembers = existMembers
        if (isCreateGroup) {
            selectedMembers.clear()
            selectedMembers.addAll(existMembers)
        }
        notifyDataSetChanged()
    }


    override fun convert(
        holder: BaseDataBindingHolder<ItemPickContactWithCheckboxLayoutBinding>,
        item: EaseUser
    ) {
        super.convert(holder, item)

        holder.dataBinding?.let { bind ->
            bind.avatar.setShapeType(SdkHelper.instance.easeAvatarOptions.avatarShape)

            val username = getRealUsername(item.username)
            bind.name.text = item.nickname
            //Glide.with(mContext).load(R.drawable.ease_default_avatar).into(avatar);
            bind.avatar.setImageResource(R.drawable.ease_default_avatar)
            val header = item.initialLetter
            if (holder.absoluteAdapterPosition == 0 || header != null && header != getItem(holder.absoluteAdapterPosition - 1).initialLetter) {
                if (TextUtils.isEmpty(header)) {
                    bind.header.visibility = View.GONE
                } else {
                    bind.header.visibility = View.VISIBLE
                    bind.header.text = header
                }
            } else {
                bind.header.visibility = View.GONE
            }
            if (checkIfContains(username) || selectedMembers.isNotEmpty() &&
                selectedMembers.contains(username)
            ) {
                bind.checkbox.isChecked = true
                if (isCreateGroup) {
                    bind.checkbox.setBackgroundResource(R.drawable.demo_selector_bg_check)
                    bind.root.isEnabled = true
                } else {
                    bind.checkbox.setBackgroundResource(R.drawable.demo_selector_bg_gray_check)
                    bind.root.isEnabled = false
                }
            } else {
                bind.checkbox.setBackgroundResource(R.drawable.demo_selector_bg_check)
                bind.checkbox.isChecked = false
                bind.root.isEnabled = true
            }
            bind.root.setOnClickListener { v ->
                bind.checkbox.isChecked = !bind.checkbox.isChecked
                val checked = bind.checkbox.isChecked
                if (isCreateGroup || !checkIfContains(username)) {
                    if (checked) {
                        if (!selectedMembers.contains(username)) {
                            selectedMembers.add(username)
                        }
                    } else {
                        if (selectedMembers.contains(username)) {
                            selectedMembers.remove(username)
                        }
                    }
                }
                listener?.onSelected(v, selectedMembers)
            }

        }
    }


    /**
     * 检查是否已存在
     * @param username
     * @return
     */
    private fun checkIfContains(username: String): Boolean {
        return if (existMembers == null) {
            false
        } else existMembers?.contains(username) ?: false
    }

    /**
     * 因为环信id只能由字母和数字组成，如果含有“/”就可以认为是多端登录用户
     * @param username
     * @return
     */
    private fun getRealUsername(username: String): String {
        if (!username.contains("/")) {
            return username
        }
        val multipleUser = username.split("/").toTypedArray()
        return multipleUser[0]
    }

    fun setOnSelectListener(listener: OnSelectListener?) {
        this.listener = listener
    }

    interface OnSelectListener {
        fun onSelected(v: View?, selectedMembers: List<String>?)
    }
}