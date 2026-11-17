package com.sync2pc.app.sync

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest

object WifiHelper {
    /**
     * Netz-Fingerabdruck des aktiven WLANs für «Bekannte Netze» (KnownNets): ohne Zusatzberechtigung aus den LinkProperties
     * (ACCESS_NETWORK_STATE). Null = kein WLAN oder nichts Brauchbares lesbar.
     */
    fun currentNetFingerprint(context: Context): String? {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return null
        val net = cm.activeNetwork ?: return null
        val caps = cm.getNetworkCapabilities(net) ?: return null
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null
        val lp = cm.getLinkProperties(net) ?: return null
        val v4 = lp.routes.filter { it.isDefaultRoute && it.gateway is java.net.Inet4Address }.mapNotNull { it.gateway?.hostAddress }
        val gateways = v4.ifEmpty { lp.routes.filter { it.isDefaultRoute }.mapNotNull { it.gateway?.hostAddress } }
        val prefix = lp.linkAddresses.firstOrNull { it.address is java.net.Inet4Address }
            ?.let { KnownNets.ipv4Network(it.address.address, it.prefixLength) }
        return KnownNets.fingerprint(gateways, prefix, lp.domains)
    }

    fun isOnWifi(context: Context): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return false) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * Mobile Daten bzw. VPN über Mobilfunk (0.11.5, DEC-126): aktives Netz mit Internet, aber kein WLAN.
     * Ein VPN über ein WLAN zählt als WLAN.
     */
    fun isOnMobileOrVpn(context: Context): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return false) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            !caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * Roaming? Das aktive Netz hat die Fähigkeit NOT_ROAMING nicht (B7, SYNC-20261114-338). Bei einem VPN gilt die Fähigkeit des
     * VPN-Netzes, die Android aus den zugrunde liegenden Netzen ableitet. Unbekannt = kein Roaming.
     */
    fun isRoaming(context: Context): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return false) ?: return false
        return !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING)
    }

    /**
     * Darf jetzt automatisch synchronisiert werden? WLAN immer (Prüfung der PC-Kennung durch den Aufrufer),
     * mobile Daten/VPN nur mit dem Schalter [com.sync2pc.app.data.GlobalSettings.syncOffWifi] und nicht im Roaming.
     */
    fun onAllowedNetwork(context: Context): Boolean =
        NetworkRules.onAllowedNetwork(
            isOnWifi(context), isOnMobileOrVpn(context), com.sync2pc.app.data.GlobalSettings(context).syncOffWifi, isRoaming(context)
        )

    /** Nur mobile Daten/VPN, der Schalter ist an und kein Roaming (kein Netz-Fingerabdruck) */
    fun offWifiSyncActive(context: Context): Boolean =
        NetworkRules.offWifiSyncActive(isOnMobileOrVpn(context), com.sync2pc.app.data.GlobalSettings(context).syncOffWifi, isRoaming(context))

    /** Wird gerade nur wegen Roaming nicht über mobile Daten/VPN synchronisiert? */
    fun skippedForRoaming(context: Context): Boolean =
        NetworkRules.skippedForRoaming(isOnMobileOrVpn(context), com.sync2pc.app.data.GlobalSettings(context).syncOffWifi, isRoaming(context))

private fun pendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context, 0,
            Intent(context, WifiNetworkReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

    /**
     * Registriert beim System einen Callback für WLAN-Verbindungen.
     * Die App muss dafür NICHT laufen – das System weckt sie per Broadcast (akkuschonend).
     */
    fun registerWifiCallback(context: Context) {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return
        val pi = pendingIntent(context)
        try { cm.unregisterNetworkCallback(pi) } catch (ignored: Exception) {}
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        try { cm.registerNetworkCallback(request, pi) } catch (ignored: Exception) {}
    }

    fun unregisterWifiCallback(context: Context) {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return
        try { cm.unregisterNetworkCallback(pendingIntent(context)) } catch (ignored: Exception) {}
    }
}
