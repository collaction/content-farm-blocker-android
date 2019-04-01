package hk.collaction.contentfarmblocker.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {

	protected Activity mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
	}

}
