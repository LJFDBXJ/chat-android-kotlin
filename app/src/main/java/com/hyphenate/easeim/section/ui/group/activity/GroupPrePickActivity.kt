package com.hyphenate.easeim.section.ui.group.activity

import android.content.Context
import android.view.View
import com.hyphenate.easeim.common.ktx.jump

class GroupPrePickActivity : GroupPickContactsActivity() {
    override fun onRightClick(view: View) {
        val selectedMembers = adapter.selectedMembers
        var newMembers: Array<String>? = null
        if (selectedMembers.isNotEmpty()) {
            newMembers = selectedMembers.toTypedArray()
        }
        NewGroupActivity.actionStart(this, newMembers)
        finish()
    }

    companion object {
        fun actionStart(context: Context) {
            context.jump<GroupPrePickActivity>()
        }
    }
}