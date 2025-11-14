package com.example.medijourney.modules.health_center.fitness_tracker_detail

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medijourney.R
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.realm_models.NotificationType
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.common.ui_components.item_decoration.GroupedItemsBorderDecoration
import com.example.medijourney.databinding.FragmentFitnessTrackerDetailBinding
import com.example.medijourney.modules.health_center.add_fitness_tracker.AddFitnessTrackerFragment
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.modules.profile.manage_profile.ManageProfileAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FitnessTrackerDetailFragment : Fragment(), MenuProvider, BaseAdapterInterface {

    // Properties
    private val viewModel: FitnessTrackerDetailViewModel by viewModels()
    private lateinit var binding: FragmentFitnessTrackerDetailBinding
    private val args: FitnessTrackerDetailFragmentArgs by navArgs()
    private val userFitnessTrackerId: String? by lazy { args.userFitnessTrackerId }
    private val notificationType: String? by lazy { args.notificationType }

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFitnessTrackerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        if (!viewModel.didHandledNavigation) {
            handleNavigation()
            viewModel.didHandledNavigation = true
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.edit_remove_action_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_edit -> {
                openAddFitnessTrackerFragment()
            }
            R.id.action_delete -> {
                deleteUserFitnessTracker()
            }
            else -> {
                findNavController().popBackStack()
            }
        }
        return true
    }

    // Functions
    private fun setupView() {
        requireActivity().addMenuProvider(this, viewLifecycleOwner)

        binding.recycleView.layoutManager = LinearLayoutManager(requireContext())
        binding.recycleView.addItemDecoration(GroupedItemsBorderDecoration(requireContext()))
    }

    private fun observeViewModel() {
        viewModel.itemModels.observe(viewLifecycleOwner) { dataList ->
            val adapter = binding.recycleView.adapter as? ManageProfileAdapter
            if (adapter == null) {
                val newAdapter = ManageProfileAdapter(dataList).apply {
                    output = this@FitnessTrackerDetailFragment
                }
                binding.recycleView.adapter = newAdapter
            } else {
                adapter.updateItems(dataList)
            }
        }
    }

    private fun handleNavigation() {
        val notificationType = viewModel.getNotificationType(notificationType) ?: return

        when (notificationType) {
            NotificationType.DAILY_SLEEP_TRACKING_REPORT -> {
                openSleepTrackingReportFragment()
            }
            NotificationType.DAILY_NUTRITION_TRACKING_REPORT -> {
                openNutritionTrackingReportFragment()
            }
            else -> {}
        }
    }

    private fun deleteUserFitnessTracker() {
        IndicatorHandler.show(requireContext())
        viewModel.deleteUserFitnessTracker {
            IndicatorHandler.hide()
            findNavController().popBackStack()
        }
    }

    // BaseAdapterInterface
    override fun selectedItem(model: BaseItemInterface?) {
        val itemTag = viewModel.getItemTag(model) ?: return
        if (viewModel.checkEnableActivity(model)) {
            handleNavigation(itemTag)
        } else {
            Toast.makeText(requireContext(),
                getString(R.string.enable_tracking_activity_alert), Toast.LENGTH_LONG).show()
        }
    }

    private fun handleNavigation(itemTag: String) {
        when (itemTag) {
            "sleep_tracking" -> {
                openSleepTrackingReportFragment()
            }
            "nutrition_tracking" -> {
                openNutritionTrackingReportFragment()
            }
            "exercise_tracking" -> {
                openExerciseTrackingReportFragment()
            }
            else -> {}
        }
    }

    // Router
    private fun openAddFitnessTrackerFragment() {
        val action = FitnessTrackerDetailFragmentDirections.actionFitnessTrackerDetailFragmentToAddFitnessTrackerFragment(userFitnessTrackerId)
        action.viewType = AddFitnessTrackerFragment.EDIT_FITNESS_TRACKER
        findNavController().navigate(action)
    }

    private fun openSleepTrackingReportFragment() {
        val action = FitnessTrackerDetailFragmentDirections.actionFitnessTrackerDetailFragmentToSleepTrackingReportFragment(userFitnessTrackerId)
        findNavController().navigate(action)
    }

    private fun openNutritionTrackingReportFragment() {
        val action = FitnessTrackerDetailFragmentDirections.actionFitnessTrackerDetailFragmentToNutritionTrackingReportFragment(userFitnessTrackerId)
        findNavController().navigate(action)
    }

    private fun openExerciseTrackingReportFragment() {
        val action = FitnessTrackerDetailFragmentDirections.actionFitnessTrackerDetailFragmentToExerciseTrackingReportFragment(userFitnessTrackerId)
        findNavController().navigate(action)
    }
}