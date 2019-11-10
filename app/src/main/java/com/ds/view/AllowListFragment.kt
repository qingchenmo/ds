package com.ds.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.ds.R

class AllowListFragment : Fragment() {

    private val TAG = "AllowListFragment"
    private var mAllowListView: RecyclerView? = null
    private var mActivity: MainActivity1? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.allow_list_fragment, container, false)
        mActivity = activity as MainActivity1
        mAllowListView = view?.findViewById(R.id.allow_list)

        view?.findViewById<View>(R.id.add)?.setOnClickListener { mActivity?.addAllowInfo(null) }

        var layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mAllowListView?.layoutManager = layoutManager
//
        mAllowListView?.adapter = AllowRecycleAdapter(mActivity!!, mActivity!!.mAllowListBeans)
        return view
    }

    fun refreList() {
        mAllowListView?.adapter?.notifyDataSetChanged()
    }

    class AllowRecycleAdapter(context: MainActivity1, list: ArrayList<AllowListBean>) : RecyclerView.Adapter<AllowRecycleHolder>() {
        private var mContext = context
        private var mAllowListBeans = list
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AllowRecycleHolder {
            return AllowRecycleHolder(LayoutInflater.from(mContext).inflate(R.layout.allow_list_item_layout, parent, false))
        }

        override fun getItemCount(): Int {
            return mAllowListBeans.size
        }

        override fun onBindViewHolder(holder: AllowRecycleHolder?, position: Int) {
            holder?.name?.text = "名称：${mAllowListBeans[position].name}"
            holder?.chepai?.text = "车牌：${mAllowListBeans[position].chepai}"
            holder?.is_allow?.isChecked = mAllowListBeans[position].isAllow
            holder?.count?.text = "已停次数：${mAllowListBeans[position].count}次"
            holder?.delete?.setOnClickListener {
                mContext.removeAllowInfo(mAllowListBeans[position])
            }

            holder?.is_allow?.setOnCheckedChangeListener { buttonView, isChecked ->
                mAllowListBeans[position].isAllow = isChecked
            }
            holder?.line?.visibility = if (position == itemCount - 1) View.GONE else View.VISIBLE

            when (position % 4) {
                0 -> {
                    holder?.car?.setImageResource(R.mipmap.car_1)
                }
                1 -> {
                    holder?.car?.setImageResource(R.mipmap.car_2)
                }
                2 -> {
                    holder?.car?.setImageResource(R.mipmap.car_3)
                }
                3 -> {
                    holder?.car?.setImageResource(R.mipmap.car_4)
                }
            }

            holder?.confirm?.setOnClickListener {
                mContext.confirAllowInfo(mAllowListBeans[position])
            }
        }
    }

    class AllowRecycleHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.name)
        var chepai = itemView.findViewById<TextView>(R.id.chepai)
        var is_allow = itemView.findViewById<CheckBox>(R.id.is_allow)
        var count = itemView.findViewById<TextView>(R.id.count)
        var delete = itemView.findViewById<TextView>(R.id.delete)
        var line = itemView.findViewById<View>(R.id.line)
        var car = itemView.findViewById<ImageView>(R.id.car)
        var confirm = itemView.findViewById<View>(R.id.confirm)
    }
}