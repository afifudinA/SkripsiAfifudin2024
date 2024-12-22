package com.example.beaconproximitysensor

import android.annotation.SuppressLint
import android.provider.Settings.Secure
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region
import retrofit2.Call
import java.io.File
import java.io.FileWriter
import java.io.IOException
import android.os.Handler
import android.os.Looper

class BeaconService : Service() {

    private lateinit var beaconManager: BeaconManager
    private lateinit var region: Region
    private val handler = Handler(Looper.getMainLooper())   // Handler to manage cooldown


    override fun onCreate() {
        super.onCreate()
        initializeBeaconManager()
        startForeground(1, createNotification())
    }

    private fun startScanning() {
        beaconManager.startRangingBeacons(region)
        Log.d("BeaconService", "Scanning started")
    }

    // Function to stop scanning
    private fun stopScanning() {
        beaconManager.stopRangingBeacons(region)
        Log.d("BeaconService", "Scanning stopped for cooldown")
    }

    private fun createNotification(): Notification {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "beacon_service_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Beacon Service Channel", NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId).setContentTitle("Beacon Service")
            .setContentText("Running in the background")
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            ).build()
    }

    @SuppressLint("HardwareIds")
    private fun initializeBeaconManager() {
        beaconManager = BeaconManager.
        getInstanceForApplication(this)
        val iBeaconLayout =
            "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"
        beaconManager.beaconParsers.add(BeaconParser().
        setBeaconLayout(iBeaconLayout))

        beaconManager.
        enableForegroundServiceScanning(createNotification(), 2)

        // Configure the scanning periods
        beaconManager.foregroundBetweenScanPeriod = 0L
        beaconManager.foregroundScanPeriod = 1000L
        beaconManager.backgroundBetweenScanPeriod = 0L
        beaconManager.backgroundScanPeriod = 1000L
        region = Region("all-beacons-region", null, null, null)
        startScanning()

        // Auto-bind to the service

        beaconManager.addRangeNotifier { beacons, _ ->
            if (beacons.isNotEmpty()) {
                // Get the device's unique ID (UUID)
                val deviceID =
                    Secure.getString(applicationContext.contentResolver, Secure.ANDROID_ID)

                for (beacon in beacons) {
                    // Check if the beacon's Bluetooth address matches the desired IDs
                    if (beacon.bluetoothAddress == "FB:FD:2A:A8:E2:BE" ||
                        beacon.bluetoothAddress == "F2:AB:73:19:59:79" ||
                        beacon.bluetoothAddress == "51:00:24:08:05:32") {
                        val timestamp = getCurrentUnixTimestamp()

                        // Create the payload object
                        val beaconPayload = BeaconData(
                            operation = "post-beacon", payload = Payload(
                                timestamp = timestamp,
                                deviceID = deviceID, // Use the device's UUID
                                bleAddress = beacon.bluetoothAddress,
                                distance = beacon.distance,
                                isRead = 0,
                                proximityUUID = beacon.id1.toString(),
                                rssi = beacon.rssi,
                                txPower = beacon.txPower
                            )
                        )

                        // Logging the data class object
                        Log.d("BeaconPayload", beaconPayload.toString())

                        // Save to CSV or other handling
                        saveToCsv(
                            beacon.id1.toString(),
                            beacon.id2.toString(),
                            beacon.id3.toString(),
                            beacon.rssi.toString(),
                            timestamp
                        )

                        // Send the object directly if your logic allows
                        sendBroadcast(beaconPayload.payload.toString())
                        sendBeaconDataToServer(beaconPayload)

                        // Stop scanning and start scooldown period of 10 seconds
                        stopScanning()

                        handler.postDelayed({
                            startScanning()  // Resume scanning after 10 seconds
                        }, 10000) // 10 seconds cooldown
                    }
                }
            }
        }
    }

    private fun getCurrentUnixTimestamp(): Long {
        return System.currentTimeMillis()
    }

    private fun saveToCsv(
        uuid: String, major: String, minor: String, rssi: String, timestamp: Long
    ) {
        val csvHeader = "UUID, Major, Minor, RSSI, Timestamp\n"
        val csvData = "$uuid, $major, $minor, $rssi, $timestamp\n"
        val fileName = "beacon_data.csv"

        // Check if external storage is available
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "BeaconData"
            )

            // Create the directory if it doesn't exist
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, fileName)
            var fileWriter: FileWriter? = null

            try {
                fileWriter = FileWriter(file, true)
                // Write header only if the file is newly created
                if (!file.exists()) {
                    fileWriter.append(csvHeader)
                }
                fileWriter.append(csvData)
                Log.d("CSV", "CSV data written to external storage")
                Log.d("CSV", "File saved at: ${file.absolutePath}")
            } catch (e: IOException) {
                Log.e("CSV", "Error writing CSV", e)
            } finally {
                try {
                    fileWriter?.flush()
                    fileWriter?.close()
                } catch (e: IOException) {
                    Log.e("CSV", "Error closing FileWriter", e)
                }
            }
        } else {
            Log.e("CSV", "External storage is not available")
        }
    }

    private fun sendBeaconDataToServer(beaconInfo: BeaconData) {
        val call = ApiClient.apiService.sendBeaconData(beaconInfo)
        call.enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("BeaconService", "Data sent successfully")
                } else {
                    Log.e("BeaconService", "Failed to send data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("BeaconService", "Error sending data", t)
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconManager.removeAllRangeNotifiers()
        beaconManager.stopRangingBeacons(region)
    }

    private fun sendBroadcast(beaconInfo: String) {
        val intent = Intent("com.example.beaconproximitysensor.BEACON_DATA")
        intent.putExtra("beacon_data", beaconInfo)
        sendBroadcast(intent)
    }
}