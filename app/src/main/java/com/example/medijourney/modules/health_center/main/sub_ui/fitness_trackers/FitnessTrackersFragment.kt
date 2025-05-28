package com.example.medijourney.modules.health_center.main.sub_ui.fitness_trackers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.databinding.FragmentFitnessTrackersBinding
import com.example.medijourney.modules.health_center.add_fitness_tracker.AddFitnessTrackerFragment
import com.example.medijourney.modules.health_center.main.MainHealthCenterFragmentDirections
import com.example.medijourney.modules.health_center.main.sub_ui.fitness_trackers.adapter.FitnessTrackersAdapter

class FitnessTrackersFragment : Fragment(), BaseAdapterInterface {

    // Properties
    private lateinit var binding: FragmentFitnessTrackersBinding
    private val viewModel: FitnessTrackersViewModel by viewModels()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFitnessTrackersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewModel.onViewCreated()
        observeUIComponents()
        observeViewModel()
    }

    // Functions
    private fun setupView() {
        binding.recycleView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun observeUIComponents() {
        binding.addButton.setOnClickListener {
            val action = MainHealthCenterFragmentDirections.actionHealthCenterFragmentToAddFitnessTrackerFragment(null)
            action.viewType = AddFitnessTrackerFragment.ADD_FITNESS_TRACKER
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        viewModel.itemModels.observe(viewLifecycleOwner) {
            val adapter = binding.recycleView.adapter as? FitnessTrackersAdapter
            if (adapter == null) {
                val newAdapter = FitnessTrackersAdapter(it).apply {
                    output = this@FitnessTrackersFragment
                }
                binding.recycleView.adapter = newAdapter
            } else {
                adapter.updateItems(it)
            }
            binding.placeholderCarView.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            binding.recycleView.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    // BaseAdapterInterface
    override fun selectedItem(model: BaseItemInterface?) {
        val id = viewModel.getUserFitnessTrackerId(model) ?: return
        val action = MainHealthCenterFragmentDirections.actionHealthCenterFragmentToFitnessTrackerDetailFragment(id, null)
        findNavController().navigate(action)
    }
}