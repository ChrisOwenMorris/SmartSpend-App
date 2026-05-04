package com.smartspend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartspend.data.entity.Expense

class MonthlyEntriesAdapter(private val expenses: List<Expense>) : RecyclerView.Adapter<MonthlyEntriesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDescription: TextView = view.findViewById(R.id.tvEntryDescription)
        val tvAmount: TextView = view.findViewById(R.id.tvEntryAmount)
        val tvDate: TextView = view.findViewById(R.id.tvEntryDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_monthly_entry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenses[position]
        holder.tvDescription.text = expense.description
        holder.tvAmount.text = "R %.2f".format(expense.amount)
        holder.tvDate.text = expense.date
    }

    override fun getItemCount() = expenses.size
}