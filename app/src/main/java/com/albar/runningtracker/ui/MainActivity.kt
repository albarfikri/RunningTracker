package com.albar.runningtracker.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.albar.runningtracker.R
import com.albar.runningtracker.databinding.ActivityMainBinding
import com.albar.runningtracker.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var _navHost: View
    private val navHost get() = _navHost


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        _navHost = findViewById(R.id.navHostFragment)

        // if activity has been destroyed and we want to enter from pending intent to our app
        navigateToTrackingFragmentIfNeeded(intent)

        setSupportActionBar(binding.toolbar)

        binding.apply {
            bottomNavigationView.setupWithNavController(navHost.findNavController())
            navHost.findNavController()
                .addOnDestinationChangedListener { _, destination, _ ->
                    when (destination.id) {
                        R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment ->
                            bottomNavigationView.visibility = View.VISIBLE
                        else -> bottomNavigationView.visibility = View.GONE
                    }
                }
        }
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        // check ACTION_SHOW_TRACKING_FRAGMENT in MainActivity
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            navHost.findNavController().navigate(R.id.action_global_trackingFragment)
        }
    }

    // if activity isn't closed yet and we want to enter the app
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }
}