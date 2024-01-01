package com.example.medijourney.modules.health_center.add_fitness_tracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.medijourney.R
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.common.ui_components.fragments.media_selection.MediaSelectionViewModel
import com.example.medijourney.common.ui_components.recycle_view_adapter.check_box.CheckBoxAdapter
import com.example.medijourney.common.ui_components.recycle_view_adapter.check_box.CheckBoxAdapterInterface
import com.example.medijourney.databinding.FragmentAddFitnessTrackerBinding

class AddFitnessTrackerFragment : Fragment(), CheckBoxAdapterInterface {

    // Properties
    private val viewModel: AddFitnessTrackerViewModel by viewModels()
    private val mediaSelectionViewModel: MediaSelectionViewModel by activityViewModels()
    private lateinit var binding: FragmentAddFitnessTrackerBinding
    private val args: AddFitnessTrackerFragmentArgs by navArgs()
    private val userFitnessTrackerId: String? by lazy { args.userFitnessTrackerId }
    private val viewType: String? by lazy { args.viewType }

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddFitnessTrackerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewModel.onViewCreated(userFitnessTrackerId)
        observeUIComponents()
        observeViewModel()
    }

    // Functions
    private fun setupView() {
        (activity as? AppCompatActivity)?.supportActionBar?.title = if (viewType == EDIT_FITNESS_TRACKER)
            getString(R.string.edit_fitness_tracker) else getString(R.string.add_fitness_tracker)
        binding.textInputEditText.isEnabled = viewType == ADD_FITNESS_TRACKER
        binding.recycleView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun observeUIComponents() {
        binding.trackerImageViewButton.setOnClickListener {
            findNavController().navigate(R.id.action_addFitnessTrackerFragment_to_mediaSelectionFragment)
        }
        binding.addButton.setOnClickListener {
            IndicatorHandler.show(requireContext())
            viewModel.handleToAddFitnessTracker(binding.textInputEditText.text.toString()) {
                IndicatorHandler.hide()
                findNavController().popBackStack()
            }
        }
    }

    private fun observeViewModel() {
        mediaSelectionViewModel.uri.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            Glide.with(requireContext()).load(it).into(binding.trackerImageView)
        }
        viewModel.trackerImage.observe(viewLifecycleOwner) {
            Glide.with(requireContext()).load(it).into(binding.trackerImageView)
        }
        viewModel.trackerDeviceName.observe(viewLifecycleOwner) {
            binding.textInputEditText.setText(it)
        }
        viewModel.itemModels.observe(viewLifecycleOwner) {
            val adapter = binding.recycleView.adapter as? CheckBoxAdapter
            if (adapter == null) {
                val newAdapter = CheckBoxAdapter(it).apply {
                    output = this@AddFitnessTrackerFragment
                }
                binding.recycleView.adapter = newAdapter
            } else {
                adapter.updateItems(it)
            }
        }
    }

    // CheckBoxAdapterInterface
    override fun selectedItem(model: BaseItemInterface?) {}

    override fun selectedCheckBox(model: BaseItemInterface?, isChecked: Boolean) {
        viewModel.handleTrackerActivities(model, isChecked)
    }

    companion object {
        const val EDIT_FITNESS_TRACKER = "EDIT_FITNESS_TRACKER"
        const val ADD_FITNESS_TRACKER = "ADD_FITNESS_TRACKER"
    }
}