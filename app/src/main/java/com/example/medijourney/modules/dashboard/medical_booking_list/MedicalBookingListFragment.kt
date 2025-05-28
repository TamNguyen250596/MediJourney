package com.example.medijourney.modules.dashboard.medical_booking_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.medijourney.R
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.composes.ConversationItem
import com.example.medijourney.databinding.FragmentMedicalBookingListBinding

class MedicalBookingListFragment : Fragment(), MenuProvider {

    // Properties
    private val viewModel: MedicalBookingListViewModel by viewModels()
    private lateinit var binding: FragmentMedicalBookingListBinding
    private val args: MedicalBookingListFragmentArgs by navArgs()
    private val showAddMedicalBooking: Boolean by lazy { args.showAddMedicalBooking }

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMedicalBookingListBinding.inflate(inflater, container, false)
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MedicalBookingListScreen(
                    viewModel = viewModel,
                    onClickItem = {
                        val doctorAppointmentId = viewModel.getDoctorAppointmentId(it) ?: return@MedicalBookingListScreen
                        openAddMedicalBookingFragment(doctorAppointmentId)
                    }
                )
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        if (!viewModel.didHandledNavigation) {
            handleNavigation()
            viewModel.didHandledNavigation = true
        }
        viewModel.onViewCreated()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.add_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId ==  R.id.action_add) {
            openAddMedicalBookingFragment(null)
        } else {
            findNavController().popBackStack()
        }
        return true
    }

    // Functions
    private fun setupView() {
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    private fun handleNavigation() {
        if (!showAddMedicalBooking) return
        openAddMedicalBookingFragment(null)
    }

    // Router
    private fun openAddMedicalBookingFragment(doctorAppointmentId: String?) {
        val action = MedicalBookingListFragmentDirections.actionMedicalBookingListFragmentToAddMedicalBookingFragment()
        action.doctorAppointmentId = doctorAppointmentId
        findNavController().navigate(action)
    }
}

@Composable
fun MedicalBookingListScreen(viewModel: MedicalBookingListViewModel,
                             onClickItem: (DynamicUIItem) -> Unit) {

    // Properties
    val itemModels by viewModel.itemModels.collectAsState()

    // Content
    LazyColumn(
        contentPadding = PaddingValues(16.dp)
    ) {
        items(
            items = itemModels,
            key = { it.itemTag }
        ) { item ->
            ConversationItem(
                modifier = Modifier.fillMaxWidth(),
                itemModel = item,
                showAddButton = false,
                onAddClick = {},
                onSelect = onClickItem
            )
        }
    }

}