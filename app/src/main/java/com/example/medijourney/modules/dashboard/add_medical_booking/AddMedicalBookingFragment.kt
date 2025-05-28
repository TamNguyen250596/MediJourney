package com.example.medijourney.modules.dashboard.add_medical_booking

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.composes.CIndicator
import com.example.medijourney.common.ui_components.composes.DatePickerModal
import com.example.medijourney.common.ui_components.composes.SaveButton
import com.example.medijourney.databinding.FragmentAddMedicalBookingBinding
import com.example.medijourney.modules.dashboard.add_medical_booking.sub_views.AddMedicalBookingItem
import com.example.medijourney.common.ui_components.composes.SelectionBottomSheet

class AddMedicalBookingFragment : Fragment(), MenuProvider {

    // Properties
    private val viewModel: AddMedicalBookingViewModel by viewModels()
    private lateinit var binding: FragmentAddMedicalBookingBinding
    private val args: AddMedicalBookingFragmentArgs by navArgs()
    private val doctorAppointmentId: String? by lazy { args.doctorAppointmentId }

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddMedicalBookingBinding.inflate(inflater, container, false)
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AddMedicalBookingScreen(
                    viewModel = viewModel,
                    onFieldClick = {
                        handleFieldClick(it)
                    },
                    onSaveClick = {
                        handleAddMedicalBooking()
                    }
                )
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewModel.onViewCreated(doctorAppointmentId)
        observeNavController()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.delete_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId ==  R.id.action_delete) {
            viewModel.deleteDoctorAppointment {
                if (!it) return@deleteDoctorAppointment
                findNavController().popBackStack()
            }
        } else {
            findNavController().popBackStack()
        }
        return true
    }

    // Functions
    private fun setupView() {
        if (doctorAppointmentId == null) return
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    private fun handleFieldClick(itemModel: DynamicUIItem) {
        when (itemModel.itemTag) {
            "hospital" -> {
                openSearchHospitalFragment()
            }
            "medical_specialty" -> {
                openSearchSubSpecialitiesFragment()
            }
            "date" -> {
                viewModel.updateDisplayDatePicker(true)
            }
            "doctor_and_time" -> {
                viewModel.updateDoctorTimePicker(true)
            }
            "payment" -> {
                viewModel.updatePaymentMethodPicker(true)
            }
        }
    }

    private fun observeNavController() {
        val currentBackStackEntry = findNavController().currentBackStackEntry ?: return
        val savedStateHandle = currentBackStackEntry.savedStateHandle

        savedStateHandle.
        getLiveData<Map<String, String>>(Constants.HOSPITAL_MAP_DATA).
        observe(viewLifecycleOwner) { result ->
            viewModel.updateField("hospital", result)
        }

        savedStateHandle.
        getLiveData<Map<String, String>>(Constants.SUB_SPECIALTY_MAP_DATA).
        observe(viewLifecycleOwner) { result ->
            viewModel.updateField("medical_specialty", result)
        }
    }

    private fun handleAddMedicalBooking() {
        viewModel.addMedicalBooking {
            if (!it) return@addMedicalBooking
            findNavController().popBackStack()
        }
    }

    // Routers
    private fun openSearchHospitalFragment() {
        val action = AddMedicalBookingFragmentDirections.actionAddMedicalBookingFragmentToSearchHospitalFragment()
        action.hospitalId = viewModel.getObjectId("hospital")
        findNavController().navigate(action)
    }

    private fun openSearchSubSpecialitiesFragment() {
        findNavController().apply {
            val hospitalId = viewModel.getObjectId("hospital") ?: return@apply
            val action = AddMedicalBookingFragmentDirections.actionAddMedicalBookingFragmentToSearchSubSpecialtiesFragment()

            action.subSpecialtyId = viewModel.getObjectId("medical_specialty")
            action.hospitalId = hospitalId
            navigate(action)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicalBookingScreen(viewModel: AddMedicalBookingViewModel,
                            onFieldClick: (DynamicUIItem) -> Unit,
                            onSaveClick: () -> Unit) {

    // Properties
    val itemModels by viewModel.itemModels.collectAsState()
    val enableAddMedicalBooking by viewModel.enableAddMedicalBooking.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val displayDatePicker by viewModel.displayDatePicker.collectAsState()
    val displayDoctorTimePicker by viewModel.displayDoctorTimePicker.collectAsState()
    val timeItemModels by viewModel.doctorTimeItemModels.collectAsState()
    val displayPaymentPicker by viewModel.displayPaymentPicker.collectAsState()
    val paymentMethodItemModels by viewModel.paymentMethodItemModels.collectAsState()

    // Content
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            ) {
                items(
                    items = itemModels,
                    key = { it.itemTag }
                ) {
                    AddMedicalBookingItem(
                        itemModel = it,
                        onClickItem = {
                            onFieldClick(it)
                        }
                    )
                }
            }

            SaveButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                enableSaveButton = enableAddMedicalBooking,
                onSaveClick = onSaveClick
            )
        }

        if (displayDatePicker) {
            DatePickerModal(
                initialSelectedDateMillis = viewModel.initialSelectedDateMillis,
                onSelectableDate = {
                    viewModel.checkSelectableDate(it)
                },
                onSelectableYear = {
                    viewModel.checkSelectedYear(it)
                },
                onDateSelected = {
                    viewModel.updateDateField(it)
                },
                onDismiss = {
                    viewModel.updateDisplayDatePicker(false)
                }
            )
        }

        if (isLoading) {
            CIndicator()
        }
    }

    if (displayDoctorTimePicker) {
        SelectionBottomSheet(
            itemModels = timeItemModels,
            skipPartiallyExpanded = true,
            selectedItemTag = viewModel.selectedDoctorTimeItemTag,
            onDismissRequest = {
                viewModel.updateDoctorTimePicker(false)
            },
            onSelectedItem = {
                viewModel.updateDoctorTimeField(it)
            }
        )
    }

    if (displayPaymentPicker) {
        SelectionBottomSheet(
            itemModels = paymentMethodItemModels,
            skipPartiallyExpanded = true,
            selectedItemTag = viewModel.selectedPaymentMethodItemTag,
            onDismissRequest = {
                viewModel.updatePaymentMethodPicker(false)
            },
            onSelectedItem = {
                viewModel.updatePaymentField(it)
            }
        )
    }
}