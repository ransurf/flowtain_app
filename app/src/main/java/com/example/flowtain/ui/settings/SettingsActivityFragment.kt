package com.example.flowtain.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.flowtain.R

class SettingsActivityFragment : PreferenceFragmentCompat() {
    companion object {
        var preference = ""
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        when (preference) {
            "points" -> {setPreferencesFromResource(R.xml.points_settings, rootKey)}
            "timer" -> {setPreferencesFromResource(R.xml.timer_settings, rootKey)}
        }
    }
}