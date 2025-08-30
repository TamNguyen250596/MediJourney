package com.example.medijourney.modules.profile.manage_onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.common.ui_components.item_decoration.GroupedSectionBorderDecoration
import com.example.medijourney.common.ui_components.recycle_view_adapter.SwitchButtonItemHolderInterface
import com.example.medijourney.databinding.FragmentManageOnboardingBinding
import com.example.medijourney.modules.profile.manage_onboarding.adapter.ManageOnboardingAdapter

class ManageOnboardingFragment : Fragment(), SwitchButtonItemHolderInterface {

    // Properties
    private lateinit var binding: FragmentManageOnboardingBinding
    private val viewModel: ManageOnboardingViewModel by viewModels()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentManageOnboardingBinding.inflate(inflater, container, false)
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
        binding.recycleView.addItemDecoration(GroupedSectionBorderDecoration(requireContext()))
    }

    private fun observeViewModel() {
        viewModel.dataList.observe(viewLifecycleOwner) {
            val adapter = binding.recycleView.adapter as? ManageOnboardingAdapter
            if (adapter == null) {
                val newAdapter = ManageOnboardingAdapter(it).apply {
                    output = this@ManageOnboardingFragment
                }
                binding.recycleView.adapter = newAdapter
            } else {
                this.viewModel.currentUpdatedIndex?.let { index ->
                    adapter.updateItem(index, it[index])
                    viewModel.currentUpdatedIndex = null
                } ?: run {
                    adapter.updateItems(it)
                }
            }
        }
    }

    // SwitchButtonItemHolderInterface
    override fun didSwitch(isChecked: Boolean, model: BaseItemInterface) {
        IndicatorHandler.show(requireContext())
        viewModel.updateUserMedicalSpecialty(isChecked, model) {
            IndicatorHandler.hide()
        }
    }

    override fun selectedItem(model: BaseItemInterface?) {
        viewModel.getWebViewModel(model)
    }
}