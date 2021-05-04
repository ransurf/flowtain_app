package com.example.flowtain.ui.gallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.flowtain.R
import com.example.flowtain.util.PrefUtil
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.android.synthetic.main.fragment_home.textViewNumPoints


class GalleryFragment : Fragment(), View.OnClickListener {

    private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var inputLength: EditText

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        galleryViewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_gallery, container, false)
        val textView: TextView = root.findViewById(R.id.timer_length_label)
        inputLength = root.findViewById(R.id.input_timer_length)
        galleryViewModel.points.observe(viewLifecycleOwner, Observer {
            textViewNumPoints.text = it.toString() })
        return root
    }

    override fun onResume() {
        super.onResume()
        saveSettings.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            saveSettings -> {
                if (inputLength.text.toString() != "") {
                    PrefUtil.setTimerLength(inputLength.text.toString().toInt(), requireActivity())
                    //Log.i("GalleryFragment", "${PrefUtil.getTimerLength(requireActivity())}")
                } else {
                    PrefUtil.setTimerLength(30, this.requireActivity())
                }
            }
        }

    }
}
