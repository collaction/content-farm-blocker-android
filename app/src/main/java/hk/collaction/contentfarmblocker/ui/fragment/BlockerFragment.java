package hk.collaction.contentfarmblocker.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hk.collaction.contentfarmblocker.C;
import hk.collaction.contentfarmblocker.R;

/**
 * BlockerFragment detect the urlString
 * <p>
 * Created by Himphen on 2/4/2017.
 */
public class BlockerFragment extends BaseFragment {

	@BindView(R.id.domainTv)
	TextView domainTv;

	@OnClick(R.id.backButton)
	void onClickBack() {
		mContext.finish();
	}

	private String urlString = "";
	private String domain = "";

	public static BlockerFragment newInstance(String url, String domain) {
		BlockerFragment fragment = new BlockerFragment();
		Bundle args = new Bundle();
		args.putString("url", url);
		args.putString("domain", domain);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			urlString = getArguments().getString("url");
			domain = getArguments().getString("domain");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_blocker, container, false);
		ButterKnife.bind(this, rootView);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		domainTv.setText(domain + " " + domainTv.getText());
	}

	@OnClick(R.id.goButton)
	public void goToUrl() {
		C.goToUrl(mContext, urlString);
	}

	@OnClick(R.id.whitelistButton)
	public void whitelist() {
		MaterialDialog.Builder dialog = new MaterialDialog.Builder(mContext)
				.title(R.string.ui_add_to_whitelist)
				.content(R.string.ui_whitelist_message)
				.positiveText(R.string.ui_okay)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
						String result = settings.getString("pref_whitelist", "");
						result = (domain.trim() + "\n" + result).trim();
						settings.edit().putString("pref_whitelist", result).apply();
						goToUrl();
					}
				})
				.negativeText(R.string.ui_cancel);
		dialog.show();
	}
}
