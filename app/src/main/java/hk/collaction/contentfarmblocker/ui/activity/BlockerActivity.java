package hk.collaction.contentfarmblocker.ui.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import hk.collaction.contentfarmblocker.C;
import hk.collaction.contentfarmblocker.R;
import hk.collaction.contentfarmblocker.ui.fragment.BlockerFragment;

public class BlockerActivity extends BaseActivity {

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
		setContentView(R.layout.activity_container);
		ButterKnife.bind(this);
		setSupportActionBar(toolbar);

		ActionBar ab = initActionBar(getSupportActionBar(), R.string.title_activity_main);
		ab.setDisplayHomeAsUpEnabled(false);
		ab.setHomeButtonEnabled(false);

		String url = getIntent().getStringExtra("url");
		String domain = getIntent().getStringExtra("domain");

		Fragment fragment = BlockerFragment.newInstance(url, domain);
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.container, fragment)
				.commit();
	}
}
