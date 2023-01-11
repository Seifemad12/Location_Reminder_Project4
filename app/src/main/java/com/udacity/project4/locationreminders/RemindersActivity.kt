package com.udacity.project4.locationreminders

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.AuthenticationLiveModel
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import kotlinx.android.synthetic.main.activity_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private val viewModel by viewModels<AuthenticationLiveModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
        checkAuthentication()

    }

    private fun checkAuthentication() {
        viewModel.authenticationState.observe(this, Observer {
            if (it == AuthenticationLiveModel.AuthenticationState.UNAUTHENTICATED){
                startActivity(Intent(this,AuthenticationActivity::class.java))
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
