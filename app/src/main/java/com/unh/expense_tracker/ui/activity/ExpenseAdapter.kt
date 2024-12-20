package com.unh.expense_tracker.ui.activity

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.unh.expense_tracker.R
import com.unh.expense_tracker.ui.activity.expensecard

class ExpenseAdapter(
    private val mChildList: ArrayList<expensecard>,
    private val fragment: ActivityFragment
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

//For recycler view i followed the procedure of what had taught in class
    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mTextView1: TextView = itemView.findViewById(R.id.text_view1)
        val mTextView2: TextView = itemView.findViewById(R.id.text_view2)
        val mTextView3: TextView = itemView.findViewById(R.id.text_view3)
        val mTextView4: TextView = itemView.findViewById(R.id.text_view4)
        val deletebutt: ImageButton = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.item_expenses,parent,false)
        return ExpenseViewHolder(itemView)
    }


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

    override fun getItemCount(): Int {
        return mChildList.size
    }
}
