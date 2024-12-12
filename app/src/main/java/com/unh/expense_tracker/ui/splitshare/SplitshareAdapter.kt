package com.unh.expense_tracker.ui.splitshare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unh.expense_tracker.R

class SplitshareAdapter(
    private val mChildList: ArrayList<splitsharecard>,
    private val fragment: SplitshareFragment
) : RecyclerView.Adapter<SplitshareAdapter.SplitshareViewHolder>() {
// For split shares i have followed the procedure of what had taught in class
    inner class SplitshareViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mTextView1: TextView = itemView.findViewById(R.id.text_view1)
        val mTextView2: TextView = itemView.findViewById(R.id.text_view2)
        val mTextView3: TextView = itemView.findViewById(R.id.text_view3)
        val mTextView4: TextView = itemView.findViewById(R.id.text_view4)
        val paidButton: Button = itemView.findViewById(R.id.buttonPaid)
    }
// For split shares i have followed the procedure of what had taught in class
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SplitshareViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.splits_shares, parent, false)
        return SplitshareViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SplitshareViewHolder, position: Int) {
        val currentItem = mChildList[position]
        holder.mTextView1.text = currentItem.text1
        holder.mTextView2.text = currentItem.text2
        holder.mTextView3.text = currentItem.text3
        holder.mTextView4.text = currentItem.text4

        holder.paidButton.setOnClickListener {
         //for delete the item in recycler list i took syntax from chatgpt
            fragment.deleteSplit(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return mChildList.size
    }
}
