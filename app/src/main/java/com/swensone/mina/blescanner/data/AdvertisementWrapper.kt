package com.swensone.mina.blescanner.data

import com.juul.kable.Advertisement

data class AdvertisementWrapper constructor(internal val adv: Advertisement) {
    val name: String? = adv.name
    val rssi = adv.rssi
    val rssiDisplay = "$rssi dBm"
    val address = adv.address

    val nameOrAddress: String
        get() = if (name.isNullOrBlank()) address else name
}
