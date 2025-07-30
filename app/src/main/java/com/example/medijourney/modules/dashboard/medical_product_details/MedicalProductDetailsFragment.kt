package com.example.medijourney.modules.dashboard.medical_product_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.helpers.FragmentHelper
import com.example.medijourney.common.ui_components.composes.CIndicator
import com.example.medijourney.common.ui_components.composes.SelectionBottomSheet
import com.example.medijourney.modules.dashboard.medical_product_details.sub_views.ItemNumberView

class MedicalProductDetailsFragment : Fragment() {

    // Properties
    private val viewModel: MedicalProductDetailsViewModel by viewModels()
    private val args: MedicalProductDetailsFragmentArgs by navArgs()
    private val medicalProductId: String by lazy { args.medicalProductId }
    private val isPreview: Boolean by lazy { args.isPreview }

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentHelper.createBaseComposeView(inflater, container) {
            MedicalProductDetailsScreen(
                viewModel = viewModel,
                isPreview = isPreview,
                onSelectedPayment = {
                    handleBuyMedicalProduct()
                }
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onViewCreated(medicalProductId)
    }

    // Function
    private fun handleBuyMedicalProduct() {
        viewModel.buyMedicalProduct {
            if (!it) return@buyMedicalProduct
            findNavController().popBackStack()
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MedicalProductDetailsScreen(viewModel: MedicalProductDetailsViewModel,
                                isPreview: Boolean,
                                onSelectedPayment: () -> Unit = {}) {

    // Properties
    val isLoading by viewModel.isLoading.collectAsState()
    val bitmap by viewModel.bitmap.collectAsState()
    val productTitle by viewModel.productTitle.collectAsState()
    val htmlDescription by viewModel.htmlDescription.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val displayPaymentPicker by viewModel.displayPaymentPicker.collectAsState()
    val paymentMethodItemModels by viewModel.paymentMethodItemModels.collectAsState()

    // Content
    Box(
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlideImage(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    model = bitmap,
                    contentDescription = "",
                    contentScale = ContentScale.FillBounds
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        productTitle,
                        fontFamily = proximaNovaFamily,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        fontSize = TextUnit(16f, TextUnitType.Sp),
                        maxLines = 2
                    )

                    ItemNumberView(
                        onCountChanged = {
                            viewModel.updatePrice(it)
                        }
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            ) {
                Text(
                    AnnotatedString.fromHtml(
                        htmlDescription,
                        linkStyles = TextLinkStyles(
                            style = SpanStyle(
                                textDecoration = TextDecoration.Underline,
                                fontStyle = FontStyle.Italic,
                                color = colorResource(id = R.color.deep_turquoise_blue_color),
                                fontFamily = proximaNovaFamily,
                                fontSize = TextUnit(16f, TextUnitType.Sp)
                            )
                        )
                    ),
                    fontFamily = proximaNovaFamily,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                    fontSize = TextUnit(16f, TextUnitType.Sp),
                    modifier = Modifier
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                )
            }

            if (!isPreview) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        modifier = Modifier.weight(7f),
                        text = stringResource(R.string.total_price, totalPrice),
                        fontFamily = proximaNovaFamily,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.deep_turquoise_blue_color)
                    )

                    OutlinedButton(
                        modifier = Modifier.weight(3f),
                        onClick = {
                            viewModel.updatePaymentMethodPicker(true)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = colorResource(id = R.color.light_blue_color)
                        ),
                        border = BorderStroke(1.dp, colorResource(id = R.color.deep_turquoise_blue_color)),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.buy),
                                fontFamily = proximaNovaFamily,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                color = colorResource(id = R.color.deep_turquoise_blue_color)
                            )
                        }
                    }
                }
            }
        }

        if (isLoading) {
            CIndicator()
        }
    }

    if (displayPaymentPicker) {
        SelectionBottomSheet(
            itemModels = paymentMethodItemModels,
            skipPartiallyExpanded = false,
            selectedItemTag = null,
            onDismissRequest = {
                viewModel.updatePaymentMethodPicker(false)
            },
            onSelectedItem = {
                onSelectedPayment()
            }
        )
    }
}