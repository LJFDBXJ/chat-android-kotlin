package com.hyphenate.easeim.section.conference

import android.widget.Filter
import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.easeim.R
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.databinding.ItemContactBinding
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter
import com.hyphenate.easeui.utils.EaseUserUtils

class ContactState(
    val userName: String,
    var checked: Boolean,
)

class ContactsAdapter(
    private val action: Function1<HashSet<String>, Unit>
) : BaseBindAdapter<ContactState, ItemContactBinding>(layoutResId = R.layout.item_contact) {
    private var mContactFilter: ContactFilter? = null
    var contacts = ArrayList<ContactState>()
    var selectMembers = HashSet<String>()
    override fun convert(
        holder: BaseDataBindingHolder<ItemContactBinding>,
        item: ContactState
    ) {
        super.convert(holder, item)
        holder.dataBinding?.run {
            val userName = item.userName
            SdkHelper.instance.getUserInfo(userName)
            EaseUserUtils.setUserAvatar(context, userName, headIcon)
            EaseUserUtils.setUserNick(userName, name)
            checkbox.isChecked = item.checked
        }

    }

    fun dataSetChanged() {
        setList(contacts)

    }

    fun setData(data: List<ContactState>?) {
        contacts.clear()
        if (!data.isNullOrEmpty()) {
            contacts.addAll(data)
            setList(data)
        }
    }

    fun filter(constraint: CharSequence?) {
        if (mContactFilter == null) {
            mContactFilter = ContactFilter(contacts)
        }
        mContactFilter?.filter(constraint) {
            setList(it)
        }
    }

    init {
        setOnItemClickListener { _, view, position ->
            val item = getItem(position)
            DataBindingUtil.getBinding<ItemContactBinding>(view)?.let { bind ->

                item.checked = !bind.checkbox.isChecked
                bind.checkbox.isChecked = item.checked
                if (selectMembers.contains(item.userName)) {
                    selectMembers.remove(item.userName)
                } else {
                    selectMembers.add(item.userName)
                }
            }
            action.invoke(selectMembers)
        }
    }
}

private class ContactFilter(private val contacts: List<ContactState>) : Filter() {
    private var mFilterCallback: IFilterCallback? = null

    fun filter(constraint: CharSequence?, callback: IFilterCallback?) {
        mFilterCallback = callback
        super.filter(constraint)
    }

    override fun performFiltering(prefix: CharSequence): FilterResults {
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
                    val splits = username.split(" ").toTypedArray()
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

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        val result = if (results.values != null)
            results.values as List<ContactState>
        else
            ArrayList()
        mFilterCallback?.invoke(result)
    }
}
