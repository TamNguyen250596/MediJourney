package com.example.medijourney.modules.profile.membership_plan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.common.ui_components.item_decoration.ItemBorderDecoration
import com.example.medijourney.common.ui_components.recycle_view_adapter.check_box.CheckBoxAdapter
import com.example.medijourney.common.ui_components.recycle_view_adapter.check_box.CheckBoxAdapterInterface
import com.example.medijourney.databinding.FragmentMembershipPlanBinding

class MembershipPlanFragment : Fragment(), CheckBoxAdapterInterface {

    // Properties
    private lateinit var binding: FragmentMembershipPlanBinding
    private val viewModel: MembershipPlanViewModel by viewModels()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMembershipPlanBinding.inflate(inflater, container, false)
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