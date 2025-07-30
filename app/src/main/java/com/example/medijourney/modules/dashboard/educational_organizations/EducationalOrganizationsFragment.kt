package com.example.medijourney.modules.dashboard.educational_organizations

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.findNavController
import com.example.medijourney.R
import com.example.medijourney.common.helpers.FragmentHelper
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.composes.LImage3TextsRButtonView

class EducationalOrganizationsFragment : Fragment() {

    // Properties
    private val viewModel: EducationalOrganizationsViewModel by viewModels()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentHelper.createBaseComposeView(inflater, container) {
            EducationalOrganizationsScreen(
                viewModel = viewModel,
                onItemClicked = {
                    handleSelectedItem(it)
                }
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onViewCreated()
    }

    // Functions
    private fun handleSelectedItem(itemModel: DynamicUIItem) {
        when (itemModel.itemTag) {
            "syndigo_university" -> {
                findNavController().navigate(R.id.action_educationalOrganizationsFragment_to_educationalNutrientFragment)
            }
        }
    }
}

@Composable
fun EducationalOrganizationsScreen(
    viewModel: EducationalOrganizationsViewModel,
    onItemClicked: (DynamicUIItem) -> Unit
) {
    // Properties
    val itemModels by viewModel.itemModels.collectAsState()

    // Content
    LazyColumn(
        contentPadding = PaddingValues(16.dp)
    ) {
        items(items = itemModels,
            key = { it.itemTag })
        {
            LImage3TextsRButtonView(
                modifier = Modifier.padding(8.dp),
                itemModel = it,
                onClickRightButton = {},
                onClickItem = {
                    onItemClicked(it)
                }
            )
        }
    }
}
