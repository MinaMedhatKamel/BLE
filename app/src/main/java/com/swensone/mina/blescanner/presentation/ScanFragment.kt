package com.swensone.mina.blescanner.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.swensone.mina.blescanner.data.AdvertisedDeviceItem
import com.swensone.mina.blescanner.BleViewModel
import com.swensone.mina.blescanner.databinding.FragmentScanBinding
import kotlinx.coroutines.launch


class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val vm: BleViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)

        binding.scaneToggle.setOnClickListener {
            vm.toggleScan()

        }
        lifecycleScope.launchWhenStarted {
            vm.isScanRunning.collect {
                binding.scaneToggle.text = if (it) {
                    binding.progressBarCyclic.isVisible = true
                    "Stop scanning"
                } else {
                    binding.progressBarCyclic.isVisible = false
                    "Start Scanning"
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemAdapter = ItemAdapter<AdvertisedDeviceItem>()
        val fastAdapter = FastAdapter.with(itemAdapter)

        binding.recyclerView.adapter = fastAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(binding.root.context)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    vm.advertisements.collect {
                        itemAdapter.setNewList(it.map { item ->
                            AdvertisedDeviceItem(item, vm)
                        })
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}