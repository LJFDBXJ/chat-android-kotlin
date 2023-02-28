package com.hyphenate.easeim.section.ui.chat.adapter

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Filter
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.ActivitySendUserCardBinding
import com.hyphenate.easeim.databinding.ItemUserCardBinding
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter
import com.hyphenate.easeim.section.conference.ContactState
import com.hyphenate.easeim.section.ui.chat.activity.FilterCall
import com.hyphenate.easeui.utils.EaseUserUtils

/**
 * Created by LXJDBXJ
 * @Date 2022/9/20 01:07
 * @Description
 */
class SelectUserContactsAdapter() :
    BaseBindAdapter<ContactState, ItemUserCardBinding>(layoutResId = R.layout.item_user_card) {
    private var mContactFilter: ContactFilter? = null


    override fun convert(
        holder: BaseDataBindingHolder<ItemUserCardBinding>,
        item: ContactState
    ) {
        super.convert(holder, item)
        holder.dataBinding?.run {
            EaseUserUtils.setUserAvatar(context, item.userName, headIcon)
            EaseUserUtils.setUserNick(item.userName, name)
        }

    }

    fun setData(data: ArrayList<ContactState>) {
        setList(data)
    }

    fun filter(constraint: CharSequence?) {
        if (mContactFilter == null) {
            mContactFilter = ContactFilter(data)
        }
        mContactFilter?.filter(constraint) {
            setList(it)
        }
    }

    /**
     * 发送名片提示框
     */
    private fun sendUserCardDisplay(userId: String) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        val bind = ActivitySendUserCardBinding.inflate(LayoutInflater.from(context))
        val user = SdkHelper.instance.getUserInfo(userId)
        if (user != null) {
            bind.userNickName.text = user.nickname
            Glide.with(context)
                .load(user.avatar)
                .placeholder(R.drawable.em_login_logo)
                .into(bind.headView)
        } else {
            bind.userNickName.text = user?.username
        }
        bind.userIdView.text = context.getString(R.string.personal_card) + userId
        dialog.setView(bind.root)
        bind.btnSend.setOnClickListener {
            val intent = Intent()
            intent.putExtra("user", user)
            val activity = context as AppCompatActivity
            activity.setResult(AppCompatActivity.RESULT_OK, intent)
            dialog.dismiss()
            activity.finish()
        }
        bind.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()

    }

    init {
        setOnItemClickListener { _, _, position ->
            val item = getItem(position)
            sendUserCardDisplay(item.userName)
        }
    }
}


private class ContactFilter(private val contacts: MutableList<ContactState>) : Filter() {
    private var mFilterCallback: FilterCall? = null

    fun filter(constraint: CharSequence?, callback: FilterCall?) {
        mFilterCallback = callback
        super.filter(constraint)
    }

    public override fun performFiltering(prefix: CharSequence): FilterResults {
        val results = FilterResults()
        if (prefix.isEmpty()) {
            results.values = contacts
            results.count = contacts.size
        } else {
            val prefixString = prefix.toString()
            val count = contacts.size
            val newValues = ArrayList<ContactState>()
            for (i in 0 until count) {
                val user = contacts[i]
                val username = user.userName
                if (username.startsWith(prefixString)) {
                    newValues.add(user)
                } else {
                    val splits = username.split(" ")
                    if (splits.isEmpty()) {
                        continue
                    }
                    val words: MutableList<String> = ArrayList()
                    for (j in splits.indices.reversed()) {
                        if (splits[j].isNotEmpty()) {
                            words.add(splits[j])
                        } else {
                            break
                        }
                    }
                    for (word in words) {
                        if (word.startsWith(prefixString)) {
                            newValues.add(user)
                            break
                        }
                    }
                }
            }
            results.values = newValues
            results.count = newValues.size
        }
        return results
    }

    public override fun publishResults(constraint: CharSequence, results: FilterResults) {
        val result = if (results.values != null)
            results.values as List<ContactState>
        else
            ArrayList()
        mFilterCallback?.invoke(result)
    }
}