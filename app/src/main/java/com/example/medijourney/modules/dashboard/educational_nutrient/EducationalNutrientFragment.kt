package com.example.medijourney.modules.dashboard.educational_nutrient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.helpers.FragmentHelper
import com.example.medijourney.common.ui_components.composes.CIndicator
import com.example.medijourney.common.ui_components.composes.EmptyPlaceholder
import com.example.medijourney.common.ui_components.composes.HorizontalRow

class EducationalNutrientFragment : Fragment() {

    // Properties
    private val viewModel: EducationalNutrientViewModel by viewModels()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentHelper.createBaseComposeView(inflater, container) {
            EducationalNutrientScreen(viewModel)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onViewCreated()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun EducationalNutrientScreen(viewModel: EducationalNutrientViewModel) {

    // Properties
    val isLoading by viewModel.isLoading.collectAsState()
    val showPlaceholder by viewModel.showPlaceholder.collectAsState()
    val photoUrl by viewModel.photoUrl.collectAsState()
    val name by viewModel.name.collectAsState()
    val itemModels by viewModel.itemModels.collectAsState()
    var searchText by rememberSaveable { mutableStateOf("") }

    // Content
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

            if (!showPlaceholder) {
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            photoUrl?.let {
                                GlideImage(
                                    model = photoUrl,
                                    contentDescription = "",
                                    modifier = Modifier.size(56.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            name?.let {
                                Text(
                                    text = it,
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    fontFamily = proximaNovaFamily,
                                    fontStyle = FontStyle.Normal,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(
                                items = itemModels,
                                key = { it.itemTag }
                            ) {
                                HorizontalRow(it)
                            }
                        }
                    }
                }
            } else {
                EmptyPlaceholder(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    text = stringResource(R.string.no_results_found)
                )
            }
        }

        if (isLoading) {
            CIndicator()
        }
    }
}