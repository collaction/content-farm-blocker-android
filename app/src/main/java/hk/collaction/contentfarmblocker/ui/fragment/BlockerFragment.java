package hk.collaction.contentfarmblocker.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.OnTouch;
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

	@OnTouch(R.id.goButton)
	boolean onTouchGo(View view, MotionEvent motionEvent) {
		if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
			Toast.makeText(mContext, "長按以進入內容農場", Toast.LENGTH_SHORT).show();

		return false;
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

	@OnLongClick(R.id.goButton)
	boolean goToUrl() {
		C.goToUrl(mContext, urlString);

		return true;
	}
}
