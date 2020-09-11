package hk.collaction.contentfarmblocker.ui.detector

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.preference.PreferenceManager
import hk.collaction.contentfarmblocker.R
import hk.collaction.contentfarmblocker.ui.base.BaseActivity
import hk.collaction.contentfarmblocker.ui.blocker.BlockerActivity
import hk.collaction.contentfarmblocker.util.UtilHelper.goToUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.HashSet
import java.util.Locale

class DetectorActivity : BaseActivity() {
    private lateinit var settings: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = PreferenceManager.getDefaultSharedPreferences(this)

        GlobalScope.launch(Dispatchers.Main) {
            intent.dataString?.let { urlString ->
                var result = urlString
                if (settings.getBoolean("pref_short_url_checking", true)) {
                    val domain = getBaseDomain(urlString)
                    if (isShortenUrl(domain)) {
                        runOnUiThread {
                            Toast.makeText(
                                this@DetectorActivity,
                                R.string.toast_redirecting,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        result = getRedirectUrl(urlString)
                    }
                }

                val domain = getBaseDomain(result)
                if (isContentFarm(domain, settings)) {
                    val intent =
                        Intent(this@DetectorActivity, BlockerActivity::class.java)
                    intent.putExtra("url", result)
                    intent.putExtra("domain", domain)
                    startActivity(intent)
                    finish()
                } else {
                    goToUrl(this@DetectorActivity, result)
                }
            }
        }
    }

    private fun isContentFarm(domain: String, settings: SharedPreferences): Boolean {
        val whitelistArray =
            settings.getString("pref_whitelist", "")?.split("\\n")?.toTypedArray()
        val blacklistArray =
            settings.getString("pref_blacklist", "")?.split("\\n")?.toTypedArray()
        val inputStream = resources.openRawResource(R.raw.site)
        val r = BufferedReader(InputStreamReader(inputStream))
        val defaultSet = HashSet<String>()
        try {
            var line: String
            while (r.readLine().also { line = it } != null) {
                defaultSet.add(line.substringBeforeLast(" //"))
            }
        } catch (ignored: Exception) {
        }
        blacklistArray?.isNotEmpty()?.let {
            if (it) {
                defaultSet.addAll(blacklistArray)
            }
        }
        whitelistArray?.isNotEmpty()?.let {
            if (it) {
                defaultSet.removeAll(whitelistArray)
            }
        }
        return defaultSet.contains(domain.toLowerCase(Locale.getDefault()))
    }

    private fun getRedirectUrl(urlString: String): String {
        var urlStringTemp = urlString
        val domain = getBaseDomain(urlStringTemp)
        if (isShortenUrl(domain)) {
            try {
                val url = URL(urlStringTemp)
                val connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = false
                var redirectUrl = connection.getHeaderField("Location")
                if (redirectUrl == null) {
                    val entrySet = connection.headerFields["Refresh"]
                    if (entrySet != null) {
                        for (refreshUrl in entrySet) {
                            redirectUrl = refreshUrl.replace("1;URL=", "")
                        }
                    }
                }
                if (redirectUrl != null) {
                    urlStringTemp = getRedirectUrl(redirectUrl)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return urlStringTemp
    }


    private fun isShortenUrl(domain: String): Boolean {
        return HashSet(listOf(*shortenDomainArray)).contains(domain)
    }

    /**
     * Will get a domain
     * - Parse any blogspot to blogspot.*
     * - Remove www.
     *
     * @param urlString String
     * @return String
     */
    private fun getBaseDomain(urlString: String): String {
        return try {
            val aURL = URL(urlString)
            var domain = aURL.host
            // Parse any blogspot to blogspot.*
            if (domain.contains(".blogspot.")) {
                domain = domain.replace("(.*)(\\.blogspot\\..*)".toRegex(), ".$1.*")
            }
            // Remove www.
            if (domain.startsWith("www.")) domain.substring(4) else domain
        } catch (ignored: MalformedURLException) {
            urlString
        }
    }

    private val shortenDomainArray = arrayOf(
        "7.ly",
        "al.ly",
        "bit.do",
        "bit.ly",
        "goo.gl",
        "tiny.cc",
        "tr.im",
        "y2u.be",
        "lm.facebook.com"
    )
}