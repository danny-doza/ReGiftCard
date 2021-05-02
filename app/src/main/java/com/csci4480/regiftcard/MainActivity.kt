package com.csci4480.regiftcard

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.csci4480.regiftcard.ui.LoginActivity
import com.csci4480.regiftcard.ui.ProfileActivity
import com.csci4480.regiftcard.utils.FirebaseMethods
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), SensorEventListener {
    companion object {
        private const val LOG_TAG = "448.MainActivity"
    }

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var sensorManager: SensorManager
    private lateinit var light : Sensor



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(LOG_TAG, "onCreate() called")
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.top_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(LOG_TAG, "onOptionsItemSelected() called")
        return when(item.itemId) {
            R.id.profile_page -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.log_out -> {
                mAuth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d(LOG_TAG, "sensor changed")


        if (event != null) {
            if( event.sensor.getType() == Sensor.TYPE_LIGHT) {
                Log.d(LOG_TAG, "got Light sensor data of ${event.values[0]}")
                var light = event.values[0];
                if (light < 25000)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                if (light > 25000)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, light,
                SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}