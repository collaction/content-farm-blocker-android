package hk.collaction.contentfarmblocker.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hk.collaction.contentfarmblocker.R;
import hk.collaction.contentfarmblocker.model.AppItem;

/**
 * Created by himphen on 25/5/16.
 */
public class AppItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private List<AppItem> mDataList;
	private ItemClickListener mListener;

	public interface ItemClickListener {
		void onItemDetailClick(AppItem appItem);
	}

	public AppItemAdapter(List<AppItem> mDataList, ItemClickListener mListener) {
		this.mDataList = mDataList;
		this.mListener = mListener;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Context mContext = parent.getContext();

		View itemView = LayoutInflater.from(mContext).inflate(R.layout.list_item_info_app, parent, false);
		return new ItemViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder rawHolder, int position) {
		AppItem item = mDataList.get(position);
		ItemViewHolder holder = (ItemViewHolder) rawHolder;

		holder.titleTv.setText(item.getAppName());
		holder.contentTv.setText(item.getPackageName());
		holder.iconIv.setImageDrawable(item.getIcon());

		holder.rootView.setTag(item);
		holder.rootView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mListener.onItemDetailClick((AppItem) view.getTag());
			}
		});
	}

	@Override
	public int getItemCount() {
		return mDataList == null ? 0 : mDataList.size();
	}

	static class ItemViewHolder extends RecyclerView.ViewHolder {

		@BindView(R.id.text1)
		TextView titleTv;
		@BindView(R.id.text2)
		TextView contentTv;
		@BindView(R.id.icon)
		ImageView iconIv;
		@BindView(R.id.root_view)
		LinearLayout rootView;

		ItemViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}
	}
}