package com.example.medijourney.modules.profile.manage_profile

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medijourney.R
import com.example.medijourney.common.managers.InternationManager
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.common.models.WebModel
import com.example.medijourney.common.ui_components.fragments.web_view.WebViewConstant
import com.example.medijourney.common.ui_components.item_decoration.GroupedItemsBorderDecoration
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.databinding.FragmentManageProfileBinding
import kotlinx.coroutines.launch

class ManageProfileFragment : Fragment(), BaseAdapterInterface {

    // Properties
    private lateinit var binding: FragmentManageProfileBinding
    private val viewModel: ManageProfileViewModel by viewModels()

    // Life cycle
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentManageProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
    }

    // Functions
    private fun setupView() {
        binding.recycleView.layoutManager = LinearLayoutManager(requireContext())
        binding.recycleView.addItemDecoration(GroupedItemsBorderDecoration(requireContext()))
    }

    private fun observeViewModel() {
        viewModel.itemModels.observe(viewLifecycleOwner) { dataList ->
            val adapter = binding.recycleView.adapter as? ManageProfileAdapter
            if (adapter == null) {
                val newAdapter = ManageProfileAdapter(dataList).apply {
                    output = this@ManageProfileFragment
                }
                binding.recycleView.adapter = newAdapter
            } else {
                adapter.updateItems(dataList)
            }
        }
    }

    // BaseAdapterInterface2
    override fun selectedItem(model: BaseItemInterface?) {
        when (model?.itemTag) {
            "tell_a_friend" -> {
                findNavController().navigate(R.id.action_manageProfileFragment_to_shareAppFragment)
            }
            "show_in_dashboard" -> {
                findNavController().navigate(R.id.action_manageProfileFragment_to_manageOnboardingFragment)
            }
            "membership_plan" -> {
                findNavController().navigate(R.id.action_manageProfileFragment_to_membershipPlanFragment)
            }
            "change_password" -> {
                findNavController().navigate(R.id.action_manageProfileFragment_to_changePasswordFragment)
            }
            "advanced_security" -> {
                findNavController().navigate(R.id.action_manageProfileFragment_to_advancedSecurityFragment)
            }
            "delete_account" -> {
                findNavController().navigate(R.id.action_manageProfileFragment_to_deleteAccountFragment)
            }
            "language" -> {
                showLanguageDialog()
            }
            "country" -> {
                showCountryDiaLog()
            }
            "terms_of_service" -> {
                handleTermsOfService()
            }
            "privacy_policy" -> {
                handlePrivacyPolicy()
            }
        }
    }

    // Router
    private fun showCountryDiaLog() {
        viewLifecycleOwner.lifecycleScope.launch {
            val info = viewModel.generateCountryNameInfo()

            AlertDialog.Builder(requireContext()).apply {
                setSingleChoiceItems(info.first, info.second) { dialogInterface, i ->
                    IndicatorHandler.show(requireContext())
                    dialogInterface.dismiss()
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.updateCurrentCountryCode(i)
                        IndicatorHandler.hide()
                        InternationManager.processChangeCountry(requireActivity())
                    }
                }
                show()
            }
        }
    }

    private fun showLanguageDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            val info = viewModel.generateLanguageNameInfo()

            AlertDialog.Builder(requireContext()).apply {
                setSingleChoiceItems(info.first, info.second) { dialogInterface, i ->
                    IndicatorHandler.show(requireContext())
                    dialogInterface.dismiss()
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.updateCurrentLanguageCode(i)
                        IndicatorHandler.hide()
                        InternationManager.processChangeCountry(requireActivity())
                    }
                }
                show()
            }
        }
    }

    private fun handleTermsOfService() {
        val model = WebModel().apply { webViewType = WebViewConstant.TERM_OF_SERVICE }
        val action = ManageProfileFragmentDirections.actionManageProfileFragmentToWebViewFragment()
        action.webModel = model
        findNavController().navigate(action)

    }

    private fun handlePrivacyPolicy() {
        val model = WebModel().apply { webViewType = WebViewConstant.PRIVACY_POLICY }
        val action = ManageProfileFragmentDirections.actionManageProfileFragmentToWebViewFragment()
        action.webModel = model
        findNavController().navigate(action)
    }
}