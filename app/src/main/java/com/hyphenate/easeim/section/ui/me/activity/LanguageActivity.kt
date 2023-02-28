package com.hyphenate.easeim.section.ui.me.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import com.hyphenate.chat.EMLanguage
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.utils.SpDbModel.Companion.instance
import com.hyphenate.easeim.databinding.ActivityLanguageBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.me.activity.LanguageActivity
import com.hyphenate.easeim.section.ui.me.adapter.LanguageAdapter
import com.hyphenate.easeui.widget.EaseTitleBar
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener
import com.hyphenate.easeui.widget.EaseTitleBar.OnRightClickListener

class LanguageActivity(override val layoutId: Int = R.layout.activity_language) :
    BaseInitActivityKtx<ActivityLanguageBinding>(), AdapterView.OnItemClickListener {
    private var adapter: LanguageAdapter? = null
    private val emLanguageList: MutableList<EMLanguage> = ArrayList()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        //获取微软支持的翻译语言
//        emLanguageList = EMClient.getInstance().translationManager().getSupportedLanguages();
        if (emLanguageList.size <= 0) {
            // 默认多语言列表
            defaultLanguages()
        }
        adapter = LanguageAdapter(this, emLanguageList)
        binding.languageList.adapter = adapter
        initSelectedLanguage()
        binding.languageList.onItemClickListener = this
    }

    override fun initListener() {
        super.initListener()
        binding.titleBarLanguage.setOnBackPressListener {
            onBackPressed()
        }
        binding.titleBarLanguage.setOnRightClickListener {
            updateLanguage()
            onBackPressed()
        }
    }

    private fun initSelectedLanguage() {
        val languageCode = instance.getTargetLanguage()
        var selectedIndex = 0
        for (index in emLanguageList.indices) {
            val language = emLanguageList[index]
            if (language.LanguageCode == languageCode) {
                selectedIndex = index
                break
            }
        }
        adapter!!.selectedIndex = selectedIndex.toLong()
    }

    private fun defaultLanguages() {
        emLanguageList.add(EMLanguage("zh-Hans", "中文 (简体)", "中文 (简体)"))
        emLanguageList.add(EMLanguage("zh-Hant", "繁體中文 (繁體)", "繁體中文 (繁體)"))
        emLanguageList.add(EMLanguage("en", "English", "English"))
        emLanguageList.add(EMLanguage("id", "Indonesia", "Indonesia"))
        emLanguageList.add(EMLanguage("ko", "한국어", "한국어"))
        emLanguageList.add(EMLanguage("it", "Italiano", "Italiano"))
        emLanguageList.add(EMLanguage("pt", "Português (Brasil)", "Português (Brasil)"))
        emLanguageList.add(EMLanguage("ja", "日本語", "日本語"))
        emLanguageList.add(EMLanguage("fr", "Français", "Français"))
        emLanguageList.add(EMLanguage("de", "Deutsch", "Deutsch"))
    }

    private fun updateLanguage() {
        val selectedIndex = adapter!!.selectedIndex
        val languageCode = emLanguageList[selectedIndex.toInt()].LanguageCode
        instance.setTargetLanguage(languageCode)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        if (id != adapter!!.selectedIndex) {
            adapter!!.selectedIndex = id
            adapter!!.notifyDataSetChanged()
        }
    }

    companion object {
        fun actionStart(context: Context) {
            val starter = Intent(context, LanguageActivity::class.java)
            context.startActivity(starter)
        }
    }
}