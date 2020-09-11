package hk.collaction.contentfarmblocker.util.extension

import java.math.BigInteger
import java.security.MessageDigest

fun String.md5(): String {
    return try {
        val md = MessageDigest.getInstance("MD5")
        BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    } catch (e: Exception) {
        ""
    }
}