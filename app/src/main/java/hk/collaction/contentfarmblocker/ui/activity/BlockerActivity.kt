package hk.collaction.contentfarmblocker.ui.activity

import android.os.Bundle
import android.os.Handler
import com.blankj.utilcode.util.SizeUtils
import com.google.android.gms.ads.AdView
import hk.collaction.contentfarmblocker.R
import hk.collaction.contentfarmblocker.helper.UtilHelper
import hk.collaction.contentfarmblocker.ui.fragment.BlockerFragment
import kotlinx.android.synthetic.main.activity_container_adview.*

class BlockerActivity : BaseFragmentActivity() {
    override var titleId: Int? = R.string.title_activity_main

    private var adView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val url = intent.getStringExtra("url")
        val domain = intent.getStringExtra("domain")
        fragment = BlockerFragment.newInstance(url, domain)
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        adLayout.setBackgroundResource(R.color.primary_dark)
        adLayout.layoutParams.height = SizeUtils.dp2px(50f)

        Handler().postDelayed({
            adView = UtilHelper.initAdView(this, adLayout)
        }, UtilHelper.DELAY_AD_LAYOUT)
    }

    public override fun onDestroy() {
        adView?.removeAllViews()
        adView?.destroy()
        super.onDestroy()
    }
}