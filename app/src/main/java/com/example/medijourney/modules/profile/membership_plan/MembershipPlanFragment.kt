package com.example.medijourney.modules.profile.membership_plan

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medijourney.common.managers.InternationManager
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.check_box.CheckBoxAdapter
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.databinding.FragmentMembershipPlanBinding
import com.example.medijourney.common.ui_components.item_decoration.ItemBorderDecoration
import com.example.medijourney.common.ui_components.recycle_view_adapter.check_box.CheckBoxAdapterInterface
import kotlinx.coroutines.launch

class MembershipPlanFragment : Fragment(), CheckBoxAdapterInterface {

    // Properties
    private lateinit var binding: FragmentMembershipPlanBinding
    private val viewModel: MembershipPlanViewModel by viewModels()

    // Life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            viewModel.inputData(InternationManager.getCurrentLanguageCode(requireContext()))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMembershipPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onCreateView()
        setupView()
        observeViewModel()
    }

    // Functions
    private fun setupView() {
        binding.recycleView.layoutManager = LinearLayoutManager(requireContext())
        binding.recycleView.addItemDecoration(ItemBorderDecoration(requireContext()))
    }

    private fun observeViewModel() {
        viewModel.membershipPlans.observe(viewLifecycleOwner) {
            val adapter = binding.recycleView.adapter as? CheckBoxAdapter
            if (adapter == null) {
                val newAdapter = CheckBoxAdapter(it).apply {
                    output = this@MembershipPlanFragment
                }
                binding.recycleView.adapter = newAdapter
            } else {
                adapter.updateItems(it)
            }
        }
    }

    // CheckBoxAdapterInterface
    override fun selectedItem(model: BaseItemInterface?) {
        IndicatorHandler.show(requireContext())
        viewModel.handleSelectedItem(model) {
            IndicatorHandler.hide()
        }
    }

    override fun selectedCheckBox(model: BaseItemInterface?, isChecked: Boolean) {}
}