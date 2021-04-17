package com.example.flowtain.ui.home

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.flowtain.R
import com.example.flowtain.ui.timer.TimerFragment
import com.example.flowtain.util.PrefUtil
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.list_reward.view.*
import kotlin.collections.ArrayList

class RewardsListAdapter(private val context: Context,
                         private val rewardsList: ArrayList<Reward>,
                         private val onNoteListener: OnNoteListener, private val textViewNumPoints: TextView) :
        RecyclerView.Adapter<RewardsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val reward = layoutInflater.inflate(R.layout.list_reward, viewGroup, false)
        return ViewHolder(reward, onNoteListener)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        //Log.i("RewardsListAdapter", "onBind called!")
        viewHolder.bind(rewardsList[position])
    }

    override fun getItemCount() = rewardsList.size

    inner class ViewHolder(itemView: View, onNoteListener: OnNoteListener) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {
        var onNoteListener: OnNoteListener
        var deleteButton: Button
        var claimButton: Button

        init {
            itemView.setOnClickListener(this)
            this.onNoteListener = onNoteListener
            deleteButton = itemView.findViewById(R.id.btn_delete)
            deleteButton.setOnClickListener(this)
            claimButton = itemView.findViewById(R.id.btn_claim_reward)
            claimButton.setOnClickListener(this)
        }

        fun bind(rewardName: Reward) {
            itemView.reward_title.text = rewardName.label
            itemView.reward_cost.text = rewardName.cost.toString() + " Points"
        }

        override fun onClick(v: View?) {
            when (v) {
                deleteButton -> rewardsList.removeAt(adapterPosition)

                claimButton -> {
                    if (rewardsList[adapterPosition].cost <= PrefUtil.getPoints(context)) {
                        val newPoints = PrefUtil.getPoints(context) - rewardsList[adapterPosition].cost
                        PrefUtil.setPoints(newPoints, context)
                        textViewNumPoints.text = "You have ${PrefUtil.getPoints(context)} points!"
                        Toast.makeText(context, "Enjoy your reward!", Toast.LENGTH_SHORT).show()
                        rewardsList.removeAt(adapterPosition)
                    } else {
                        Toast.makeText(context, "Insufficient Points!",
                                Toast.LENGTH_SHORT).show()
                    }
                }
            }
            notifyDataSetChanged()
        }
    }

    interface OnNoteListener {
        fun onNoteClick(position: Int)
    }
}