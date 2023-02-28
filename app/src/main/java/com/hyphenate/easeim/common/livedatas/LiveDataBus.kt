package com.hyphenate.easeim.common.livedatas

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer



fun <T> String.obs(lifecycleOwner: LifecycleOwner, result: Function1<T?, Unit>) {
    LiveDataBus.get().obs<T>(this).observe(lifecycleOwner) {
        result(it)
    }
}

fun <T> Array<String>.obs(lifecycleOwner: LifecycleOwner, result: Function2<String,T?, Unit>) {
    forEach { key ->
        LiveDataBus.get().obs<T>(key).observe(lifecycleOwner) {
            result(key,it)
        }
    }
}

class LiveDataBus private constructor() {
    private val bus = HashMap<String, MutableLiveData<Any>>()


    fun <T> obs(key: String): MutableLiveData<T> {
        if (!bus.containsKey(key)) {
            bus[key] = MutableLiveData()
        }
        return bus[key] as MutableLiveData<T>
    }

    fun use(key: String): MutableLiveData<Any> {
        if (!bus.containsKey(key)) {
            bus[key] = MutableLiveData()
        }
        return bus[key]!!
    }


    companion object {
        private val DEFAULT_BUS = LiveDataBus()

        fun get(): LiveDataBus {
            return DEFAULT_BUS
        }
    }


}
