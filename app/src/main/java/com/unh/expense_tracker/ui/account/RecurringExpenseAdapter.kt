package com.unh.expense_tracker.ui.account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unh.expense_tracker.R

class RecurringExpenseAdapter(
    private val mChildList: ArrayList<recurringexpensecard>,
    private val fragment: RecurringExpenseFragment
) : RecyclerView.Adapter<RecurringExpenseAdapter.RecurringExpenseViewHolder>() {

    inner class RecurringExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mTextView1: TextView = itemView.findViewById(R.id.text_view1)
        val mTextView2: TextView = itemView.findViewById(R.id.text_view2)
        val mTextView3: TextView = itemView.findViewById(R.id.text_view3)
        val mTextView4: TextView = itemView.findViewById(R.id.text_view4)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecurringExpenseViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recurring_expenses, parent, false)
        return RecurringExpenseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecurringExpenseViewHolder, position: Int) {
        val currentItem = mChildList[position]
        holder.mTextView1.text = currentItem.text1
        holder.mTextView2.text = currentItem.text2
        holder.mTextView3.text = currentItem.text3
        holder.mTextView4.text = currentItem.text4
    }

    override fun getItemCount(): Int {
        return mChildList.size
    }
}