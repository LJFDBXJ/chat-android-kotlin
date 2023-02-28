package com.hyphenate.easeim.common.ktx


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

var makeText: Toast? = null

@SuppressLint("ShowToast")
fun Context?.toast(msg: String, duration: Int = Toast.LENGTH_SHORT) {
    CoroutineScope(Dispatchers.Main).launch {
        makeText?.cancel()
        makeText = Toast.makeText(this@toast, msg, duration)
        makeText?.setGravity(Gravity.CENTER, 0, 0)
        makeText?.show()
    }
}

@SuppressLint("ShowToast")
fun Context?.toast(@StringRes msg: Int, duration: Int = Toast.LENGTH_SHORT) {
    CoroutineScope(Dispatchers.Main).launch {
        makeText?.cancel()
        makeText = Toast.makeText(this@toast, msg, duration)
        makeText?.setGravity(Gravity.CENTER, 0, 0)
        makeText?.show()
    }
}

fun Fragment?.toast(@StringRes msg: Int, duration: Int = Toast.LENGTH_SHORT) {

    CoroutineScope(Dispatchers.Main).launch {
        makeText?.cancel()
        makeText = Toast.makeText(this@toast?.requireContext(), msg, duration)
        makeText?.setGravity(Gravity.CENTER, 0, 0)
        makeText?.show()
    }
}
fun Fragment?.toast( msg: String, duration: Int = Toast.LENGTH_SHORT) {

    CoroutineScope(Dispatchers.Main).launch {
        makeText?.cancel()
        makeText = Toast.makeText(this@toast?.requireContext(), msg, duration)
        makeText?.setGravity(Gravity.CENTER, 0, 0)
        makeText?.show()
    }
}

inline fun <reified T : Activity> Context?.jump(bundle: Bundle? = null, flag: Int? = null) {
    val intent = Intent(this, T::class.java)
    bundle?.let {
        intent.putExtras(it)
    }
    flag?.let {
        intent.flags = flag
    }
    this?.startActivity(intent)
}

inline fun <reified T : Activity> Context?.jumpBundle(bundle: Bundle? = null, flag: Int? = null) {
    val intent = Intent(this, T::class.java)

    flag?.let {
        intent.flags = flag
    }
    this?.startActivity(intent, bundle)
}

inline fun <reified T : Activity> Activity?.jumpResult(requestCode: Int, bundle: Bundle? = null) {
    val intent = Intent(this, T::class.java)
    bundle?.let {
        intent.putExtras(it)
    }
    this?.startActivityForResult(intent, requestCode, bundle)
}

fun Context?.toInt(data: String?): Int {
    return when {
        data.isNullOrEmpty() -> 0
        data.isDigitsOnly() -> data.toInt()
        else -> 0
    }
}

fun Context?.finishAc() {
    if (this is AppCompatActivity)
        finish()
}

fun AppCompatEditText.number(): Pair<Boolean,String> {
    val data = hashSetOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
    val value=editableText.toString()
    return data.contains(value) to value
}
