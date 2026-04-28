package com.smartspend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartspend.data.dao.CategoryWithTotal

class TopCategoriesAdapter(private val items: List<CategoryWithTotal>) :
    RecyclerView.Adapter<TopCategoriesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvMerchantName)
        val tvAmount: TextView = view.findViewById(R.id.tvMerchantAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_merchant, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.categoryName
        holder.tvAmount.text = "R %.2f".format(item.total)
    }

    override fun getItemCount() = items.size
}