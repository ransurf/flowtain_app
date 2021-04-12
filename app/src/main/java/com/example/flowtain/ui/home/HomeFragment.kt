package com.example.flowtain.ui.home

import android.content.Intent
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
import com.example.flowtain.ui.settings.SettingsActivity
import com.example.flowtain.ui.settings.SettingsActivityFragment
import com.example.flowtain.ui.timer.TimerFragment.Companion.points
import com.example.flowtain.util.PrefUtil
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(), RewardsListAdapter.OnNoteListener {

    var rewardsList = ArrayList<Reward>()
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var textNumPoints: TextView
    private lateinit var rewardsAdapter: RewardsListAdapter
    private lateinit var rewardsListView: RecyclerView
    private lateinit var inputTitle: EditText
    private lateinit var btnAddReward: Button

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        rewardsAdapter = RewardsListAdapter(this.requireActivity(), rewardsList, this)
        btnAddReward = root.findViewById(R.id.btn_create_reward)
        rewardsListView = root.findViewById(R.id.rewards_list_view)
        inputTitle = root.findViewById(R.id.input_reward_title)
        inputTitle = root.findViewById(R.id.input_points_cost)
        textNumPoints = root.findViewById(R.id.num_points)
        rewardsListView.adapter = RewardsListAdapter(this.requireActivity(), rewardsList, this)
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
                input_reward_title.text.clear()
                input_points_cost.text.clear()
            }
        }

        Log.i("HomeFragment", "$points")
        textNumPoints.text = "You have ${PrefUtil.getNumPoints(this.requireActivity())} points!"
        rewardsList.add(Reward("Play VALORANT", 50))
        rewardsList.add(Reward("Play VALORANT", 50))
        rewardsList.add(Reward("Play VALORANT", 50))

    }

    override fun onPause() {
        super.onPause()
        PrefUtil.setNumPoints(points, this.requireActivity())
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) { //inflates menu
        super.onCreateOptionsMenu(menu, menuInflater)
        menuInflater.inflate(R.menu.menu_points_popup, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.points_settings -> {
                SettingsActivityFragment.preference = "points"
                val intent = Intent(this.requireActivity(), SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNoteClick(position: Int) {
        //rewardsList.removeAt(0)
        //rewardsAdapter.notifyDataSetChanged()
    }
}