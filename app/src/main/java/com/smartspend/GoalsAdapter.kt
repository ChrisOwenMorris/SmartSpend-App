package com.smartspend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartspend.data.entity.Goal

class GoalsAdapter(
    private var goals: List<Goal>,
    private val onDeleteClick: (Goal) -> Unit
) : RecyclerView.Adapter<GoalsAdapter.GoalViewHolder>() {

    inner class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvGoalName: TextView = itemView.findViewById(R.id.tvGoalName)
        val tvGoalAmount: TextView = itemView.findViewById(R.id.tvGoalAmount)
        val tvTargetDate: TextView = itemView.findViewById(R.id.tvTargetDate)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        holder.tvGoalName.text = goal.name
        holder.tvGoalAmount.text = "R$${goal.targetAmount}"
        holder.tvTargetDate.text = "Target: ${goal.targetDate}"
        holder.btnDelete.setOnClickListener { onDeleteClick(goal) }
    }

    override fun getItemCount(): Int = goals.size

    fun updateGoals(newGoals: List<Goal>) {
        goals = newGoals
        notifyDataSetChanged()
    }
}
