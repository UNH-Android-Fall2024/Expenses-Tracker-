package com.unh.expense_tracker.ui.activity

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.unh.expense_tracker.R
import com.unh.expense_tracker.ui.activity.expensecard

class ExpenseAdapter(
    private val mChildList: ArrayList<expensecard>,
    private val context: ActivityFragment
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {


    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mTextView1: TextView = itemView.findViewById(R.id.text_view1)
        val mTextView2: TextView = itemView.findViewById(R.id.text_view2)
        val mTextView3: TextView = itemView.findViewById(R.id.text_view3)
        val mTextView4: TextView = itemView.findViewById(R.id.text_view4)
    }

    // Inflate item layout and create a ViewHolder instance
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.item_expenses,parent,false)
        return ExpenseViewHolder(itemView)
    }

    // Bind data to the views in the ViewHolder
    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expenseCard = mChildList[position]
        holder.mTextView1.text = expenseCard.text1
        holder.mTextView2.text = expenseCard.text2
        holder.mTextView3.text = expenseCard.text3
        holder.mTextView4.text = expenseCard.text4
        //holder.mTextView1.setOnClickListener {
          //  Log.w("test", position.toString())
        //}
    }

    // Return the total number of items in the list
    override fun getItemCount(): Int {
        return mChildList.size
    }
}
