package com.example.hikermanagementapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup navigation after ensuring fragment container is ready
        // Using post() ensures the toolbar is fully laid out before setup
        findViewById<MaterialToolbar>(R.id.toolbar).post {
            setupNavigation()
        }
    }


    private fun setupNavigation() {
        // Find the NavHostFragment from the layout
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

        navHostFragment?.let { fragment ->
            // Get the NavController which manages fragment navigation
            val navController = fragment.navController

            // Find and configure the toolbar
            val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)

            // Define which destinations are "top level" (no back button shown)
            val appBarConfiguration = AppBarConfiguration(setOf(R.id.hikeListFragment))

            // automatically updates title and shows back button
            toolbar.setupWithNavController(navController, appBarConfiguration)

            val backClick: (android.view.View) -> Unit = { onBackPressedDispatcher.onBackPressed() }
            toolbar.setNavigationOnClickListener(backClick)

            navController.addOnDestinationChangedListener { _, _, _ ->
                toolbar.setNavigationOnClickListener(backClick)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {

        if (onBackPressedDispatcher.hasEnabledCallbacks()) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }

        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment)?.navController
        return (navController?.navigateUp() == true) || super.onSupportNavigateUp()
    }
}
