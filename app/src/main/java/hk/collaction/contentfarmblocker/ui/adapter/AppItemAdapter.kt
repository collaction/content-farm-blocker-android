package hk.collaction.contentfarmblocker.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hk.collaction.contentfarmblocker.R
import hk.collaction.contentfarmblocker.model.AppItem

/**
 * Created by himphen on 25/5/16.
 */
class AppItemAdapter(
        private val dataList: List<AppItem>,
        private val listener: ItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface ItemClickListener {
        fun onItemDetailClick(appItem: AppItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_info_app, parent, false))
    }

    override fun onBindViewHolder(rawHolder: RecyclerView.ViewHolder, position: Int) {
        val item = dataList[position]
        val holder = rawHolder as ItemViewHolder
        holder.titleTv.text = item.appName
        holder.contentTv.text = item.packageName
        holder.iconIv.setImageDrawable(item.icon)
        holder.rootView.tag = item
        holder.rootView.setOnClickListener { view -> listener.onItemDetailClick(view.tag as AppItem) }
    }

    override fun getItemCount(): Int = dataList.size

    internal class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var titleTv: TextView = view.findViewById(R.id.text1)
        var contentTv: TextView = view.findViewById(R.id.text2)
        var iconIv: ImageView = view.findViewById(R.id.icon)
        var rootView: LinearLayout = view.findViewById(R.id.root_view)
    }

}