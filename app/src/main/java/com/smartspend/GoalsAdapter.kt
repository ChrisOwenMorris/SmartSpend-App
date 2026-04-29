package com.smartspend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.smartspend.data.entity.Goal

class GoalsAdapter(
    private var goals: List<Goal>,
    private val onGoalClick: (Goal) -> Unit
) : RecyclerView.Adapter<GoalsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGoalName: TextView = view.findViewById(R.id.tvGoalName)
        val tvGoalDate: TextView = view.findViewById(R.id.tvGoalDate)
        val progressGoalItem: ProgressBar = view.findViewById(R.id.progressGoalItem)
        val tvGoalCurrentAmount: TextView = view.findViewById(R.id.tvGoalCurrentAmount)
        val tvGoalPercentage: TextView = view.findViewById(R.id.tvGoalPercentage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val goal = goals[position]
        // FIX 1: Use holder.itemView.context — 'context' alone is not in scope here
        val ctx = holder.itemView.context
        val percentage = if (goal.targetAmount > 0)
            ((goal.currentAmount / goal.targetAmount) * 100).toInt()
        else 0

        holder.tvGoalName.text = goal.goalName
        holder.tvGoalDate.text = ctx.getString(R.string.target_date_format, goal.targetDate)
        holder.progressGoalItem.progress = percentage
        // FIX 2: Add to strings.xml: <string name="goal_amount_format">R %.2f / R %.2f</string>
        holder.tvGoalCurrentAmount.text = ctx.getString(
            R.string.goal_amount_format,
            goal.currentAmount,
            goal.targetAmount
        )
        holder.tvGoalPercentage.text = ctx.getString(R.string.percentage_format, percentage)

        holder.itemView.setOnClickListener { onGoalClick(goal) }
    }

    override fun getItemCount() = goals.size

    fun updateGoals(newGoals: List<Goal>) {
        // FIX 3: DiffUtil is from androidx.recyclerview.widget — already imported above
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = goals.size
            override fun getNewListSize(): Int = newGoals.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean =
                goals[oldPos].goalId == newGoals[newPos].goalId
            override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean =
                goals[oldPos] == newGoals[newPos]
        })
        goals = newGoals
        diffResult.dispatchUpdatesTo(this)
    }
}