package hk.collaction.contentfarmblocker.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import hk.collaction.contentfarmblocker.C;

/**
 * Created by himphen on 21/5/16.
 */
@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

	protected Activity mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		C.detectLanguage(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	protected ActionBar initActionBar(ActionBar ab, String title) {
		return initActionBar(ab, title, null);
	}

	protected ActionBar initActionBar(ActionBar ab, String title, String subtitle) {
		if (ab != null) {
			ab.setElevation(100);
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setHomeButtonEnabled(true);
			ab.setTitle(title);

			if (subtitle != null) {
				ab.setSubtitle(subtitle);
			}
		}

		return ab;
	}

	protected ActionBar initActionBar(ActionBar ab, int titleId) {
		return initActionBar(ab, titleId, 0);
	}

	protected ActionBar initActionBar(ActionBar ab, int titleId, int subtitleId) {
		if (ab != null) {
			ab.setElevation(100);
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setHomeButtonEnabled(true);
			ab.setTitle(titleId);

			if (subtitleId != 0) {
				ab.setSubtitle(subtitleId);
			}
		}
		return ab;
	}

	public void setActionBarTitle(int titleId) {
		ActionBar ab = getSupportActionBar();
		if (ab != null) {
			ab.setTitle(titleId);
		}
	}

	public void setActionBarTitle(String title) {
		ActionBar ab = getSupportActionBar();
		if (ab != null) {
			ab.setTitle(title);
		}
	}
}
