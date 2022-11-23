package com.swensone.mina.blescanner.data

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.swensone.mina.blescanner.BleViewModel

import com.swensone.mina.blescanner.R
import com.swensone.mina.blescanner.databinding.RecyclerviewScanItemBinding

/**
 * wrapping the AdvertisementWrapper
 */
class AdvertisedDeviceItem constructor(
    private val item: AdvertisementWrapper,
    val viewModel: BleViewModel
) : AbstractBindingItem<RecyclerviewScanItemBinding>() {
    override var identifier: Long
        get() = java.lang.Long.parseLong(item.address.replace(":", ""), 16)
        set(_) {}

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): RecyclerviewScanItemBinding {
        return RecyclerviewScanItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: RecyclerviewScanItemBinding, payloads: List<Any>) {
        binding.obj = item
        binding.viewModel = viewModel
    }

    override val type: Int
        get() = R.id.provisionable_device_advertisement_item

}
