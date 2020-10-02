package hk.collaction.contentfarmblocker.ui.base

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import hk.collaction.contentfarmblocker.R
import hk.collaction.contentfarmblocker.util.Utils.detectLanguage
import kotlinx.android.synthetic.main.toolbar.*

/**
 * Created by himphen on 21/5/16.
 */
abstract class BaseActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detectLanguage(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    protected fun initActionBar(
        toolbar: Toolbar,
        titleString: String? = null, subtitleString: String? = null,
        @StringRes titleId: Int? = null, @StringRes subtitleId: Int? = null
    ) {
        setSupportActionBar(toolbar)
        supportActionBar?.let { ab ->
            ab.setDisplayHomeAsUpEnabled(true)
            ab.setHomeButtonEnabled(true)
            titleString?.let {
                ab.title = titleString
            }
            titleId?.let {
                ab.setTitle(titleId)
            }
            subtitleString?.let {
                ab.subtitle = subtitleString
            }
            subtitleId?.let {
                ab.setSubtitle(subtitleId)
            }
        }
    }

    fun initFragment(fragment: Fragment?, titleString: String?, titleId: Int?) {
        fragment?.let {
            setContentView(R.layout.activity_container_adview)
            initActionBar(toolbar, titleString = titleString, titleId = titleId)

            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        }
    }
}