package com.hyphenate.easeim.section.base_ktx

import android.content.Context
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.webkit.*
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.ActivityBaseWebviewBinding

class WebViewActivity(override val layoutId: Int = R.layout.activity_base_webview) :
    BaseInitActivityKtx<ActivityBaseWebviewBinding>() {
    private var url: String? = null
    private var showTitle = false


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        url = intent.getStringExtra("url")
        showTitle = intent.getBooleanExtra("showTitle", true)
        if (!showTitle) {
            binding.titleBar.visibility = View.GONE
        }
    }

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener { //判断网页是否可以后退
            if (binding.webview.canGoBack()) {
                binding.webview.goBack()
            } else {
                onBackPressed()
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        binding.webview.onResume()
    }

    public override fun onPause() {
        super.onPause()
        binding.webview.onPause()
    }

    override fun initData() {
        super.initData()
        if (!TextUtils.isEmpty(url)) {
            binding.webview.loadUrl(url!!)
        }
        //配置WebSettings
        val settings = binding.webview.settings
        //设置自适应屏幕，两者合用
        settings.useWideViewPort = true //将图片调整到适合webview的大小
        settings.loadWithOverviewMode = true // 缩放至屏幕的大小
        //缩放操作
        settings.setSupportZoom(true) //支持缩放，默认为true。是下面那个的前提。
        settings.builtInZoomControls = true //设置内置的缩放控件。若为false，则该WebView不可缩放
        settings.displayZoomControls = false //隐藏原生的缩放控件
        //其他操作
        settings.loadsImagesAutomatically = true //支持自动加载图片

        //配置WebViewClient，使用WebView加载
        binding.webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(url ?: "")
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                handler.proceed() //表示等待证书相应
            }
        }

        //配置WebChromeClient类
        binding.webview.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, title: String) {
                binding.titleBar.setTitle(title)
            }

            override fun onProgressChanged(view: WebView, newProgress: Int) {
                if (newProgress < 100) {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressBar.progress = newProgress
                } else {
                    binding.progressBar.visibility = View.INVISIBLE
                }
            }
        }
    }

    companion object {
        fun actionStart(context: Context, url: String?) {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", url)
            context.startActivity(intent)
        }

        fun actionStart(context: Context, url: String?, showTitle: Boolean) {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", url)
            intent.putExtra("showTitle", showTitle)
            context.startActivity(intent)
        }
    }
}