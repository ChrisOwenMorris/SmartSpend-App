package com.smartspend

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartspend.data.entity.Expense
import com.smartspend.data.entity.Income

class RecentTransactionsAdapter(private val transactions: List<Any>) : RecyclerView.Adapter<RecentTransactionsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDescription: TextView = view.findViewById(R.id.tvTransactionDescription)
        val tvAmount: TextView = view.findViewById(R.id.tvTransactionAmount)
        val tvDate: TextView = view.findViewById(R.id.tvTransactionDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        when (transaction) {
            is Expense -> {
                holder.tvDescription.text = transaction.description
                holder.tvAmount.text = "-R %.2f".format(transaction.amount)
                holder.tvAmount.setTextColor(Color.parseColor("#E53935"))
                holder.tvDate.text = transaction.date
            }
            is Income -> {
                holder.tvDescription.text = transaction.description ?: "Income"
                holder.tvAmount.text = "+R %.2f".format(transaction.amount)
                holder.tvAmount.setTextColor(Color.parseColor("#10B981"))
                holder.tvDate.text = transaction.date
            }
        }
    }

    override fun getItemCount() = transactions.size
}