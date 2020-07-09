package hk.collaction.contentfarmblocker.ui.activity

import android.os.Bundle
import hk.collaction.contentfarmblocker.R
import hk.collaction.contentfarmblocker.helper.UtilHelper
import hk.collaction.contentfarmblocker.ui.fragment.MainFragment

class MainActivity : BaseFragmentActivity() {
    override var titleId: Int? = R.string.title_activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        val isNoBrowser: Boolean = intent.getBooleanExtra("no_browser", false)
        fragment = MainFragment.newInstance(isNoBrowser)
        super.onCreate(savedInstanceState)
        supportActionBar?.setHomeButtonEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}