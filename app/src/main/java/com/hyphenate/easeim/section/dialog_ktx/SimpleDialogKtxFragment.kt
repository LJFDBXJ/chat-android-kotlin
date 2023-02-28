package com.hyphenate.easeim.section.dialog_ktx

import com.hyphenate.easeim.section.base_ktx.BaseActivityKtx

class SimpleDialogKtxFragment : DialogKtxFragment() {
    override fun initArgument() {
        if (arguments != null) {
            setTitle(arguments?.getString(MESSAGE_KEY))
        }
    }

    class Builder(context: BaseActivityKtx) : DialogKtxFragment.Builder(context) {

        override fun iniFragment(): Builder {
            currentFragment = SimpleDialogKtxFragment()
            return this
        }
    }

    companion object {
        const val MESSAGE_KEY = "message"
    }
}