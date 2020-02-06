package hk.collaction.contentfarmblocker.ui.activity

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.preference.PreferenceManager
import hk.collaction.contentfarmblocker.R
import hk.collaction.contentfarmblocker.helper.UtilHelper.goToUrl
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.HashSet
import java.util.Locale

class DetectorActivity : BaseActivity() {

    class CheckingAsyncTask(private val activity: Activity?) : AsyncTask<String, Void?, String>() {
        private val settings = PreferenceManager.getDefaultSharedPreferences(activity)

        override fun doInBackground(vararg strings: String): String {
            var urlString = strings[0]
            if (settings.getBoolean("pref_short_url_checking", true)) {
                val domain = getBaseDomain(urlString)
                if (isShortenUrl(domain)) {
                    activity?.runOnUiThread {
                        Toast.makeText(activity, R.string.toast_redirecting, Toast.LENGTH_LONG).show()
                    }
                    urlString = getRedirectUrl(urlString)
                }
            }
            return urlString
        }

        override fun onPostExecute(urlString: String) {
            activity?.let {
                val domain = getBaseDomain(urlString)
                if (isContentFarm(domain, settings)) {
                    val intent = Intent().setClass(activity, BlockerActivity::class.java)
                    intent.putExtra("url", urlString)
                    intent.putExtra("domain", domain)
                    activity.startActivity(intent)
                    activity.finish()
                } else {
                    goToUrl(activity, urlString)
                }
            }
        }

        private fun isContentFarm(domain: String, settings: SharedPreferences): Boolean {
            val whitelistArray = settings.getString("pref_whitelist", "")?.split("\\n")?.toTypedArray()
            val blacklistArray = settings.getString("pref_blacklist", "")?.split("\\n")?.toTypedArray()
            val inputStream = activity!!.resources.openRawResource(R.raw.site)
            val r = BufferedReader(InputStreamReader(inputStream))
            val defaultSet = HashSet<String>()
            try {
                var line: String
                while (r.readLine().also { line = it } != null) {
                    defaultSet.add(line)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CheckingAsyncTask(this).execute(intent.dataString)
    }
}