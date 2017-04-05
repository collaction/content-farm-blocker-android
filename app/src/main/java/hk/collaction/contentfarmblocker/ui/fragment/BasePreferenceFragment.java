package hk.collaction.contentfarmblocker.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

public class BasePreferenceFragment extends PreferenceFragmentCompat {

	protected Activity mContext;

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		mContext = getActivity();
	}

}
