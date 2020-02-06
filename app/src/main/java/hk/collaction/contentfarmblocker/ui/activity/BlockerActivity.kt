package hk.collaction.contentfarmblocker.ui.activity

import android.os.Bundle
import hk.collaction.contentfarmblocker.R
import hk.collaction.contentfarmblocker.ui.fragment.BlockerFragment

class BlockerActivity : BaseFragmentActivity() {
    override var titleId: Int? = R.string.title_activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        val url = intent.getStringExtra("url")
        val domain = intent.getStringExtra("domain")
        fragment = BlockerFragment.newInstance(url, domain)
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}