package com.example.medijourney.modules.dashboard.search_sub_specialties

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.helpers.FragmentHelper
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.composes.SearchScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue

class SearchSubSpecialtiesFragment : Fragment() {

    // Properties
    private val viewModel: SearchSubSpecialtiesViewModel by viewModels()
    private val args: SearchSubSpecialtiesFragmentArgs by navArgs()
    private val hospitalId: String by lazy { args.hospitalId }
    private val subSpecialtyId: String? by lazy { args.subSpecialtyId }

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentHelper.createBaseComposeView(inflater, container) {
            SearchSubSpecialtiesScreen(
                viewModel = viewModel,
                onItemClicked = {
                    handleItemClicked(it)
                }
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.highlightItemTag = subSpecialtyId
        viewModel.onViewCreated(hospitalId)
    }

    // Functions
    private fun handleItemClicked(item: DynamicUIItem) {
        val mapData = viewModel.getMedicalSubSpecialityMapData(item)
        viewLifecycleOwner.lifecycleScope.launch {
            delay(500)
            findNavController().apply {
                previousBackStackEntry?.savedStateHandle?.set(Constants.SUB_SPECIALTY_MAP_DATA, mapData)
                popBackStack()
            }
        }
    }
}

@Composable
fun SearchSubSpecialtiesScreen(viewModel: SearchSubSpecialtiesViewModel, onItemClicked: (DynamicUIItem) -> Unit) {

    // Properties
    val itemModels by viewModel.itemModels.collectAsState()

    // Content
    SearchScreen(
        itemModels = itemModels,
        emptyPlaceHolderText = stringResource(R.string.no_hospitals_found),
        highlightItemTag = viewModel.highlightItemTag,
        onQueryChange = {
            viewModel.didSearch = true
            viewModel.searchTextFlow.value = it
        },
        onLastIndexAppear = {},
        onItemClicked = onItemClicked
    )
}