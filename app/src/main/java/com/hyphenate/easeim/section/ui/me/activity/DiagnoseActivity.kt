package com.hyphenate.easeim.section.ui.me.activity

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.hyphenate.EMCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.easeim.R
import com.hyphenate.easeim.databinding.ActivityDiagnoseBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.util.EMLog


class DiagnoseActivity(override val layoutId: Int = R.layout.activity_diagnose) :
    BaseInitActivityKtx<ActivityDiagnoseBinding>() {

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener { view: View? -> onBackPressed() }
        binding.buttonUploadlog.setOnClickListener { v: View? -> uploadlog() }
    }

    override fun initData() {
        super.initData()
        var strVersion = ""
        try {
            strVersion = versionName
        } catch (e: Exception) {
        }
        if (!TextUtils.isEmpty(strVersion)) binding.tvVersion.text = "V$strVersion" else {
            val st = resources.getString(R.string.Not_Set)
            binding.tvVersion.text = st
        }
    }

    @get:Throws(Exception::class)
    private val versionName: String
         get() = EMClient.VERSION
    private var progressDialog: ProgressDialog? = null
    fun uploadlog() {
        if (progressDialog == null) progressDialog = ProgressDialog(this)
        val stri = resources.getString(R.string.Upload_the_log)
        progressDialog!!.setMessage(stri)
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
        val st = resources.getString(R.string.Log_uploaded_successfully)
        try {
            EMClient.getInstance().uploadLog(object : EMCallBack {
                override fun onSuccess() {
                    runOnUiThread {
                        progressDialog?.dismiss()
                        Toast.makeText(
                            this@DiagnoseActivity, st,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onProgress(progress: Int, status: String) {
                    // getActivity().runOnUiThread(new Runnable() {
                    //
                    // @Override
                    // public void run() {
                    // progressDialog.setMessage("上传中 "+progress+"%");
                    //
                    // }
                    // });
                }

                override fun onError(code: Int, message: String) {
                    EMLog.e("###", message)
                    runOnUiThread {
                        progressDialog?.dismiss()
                        val st3 = resources.getString(R.string.Log_Upload_failed)
                        Toast.makeText(
                            this@DiagnoseActivity, st3,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun actionStart(context: Context) {
            val starter = Intent(context, DiagnoseActivity::class.java)
            context.startActivity(starter)
        }
    }
}