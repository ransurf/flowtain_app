package com.example.flowtain.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.flowtain.R
import com.example.flowtain.TinyDB
import com.example.flowtain.util.PrefUtil
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(), RewardsListAdapter.OnNoteListener {
    var rewardsList = ArrayList<Reward>()
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var homeViewModelFactory: HomeViewModelFactory
    private lateinit var textNumPoints: TextView
    private lateinit var rewardsAdapter: RewardsListAdapter
    private lateinit var rewardsListView: RecyclerView
    private lateinit var inputTitle: EditText
    private lateinit var btnAddReward: Button
    private lateinit var tinyDB : TinyDB
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val points = PrefUtil.getPoints(requireActivity())
        homeViewModelFactory = HomeViewModelFactory(points)
        homeViewModel = ViewModelProvider(this, homeViewModelFactory)
                .get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        btnAddReward = root.findViewById(R.id.btn_create_reward)
        rewardsListView = root.findViewById(R.id.rewards_list_view)
        inputTitle = root.findViewById(R.id.input_reward_title)
        inputTitle = root.findViewById(R.id.input_points_cost)
        textNumPoints = root.findViewById(R.id.textViewNumPoints)
        tinyDB = TinyDB(requireActivity())

        Log.i("HomeFragment", "$rewardsList")
        rewardsAdapter = RewardsListAdapter(this.requireActivity(), rewardsList, this, textNumPoints)
        rewardsListView.adapter = RewardsListAdapter(this.requireActivity(), rewardsList, this, textNumPoints)

        setHasOptionsMenu(true)
        return root
    }

    override fun onResume(){
        super.onResume()

        btnAddReward.setOnClickListener {

            val rewardTitle =
                    if (input_reward_title?.text.toString() != null) input_reward_title.text.toString()
                    else  {
                        Toast.makeText(context, "Invalid Name/Cost!",
                                Toast.LENGTH_SHORT).show()
                        ""
                    }

            val rewardCost =
                    if ( (input_points_cost.text.toString() != "") &&
                            input_points_cost?.text?.toString()?.toInt()!! > 0)
                                input_points_cost.text.toString().toInt()
                    else {
                        Toast.makeText(context, "Invalid Name/Cost!",
                                Toast.LENGTH_SHORT).show()
                        0
                    }
            val newReward = Reward(rewardTitle, rewardCost)

            if (rewardTitle != "" && rewardCost >= 0) {
                rewardsList.add(newReward)
                rewardsAdapter.notifyItemInserted(rewardsList.size - 1)
            }
            Log.i("HomeFragment", "$rewardsList")
            input_reward_title.text.clear()
            input_points_cost.text.clear()
        }
        //retrieves previous rewards list from sharedpreferences
        var rewardsListTemp: java.util.ArrayList<Any>? =
                tinyDB.getListObject("rewards", Reward::class.java)
        if (rewardsListTemp != null) {
            for (reward in rewardsListTemp) {
                rewardsList.add(reward as Reward)
            }
        }
        rewardsAdapter.notifyDataSetChanged()

        updatePointsDisplay()
        homeViewModel.points.observe(this, { points ->
            textNumPoints.text = "You have $points points!"
        })

    }

    fun updatePointsDisplay() {
        textNumPoints.text = "You have ${PrefUtil.getPoints(requireActivity())} points!"
    }

    override fun onPause() {
        super.onPause()
        var rewardsObjects = ArrayList<Object>()
        for (reward in rewardsList) {
            Log.i("HomeFragment", reward.toString())
            rewardsObjects.add((reward as Object))
        }
        tinyDB.putListObject("rewards", rewardsObjects)
        PrefUtil.setPoints(PrefUtil.getPoints(requireActivity()), requireActivity())
    }

    override fun onNoteClick(position: Int) {
    }
}