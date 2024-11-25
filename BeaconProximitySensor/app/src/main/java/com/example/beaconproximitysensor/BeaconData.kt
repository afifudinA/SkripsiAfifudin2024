package com.example.beaconproximitysensor

import com.google.gson.annotations.SerializedName

data class BeaconData(

	@field:SerializedName("payload")
	val payload: Payload? = null,

	@field:SerializedName("operation")
	val operation: String? = null
)

data class Payload(

	@field:SerializedName("proximityUUID")
	val proximityUUID: String? = null,

	@field:SerializedName("rssi")
	val rssi: Int? = null,

	@field:SerializedName("txPower")
	val txPower: Int? = null,

	@field:SerializedName("distance")
	val distance: Any? = null,

	@field:SerializedName("isRead")
	val isRead: Int? = null,

	@field:SerializedName("bleAddress")
	val bleAddress: String? = null,

	@field:SerializedName("deviceID")
	val deviceID: String? = null,

	@field:SerializedName("timestamp")
	val timestamp: Long? = null
)
