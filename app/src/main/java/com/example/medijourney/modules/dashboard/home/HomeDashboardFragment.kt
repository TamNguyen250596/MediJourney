package com.example.medijourney.modules.dashboard.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.medijourney.R
import com.example.medijourney.common.helpers.FragmentHelper
import com.example.medijourney.common.models.WebModel
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.modules.dashboard.home.sub_views.advertisement.AdvertisementList
import com.example.medijourney.modules.dashboard.home.sub_views.advertisement.AdvertisementViewModel
import com.example.medijourney.modules.dashboard.home.sub_views.home_doctor_appointments.HomeDoctorAppointmentList
import com.example.medijourney.modules.dashboard.home.sub_views.home_doctor_appointments.HomeDoctorAppointmentViewModel
import com.example.medijourney.modules.dashboard.home.sub_views.medical_purchase_progress.MedicalPurchaseProgressView
import com.example.medijourney.modules.dashboard.home.sub_views.medical_purchase_progress.MedicalPurchaseProgressViewModel
import com.example.medijourney.modules.dashboard.home.sub_views.onboarding.HomeOnboardingList
import com.example.medijourney.modules.dashboard.home.sub_views.onboarding.HomeOnboardingViewModel
import com.example.medijourney.modules.dashboard.home.sub_views.top_dashboard_menus.TopDashboardMenus
import com.example.medijourney.modules.dashboard.home.sub_views.top_dashboard_menus.TopDashboardMenusViewModel

class HomeDashboardFragment : Fragment() {

    // Properties
    private val advertisementViewModel: AdvertisementViewModel by viewModels()
    private val topDashboardMenusViewModel: TopDashboardMenusViewModel by viewModels()
    private val homeDoctorAppointmentViewModel: HomeDoctorAppointmentViewModel by viewModels()
    private val medicalPurchaseProgressViewModel: MedicalPurchaseProgressViewModel by viewModels()
    private val homeOnboardingViewModel: HomeOnboardingViewModel by viewModels()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentHelper.createBaseComposeView(inflater, container) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HomeDashboardSubViews()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        advertisementViewModel.onViewCreated()
        topDashboardMenusViewModel.onViewCreated()
        homeDoctorAppointmentViewModel.onViewCreated()
        medicalPurchaseProgressViewModel.onViewCreated()
        homeOnboardingViewModel.onViewCreated()
    }

    // Functions
    @Composable
    private fun HomeDashboardSubViews() {
        AdvertisementList(
            viewModel = advertisementViewModel,
            onClickItem = {
                val webModel = advertisementViewModel.getWebViewModel(it) ?: return@AdvertisementList
                openWebView(webModel)
            }
        )

        TopDashboardMenus(
            viewModel = topDashboardMenusViewModel,
            onClickShowMore = {
                openDashboardMenusView()
            },
            onClickItem = {
                handleCLickOnTopMenu(it)
            }
        )

        HomeDoctorAppointmentList(
            viewModel = homeDoctorAppointmentViewModel,
            onShowMore = {
                openMedicalBookingList(false)
            },
            onClickItem = {
                val id = homeDoctorAppointmentViewModel.getDoctorAppointmentId(it) ?: return@HomeDoctorAppointmentList
                openAddMedicalBookingView(id)
            }
        )

        MedicalPurchaseProgressView(
            viewModel = medicalPurchaseProgressViewModel,
            onSelectedItem = {
                openPurchasedMedicalProductList(it)
            }
        )

        HomeOnboardingList(
            viewModel = homeOnboardingViewModel,
            onShowMore = {
                openManageOnboardingView()
            },
            onClickItem = {
                val webModel = homeOnboardingViewModel.getWebViewModel(it) ?: return@HomeOnboardingList
                openWebView(webModel)
            }
        )
    }

    private fun handleCLickOnTopMenu(itemModel: ImageItemModel) {
        when (itemModel.itemTag) {
            "medical_booking" -> {
                openMedicalBookingList(true)
            }
            "buy_medication" -> {
                openMedicationListView()
            }
            "medical_knowledge" -> {
                openEducationalOrganizationsView()
            }
        }
    }

    // Routers
    private fun openWebView(webModel: WebModel) {
        val action = HomeDashboardFragmentDirections.actionHomeDashboardFragmentToWebViewFragment()
        action.webModel = webModel
        findNavController().navigate(action)
    }

    private fun openMedicalBookingList(showAddMedicalBooking: Boolean) {
        val action = HomeDashboardFragmentDirections.actionHomeDashboardFragmentToMedicalBookingListFragment()
        action.showAddMedicalBooking = showAddMedicalBooking
        findNavController().navigate(action)
    }

    private fun openAddMedicalBookingView(doctorAppointmentId: String) {
        val action = HomeDashboardFragmentDirections.actionHomeDashboardFragmentToAddMedicalBookingFragment()
        action.doctorAppointmentId = doctorAppointmentId
        findNavController().navigate(action)
    }

    private fun openMedicationListView() {
        findNavController().navigate(R.id.action_homeDashboardFragment_to_medicationListFragment)
    }

    private fun openPurchasedMedicalProductList(itemModel: DynamicUIItem) {
        val action = HomeDashboardFragmentDirections.actionHomeDashboardFragmentToPurchasedMedicalProductListFragment()
        action.medicalProductStatus = itemModel.itemTag
        action.title = itemModel.title?.text
        findNavController().navigate(action)
    }

    private fun openEducationalOrganizationsView() {
        findNavController().navigate(R.id.action_homeDashboardFragment_to_educationalOrganizationsFragment)
    }

    private fun openDashboardMenusView() {
        findNavController().navigate(R.id.action_homeDashboardFragment_to_dashboardMenusFragment)
    }

    private fun openManageOnboardingView() {
        findNavController().navigate(R.id.action_homeDashboardFragment_to_manageOnboardingFragment)
    }
}