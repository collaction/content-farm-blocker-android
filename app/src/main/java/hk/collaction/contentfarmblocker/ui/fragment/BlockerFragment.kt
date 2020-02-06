package hk.collaction.contentfarmblocker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import hk.collaction.contentfarmblocker.R
import hk.collaction.contentfarmblocker.helper.UtilHelper
import kotlinx.android.synthetic.main.fragment_blocker.*

/**
 * BlockerFragment detect the urlString
 *
 *
 * Created by Himphen on 2/4/2017.
 */
class BlockerFragment : BaseFragment() {

    private var urlString = ""
    private var domain = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        urlString = arguments?.getString("url") ?: ""
        domain = arguments?.getString("domain") ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_blocker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        domainTv.text = String.format("%s %s", domain, domainTv.text)
        backButton.setOnClickListener { activity?.finish() }
        goButton.setOnClickListener { UtilHelper.goToUrl(activity, urlString) }
        whitelistButton.setOnClickListener { showWhiteConfirmDialog() }
    }

    private fun showWhiteConfirmDialog() {
        context?.let { context ->
            MaterialDialog(context)
                    .title(R.string.ui_add_to_whitelist)
                    .message(R.string.ui_whitelist_message)
                    .positiveButton(R.string.ui_okay) {
                        val settings = PreferenceManager.getDefaultSharedPreferences(context)
                        var result = settings.getString("pref_whitelist", "")
                        result = (domain.trim { it <= ' ' } + "\n" + result).trim { it <= ' ' }
                        settings.edit().putString("pref_whitelist", result).apply()
                        UtilHelper.goToUrl(activity, urlString)
                    }
                    .negativeButton(R.string.ui_cancel)
                    .show()
        }
    }

    companion object {
        fun newInstance(url: String?, domain: String?): BlockerFragment {
            val fragment = BlockerFragment()
            val args = Bundle()
            args.putString("url", url)
            args.putString("domain", domain)
            fragment.arguments = args
            return fragment
        }
    }
}