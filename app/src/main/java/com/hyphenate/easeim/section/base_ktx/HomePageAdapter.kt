package com.hyphenate.easeim.section.base_ktx

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomePageAdapter(manager: FragmentManager,lifecycle: Lifecycle) : FragmentStateAdapter(manager,lifecycle) {
    val data = ArrayList<Fragment>()

    fun addAll(fragments: List<Fragment>) {
        data.addAll(fragments)
        notifyItemRangeChanged(0, data.size)
    }

    fun reAddAll(fragments: List<Fragment>) {
        removeAll()
        data.addAll(fragments)
        notifyItemRangeChanged(0, fragments.size)
    }

    fun removeAll() {
        val previewSize = data.size
        data.clear()
        notifyItemRangeRemoved(0, previewSize)
    }

    fun addElement(fragment: Fragment) {
        data.add(fragment)
        notifyItemInserted(data.size)
    }

    fun addElement(index: Int, fragment: Fragment) {
        data.add(index,fragment)
        notifyItemInserted(index)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun createFragment(position: Int): Fragment {
        return data[position]
    }

}