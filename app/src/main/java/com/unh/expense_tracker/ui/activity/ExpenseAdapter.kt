package com.unh.expense_tracker.ui.activity

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.unh.expense_tracker.R
import com.unh.expense_tracker.ui.activity.expensecard

class ExpenseAdapter(
    private val mChildList: ArrayList<expensecard>,
    private val fragment: ActivityFragment
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {


    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mTextView1: TextView = itemView.findViewById(R.id.text_view1)
        val mTextView2: TextView = itemView.findViewById(R.id.text_view2)
        val mTextView3: TextView = itemView.findViewById(R.id.text_view3)
        val mTextView4: TextView = itemView.findViewById(R.id.text_view4)
        val deletebutt: Button = itemView.findViewById(R.id.buttonDelete)
    }

    // Inflate item layout and create a ViewHolder instance
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.item_expenses,parent,false)
        return ExpenseViewHolder(itemView)
    }

    // Bind data to the views in the ViewHolder
    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val currentItem = mChildList[position]
        holder.mTextView1.text = currentItem.text1
        holder.mTextView2.text = currentItem.text2
        holder.mTextView3.text = currentItem.text3
        holder.mTextView4.text = currentItem.text4
        //holder.mTextView1.setOnClickListener {
          //  Log.w("test", position.toString())
        //}
        holder.deletebutt.setOnClickListener {
           fragment.deleteExpense(currentItem)
           // Log.d("funcdelete","inside deletebutt holder")
        }
    }

    // Return the total number of items in the list
    override fun getItemCount(): Int {
        return mChildList.size
    }
}
