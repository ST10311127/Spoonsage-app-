package com.spoonsage.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NetworkMonitor(context: Context, private val onReconnected: () -> Unit) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOnline = MutableLiveData(checkNow())
    val isOnline: LiveData<Boolean> = _isOnline

    fun isConnected(): Boolean = _isOnline.value == true

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val wasOffline = _isOnline.value != true
            _isOnline.postValue(true)
            Log.d(TAG, "Network available (wasOffline=$wasOffline)")
            if (wasOffline) onReconnected()
        }

        override fun onLost(network: Network) {
            Log.d(TAG, "Network lost - app is now in offline mode")
            _isOnline.postValue(checkNow())
        }
    }

    fun start() {
        try {
            connectivityManager.registerDefaultNetworkCallback(callback)
        } catch (e: Exception) {
            Log.e(TAG, "Could not register network callback", e)
        }
    }

    private fun checkNow(): Boolean {
        val caps = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    companion object {
        private const val TAG = "NetworkMonitor"
    }
}
