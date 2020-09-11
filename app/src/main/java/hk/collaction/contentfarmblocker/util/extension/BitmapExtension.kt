package hk.collaction.contentfarmblocker.util.extension

import android.graphics.Bitmap
import android.graphics.Matrix

fun Bitmap.rotate(degree: Int): Bitmap {
    val w = this.width
    val h = this.height
    val mtx = Matrix()
    mtx.setRotate(degree.toFloat())
    return Bitmap.createBitmap(this, 0, 0, w, h, mtx, true)
}