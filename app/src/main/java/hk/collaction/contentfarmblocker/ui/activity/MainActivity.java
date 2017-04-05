package hk.collaction.contentfarmblocker.ui.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import hk.collaction.contentfarmblocker.C;
import hk.collaction.contentfarmblocker.R;
import hk.collaction.contentfarmblocker.ui.fragment.MainFragment;

public class MainActivity extends BaseActivity {

	@BindView(R.id.toolbar)
	Toolbar toolbar;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		C.detectLanguage(mContext);
		ActionBar ab = initActionBar(getSupportActionBar(), R.string.title_activity_main);
		ab.setDisplayHomeAsUpEnabled(false);
		ab.setHomeButtonEnabled(false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		C.detectLanguage(mContext);

		setContentView(R.layout.activity_container);
		ButterKnife.bind(this);
		setSupportActionBar(toolbar);

		ActionBar ab = initActionBar(getSupportActionBar(), R.string.title_activity_main);
		ab.setDisplayHomeAsUpEnabled(false);
		ab.setHomeButtonEnabled(false);

		boolean isNoBrowser = false;
		if (getIntent() != null) {
			isNoBrowser = getIntent().getBooleanExtra("no_browser", false);
		}

		Fragment fragment = MainFragment.newInstance(isNoBrowser);
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.container, fragment)
				.commit();
	}
}
