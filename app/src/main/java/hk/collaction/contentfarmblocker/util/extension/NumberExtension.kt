package hk.collaction.contentfarmblocker.util.extension

import android.content.res.Resources
import android.util.TypedValue
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.roundToInt

fun Float.dp2px(): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    ).toInt()

fun Float.roundTo(n: Int): Float {
    return this.toDouble().roundTo(n).toFloat()
}

fun Double.roundTo(n: Int): Double {
    if (this.isNaN()) return 0.0

    return try {
        BigDecimal(this).setScale(n, RoundingMode.HALF_EVEN).toDouble()
    } catch (e: NumberFormatException) {
        this.roundToInt().toDouble()
    }
}

fun Double.formatSignificant(significant: Int): String {
    val mathContext = MathContext(significant, RoundingMode.DOWN)
    val bigDecimal = BigDecimal(this, mathContext)
    return bigDecimal.toPlainString()
}