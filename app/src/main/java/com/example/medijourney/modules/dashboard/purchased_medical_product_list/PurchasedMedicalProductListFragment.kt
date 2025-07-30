package com.example.medijourney.modules.dashboard.purchased_medical_product_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.medijourney.common.helpers.FragmentHelper
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.composes.CIndicator
import com.example.medijourney.modules.dashboard.purchased_medical_product_list.sub_views.PurchasedMedicalProductItem

class PurchasedMedicalProductListFragment : Fragment() {

    // Properties
    private val viewModel: PurchasedMedicalProductListViewModel by viewModels()
    private val args: PurchasedMedicalProductListFragmentArgs by navArgs()
    private val medicalProductStatus: String by lazy { args.medicalProductStatus }
    private val title: String? by lazy { args.title }

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentHelper.createBaseComposeView(inflater, container) {
            PurchasedMedicalProductListScreen(
                viewModel = viewModel,
                onSelectedItem = {
                }
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewModel.onViewCreated(medicalProductStatus)
    }

    // Functions
    private fun setupView() {
        (activity as? AppCompatActivity)?.supportActionBar?.title = title
    }
}

// Composable
@Composable
fun PurchasedMedicalProductListScreen(viewModel: PurchasedMedicalProductListViewModel,
                                      onSelectedItem: (DynamicUIItem) -> Unit) {

    // Properties
    val isLoading by viewModel.isLoading.collectAsState()
    val itemModels by viewModel.itemModels.collectAsState()
    val listState = rememberLazyListState()

    // LaunchedEffect
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.indices }
            .collect { indices ->
                viewModel.seenProduct(indices.toList())
            }
    }

    // Content
    Box(
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(
                items = itemModels,
                key = { it.itemTag }
            ) {
                PurchasedMedicalProductItem(
                    imageUrl = it.image?.url,
                    title = it.title?.text,
                    description = it.description?.text,
                    secondaryDescription = it.secondaryDescription?.text,
                    showRateView = viewModel.checkShowRate(it),
                    checkEnableRate = {
                        viewModel.checkEnableRate(it)
                    },
                    getInitialRate = {
                        viewModel.getInitialRate(it)
                    },
                    onRate = { rate ->
                        viewModel.rateProduct(it, rate)
                    },
                    showConfirmButton = viewModel.checkShowConfirmButton(it),
                    onConfirmed = {
                        viewModel.confirmToReceiveProduct(it)
                    },
                    onSelectedItem = {
                        onSelectedItem(it)
                    }
                )
            }
        }

        if (isLoading) {
            CIndicator()
        }
    }
}