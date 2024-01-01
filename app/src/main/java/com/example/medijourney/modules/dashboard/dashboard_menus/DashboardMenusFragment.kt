package com.example.medijourney.modules.dashboard.dashboard_menus

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.ui_components.composes.EmptyPlaceholder
import com.example.medijourney.common.ui_components.composes.VImageItem
import com.example.medijourney.databinding.FragmentDashboardMenusBinding
import kotlinx.coroutines.delay

class DashboardMenusFragment : Fragment() {

    // Properties
    private val viewModel: DashboardMenusViewModel by viewModels()
    private lateinit var binding: FragmentDashboardMenusBinding

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardMenusBinding.inflate(inflater, container, false)
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashboardMenusScreen(
                    viewModel = viewModel,
                    onItemClick = {
                        handleCLickOnTopMenu(it)
                    }
                )
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onViewCreated()
    }

    // Functions
    private fun handleCLickOnTopMenu(itemModel: ImageItemModel) {
        when (itemModel.itemTag) {
            "medical_booking" -> {
                openMedicalBookingList()
            }
            "buy_medication" -> {
                openMedicationListView()
            }
            "medical_knowledge" -> {
                openEducationalOrganizationsView()
            }
        }
    }

    // Router
    private fun openMedicalBookingList() {
        val action = DashboardMenusFragmentDirections.actionDashboardMenusFragmentToMedicalBookingListFragment()
        action.showAddMedicalBooking = true
        findNavController().navigate(action)
    }

    private fun openMedicationListView() {
        findNavController().navigate(R.id.action_dashboardMenusFragment_to_medicationListFragment)
    }

    private fun openEducationalOrganizationsView() {
        findNavController().navigate(R.id.action_dashboardMenusFragment_to_educationalOrganizationsFragment)
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardMenusScreen(viewModel: DashboardMenusViewModel, onItemClick: (ImageItemModel) -> Unit) {

    // Properties
    val itemModels by viewModel.itemModels.collectAsState()
    var searchText by rememberSaveable { mutableStateOf("") }
    var showPlaceholder by remember { mutableStateOf(false) }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val width = (screenWidthDp - 20.dp.value.toInt() * 2) / 3

    // LaunchedEffect
    LaunchedEffect(itemModels) {
        if (itemModels.isEmpty()) {
            delay(1000)
            showPlaceholder = true
        } else {
            showPlaceholder = false
        }
    }

    // Content
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchText,
                        onQueryChange = {
                            searchText = it
                            viewModel.searchTextFlow.value = it
                        },
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = {},
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = colorResource(R.color.disable_grey_color)
                            )
                        },
                        trailingIcon = {},
                    )
                },
                expanded = false,
                onExpandedChange = {},
                windowInsets = WindowInsets(top = 0),
                content = {}
            )

            LazyVerticalGrid(
                columns = GridCells.FixedSize(width.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                items (
                    items = itemModels,
                    key = { it.itemTag },
                    span = { item ->
                        if (item.type == Constants.HEADER) {
                            GridItemSpan(maxLineSpan)
                        } else {
                            GridItemSpan(1)
                        }
                    }
                ) {
                    if (it.type == Constants.HEADER) {
                        it.title?.text?.let {
                            Text(
                                text = it,
                                fontFamily = proximaNovaFamily,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = colorResource(id = R.color.deep_turquoise_blue_color),
                                textAlign = TextAlign.Start
                            )
                        }
                    } else {
                        VImageItem(
                            itemModel = it,
                            modifier = Modifier.fillMaxSize(),
                            imageModifier = Modifier.size(48.dp),
                            cardColors = CardDefaults.cardColors(
                                containerColor = colorResource(id = R.color.transparent)
                            ),
                            onClick = onItemClick
                        )
                    }
                }
            }

            if (showPlaceholder) {
                EmptyPlaceholder(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(R.string.empty_dashboard_menu_placeholder)
                )
            }
        }
    }
}