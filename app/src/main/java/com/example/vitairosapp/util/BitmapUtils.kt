package com.example.vitairosapp.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory

object BitmapUtils {

    /** Mayor potencia de 2 que mantiene el lado mayor sobre [target] px. */
    fun sampleSize(width: Int, height: Int, target: Int): Int {
        var sample = 1
        while (maxOf(width, height) / (sample * 2) >= target) sample *= 2
        return sample
    }

    fun decodeSampled(path: String, target: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0) return null
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, target)
        }
        return BitmapFactory.decodeFile(path, opts)
    }
}
