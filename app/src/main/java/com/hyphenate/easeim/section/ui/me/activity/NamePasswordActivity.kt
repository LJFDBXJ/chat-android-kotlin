package com.hyphenate.easeim.section.ui.me.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.ActivityNamePasswordBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeui.widget.EaseTitleBar

class NamePasswordActivity(override val layoutId: Int = R.layout.activity_name_password) :
    BaseInitActivityKtx<ActivityNamePasswordBinding>() {
    private var titleBar: EaseTitleBar? = null


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        titleBar = findViewById(R.id.title_bar)
    }

    override fun initListener() {
        super.initListener()
        titleBar!!.setOnBackPressListener { view -> back(view) }
    }

    fun back(view: View?) {
        setResult(RESULT_CANCELED)
        finish()
    }

    fun onOk(view: View?) {
        val username =
            (findViewById<View>(R.id.username) as EditText).text.toString().trim { it <= ' ' }
        val password =
            (findViewById<View>(R.id.password) as EditText).text.toString().trim { it <= ' ' }
        setResult(RESULT_OK, Intent().putExtra("username", username).putExtra("password", password))
        finish()
    }

    fun onCancel(view: View?) {
        setResult(RESULT_CANCELED)
        finish()
    }
}