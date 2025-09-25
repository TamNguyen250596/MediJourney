package com.example.medijourney.modules.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.realm_models.NotificationType
import com.example.medijourney.common.models.realm_models.UserFitnessTracker
import com.example.medijourney.common.models.realm_models.UserNotification
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.common.ui_components.item_touch_helper.swipe_delete.SwipeToDeleteCallback
import com.example.medijourney.common.ui_components.item_touch_helper.swipe_delete.SwipeToDeleteCallbackInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.databinding.FragmentNotificationBinding
import com.example.medijourney.modules.base.main.MainActivity
import com.example.medijourney.modules.health_center.add_fitness_tracker.AddFitnessTrackerFragment
import com.example.medijourney.modules.notification.adapter.NotificationAdapter
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.ext.isValid

@AndroidEntryPoint
class NotificationFragment : Fragment(), MenuProvider, BaseAdapterInterface,
    SwipeToDeleteCallbackInterface {

    // Properties
    private lateinit var binding: FragmentNotificationBinding
    private val viewModel: NotificationViewModel by viewModels()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.delete_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        IndicatorHandler.show(requireContext())
        viewModel.deleteAllNotifications {
            IndicatorHandler.hide()
        }
        return true
    }

    // Functions
    private fun setupView() {
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
        binding.recycleView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recycleView.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                val position = binding.recycleView.getChildAdapterPosition(view)
                viewModel.observeUserNotifications(position)
            }

            override fun onChildViewDetachedFromWindow(view: View) {}
        })

        val dividerItemDecoration = MaterialDividerItemDecoration(requireContext(), MaterialDividerItemDecoration.VERTICAL)
        dividerItemDecoration.isLastItemDecorated = false
        dividerItemDecoration.dividerColor = getColor(resources, R.color.deep_turquoise_blue_color, null)
        binding.recycleView.addItemDecoration(dividerItemDecoration)

        val swipeController = SwipeToDeleteCallback(requireContext(), 0f)
        swipeController.output = this
        val itemTouchHelper = ItemTouchHelper(swipeController)
        itemTouchHelper.attachToRecyclerView(binding.recycleView)
    }

    private fun observeViewModel() {
        viewModel.models.observe(viewLifecycleOwner) {
            val adapter = binding.recycleView.adapter as? NotificationAdapter
            if (adapter == null) {
                val newAdapter = NotificationAdapter(it).apply {
                    output = this@NotificationFragment
                }
                binding.recycleView.adapter = newAdapter
            } else {
                adapter.updateItems(it)
            }
        }
    }

    // BaseAdapterInterface
    override fun selectedItem(model: BaseItemInterface?) {
        viewModel.readNotification(model)

        val notification = model?.data as? UserNotification ?: return
        when (val notificationType = notification.getNotificationType()) {
            NotificationType.ADD_FITNESS_TRACKER_SUCCESS -> {
                openFitnessTrackerDetailFragment(notification)
            }
            NotificationType.ADD_FITNESS_TRACKER_FAILED -> {
                handleAddFitnessTrackerFailed(notification)
            }
            NotificationType.DAILY_SLEEP_TRACKING_REPORT,
            NotificationType.DAILY_NUTRITION_TRACKING_REPORT -> {
                openFitnessTrackerDetailFragment(notification, notificationType.name)
            }
            NotificationType.RECEIVE_NEW_MESSAGE -> {
                openMessageFragment(notification)
            }
            else -> {}
        }
    }

    private fun openFitnessTrackerDetailFragment(notification: UserNotification, notificationType: String? = null) {
        if (!notification.isValid()) return
        if (notification.relatedObjectType == UserFitnessTracker::class.simpleName) {
            notification.relatedObjectId?.let {
                val action = NotificationFragmentDirections.actionNotificationFragmentToFitnessTrackerDetailFragment(it, notificationType)
                findNavController().navigate(action)
            }
        }
    }

    private fun handleAddFitnessTrackerFailed(notification: UserNotification) {
        if (!notification.isValid()) return
        if (notification.relatedObjectType == UserFitnessTracker::class.simpleName) {
            notification.relatedObjectId?.let {
                val action = NotificationFragmentDirections.actionNotificationFragmentToAddFitnessTrackerFragment(it)
                action.viewType = AddFitnessTrackerFragment.ADD_FITNESS_TRACKER
                findNavController().navigate(action)
            }
        }
    }

    private fun openMessageFragment(notification: UserNotification) {
        val messageId = viewModel.getUserMessageId(notification) ?: return
        val activity = requireActivity() as? MainActivity ?: return
        activity.switchToTab(R.id.navigationChat, mapOf(Constants.HIGHLIGHT_USER_MESSAGE_ID to messageId))
    }

    // SwipeToDeleteCallbackInterface
    override fun onSwiped(position: Int, direction: Int) {
        IndicatorHandler.show(requireContext())
        viewModel.deleteNotification(position) {
            IndicatorHandler.hide()
        }
    }
}