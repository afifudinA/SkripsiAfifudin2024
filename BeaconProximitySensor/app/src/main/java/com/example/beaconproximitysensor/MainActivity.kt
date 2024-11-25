package com.example.beaconproximitysensor

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }

    private var isScanning = false
    private lateinit var beaconDataTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toggleScannerButton = findViewById<Button>(R.id.button_toggle_scanner)
        beaconDataTextView = findViewById(R.id.beacon_data_text_view)

        // Register to receive broadcast messages from the BeaconService
        val filter = IntentFilter("com.example.beaconproximitysensor.BEACON_DATA")
        registerReceiver(beaconDataReceiver, filter)

        toggleScannerButton.setOnClickListener {
            if (isScanning) {
                stopService(Intent(this, BeaconService::class.java))
                toggleScannerButton.text = "Start Scanning"
            } else {
                if (arePermissionsGranted()) {
                    startScanning()
                    toggleScannerButton.text = "Stop Scanning"
                } else {
                    requestPermissions()
                }
            }
            isScanning = !isScanning
        }

        // Automatically start scanning if permissions are granted
        if (arePermissionsGranted()) {
            startScanning()
            toggleScannerButton.text = "Stop Scanning"
        } else {
            requestPermissions()
        }
    }

    // Function to start scanning by starting the BeaconService
    private fun startScanning() {
        startService(Intent(this, BeaconService::class.java))
        Log.d("MainActivity", "Service started automatically")
        isScanning = true
    }

    // BroadcastReceiver that updates the TextView with BLE data
    private val beaconDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val beaconData = intent.getStringExtra("beacon_data") ?: "No Data"
            beaconDataTextView.text = "Beacon Data: $beaconData"
        }
    }

    private fun arePermissionsGranted(): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.INTERNET
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        } && (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager())
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.INTERNET
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            showManageExternalStorageDialog()
        } else {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showManageExternalStorageDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs permission to manage external storage to function properly. Please allow this permission in settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions =
                permissions.indices.filter { grantResults[it] != PackageManager.PERMISSION_GRANTED }
                    .map { permissions[it] }

            if (deniedPermissions.isNotEmpty()) {
                Log.d("MainActivity", "Permissions denied: $deniedPermissions")
            } else {
                startScanning()
                Log.d("MainActivity", "Service started after permissions granted")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(beaconDataReceiver)
    }
}
