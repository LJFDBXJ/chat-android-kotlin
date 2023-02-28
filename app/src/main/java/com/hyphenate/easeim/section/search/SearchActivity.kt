package com.hyphenate.easeim.section.search

import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DividerItemDecoration
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.ActivitySearchBinding
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter

abstract class SearchActivity : BaseInitActivityKtx<ActivitySearchBinding>() {

    override val layoutId: Int
        get() = R.layout.activity_search

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        //让EditText获取焦点并弹出软键盘
        binding.query.requestFocus()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener {
            onBackPressed()
        }
        binding.query.doAfterTextChanged {
            binding.searchClear.visibility = if (it.isNullOrEmpty()) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }
        }


        binding.query.setOnEditorActionListener(object : OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val search = binding.query.text.toString()
                    if (search.isNotEmpty()) {
                        searchMessages(search)
                    }
                    hideKeyboard()
                    return true
                }
                return false
            }

        })
        binding.searchClear.setOnClickListener {
            binding.query.text?.clear()
        }
        binding.tvCancel.setOnClickListener { onBackPressed() }

    }

    override fun initData() {
        super.initData()

//        binding.rvList.adapter = adapter
//        adapter?.setOnItemClickListener { adapter, view, position ->
//            onChildItemClick(view, position)
//        }
    }

    abstract fun searchMessages(search: String)
}