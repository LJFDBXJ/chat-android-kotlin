package com.hyphenate.easeim.common.utils.video

import android.hardware.Camera
import android.os.Build
import android.os.Build.VERSION_CODES
import java.util.Comparator

object Utils {

    @JvmStatic
	fun hasFroyo(): Boolean {
        return Build.VERSION.SDK_INT >= VERSION_CODES.FROYO
    }

    @JvmStatic
	fun hasGingerbread(): Boolean {
        return Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD
    }

    @JvmStatic
	fun hasHoneycomb(): Boolean {
        return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB
    }

    @JvmStatic
	fun hasHoneycombMR1(): Boolean {
        return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1
    }

    fun hasJellyBean(): Boolean {
        return Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN
    }

    @JvmStatic
	fun hasKitKat(): Boolean {
        return Build.VERSION.SDK_INT >= 19
    }

    @JvmStatic
	fun getResolutionList(camera: Camera): List<Camera.Size> {
        val parameters = camera.parameters
        return parameters.supportedPreviewSizes
    }

    class ResolutionComparator : Comparator<Camera.Size> {
        override fun compare(lhs: Camera.Size, rhs: Camera.Size): Int {
            return if (lhs.height != rhs.height) lhs.height - rhs.height else lhs.width - rhs.width
        }
    }
}