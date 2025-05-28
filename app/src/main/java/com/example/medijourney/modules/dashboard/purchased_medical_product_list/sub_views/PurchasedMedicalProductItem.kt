package com.example.medijourney.modules.dashboard.purchased_medical_product_list.sub_views

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PurchasedMedicalProductItem(imageUrl: String?,
                                title: String?,
                                description: String?,
                                secondaryDescription: String?,
                                showRateView: Boolean,
                                checkEnableRate: () -> Boolean,
                                getInitialRate: () -> Int,
                                onRate: (Int) -> Unit,
                                showConfirmButton: Boolean,
                                onConfirmed: () -> Unit,
                                onSelectedItem: () -> Unit) {

    // Properties
    var bitmap by rememberSaveable { mutableStateOf<Uri?>(null) }

    // LaunchedEffect
    LaunchedEffect(imageUrl) {
        imageUrl?.let {
            FirebaseStorageManager.downloadImage(it) { downloadedBitmap ->
                bitmap = downloadedBitmap
            }
        }
    }

    // Content
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.white),
        ),
        border = BorderStroke(1.dp, color = colorResource(R.color.deep_turquoise_blue_color)),
        onClick = onSelectedItem
    ) {
        ConstraintLayout(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
        ) {

            val (row, iconButton) = createRefs()

            Row(
                modifier = Modifier.constrainAs(row) {
                    top.linkTo(parent.top, 8.dp)
                    bottom.linkTo(parent.bottom, 8.dp)
                    start.linkTo(parent.start, 8.dp)
                    if (showConfirmButton) {
                        end.linkTo(iconButton.start, 16.dp)
                    } else {
                        end.linkTo(parent.end, 8.dp)
                    }
                    width = Dimension.fillToConstraints
                },
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (bitmap != null) {
                    GlideImage(
                        model = bitmap,
                        contentDescription = "",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_box),
                        contentDescription = "",
                        modifier = Modifier.size(56.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    title?.let {
                        Text(
                            text = it,
                            fontFamily = proximaNovaFamily,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    description?.let {
                        Text(
                            text = it,
                            fontFamily = proximaNovaFamily,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp
                        )
                    }

                    secondaryDescription?.let {
                        Text(
                            text = it,
                            fontFamily = proximaNovaFamily,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }

                    if (showRateView) {
                        RateView(
                            enable = checkEnableRate(),
                            rateMax = 5,
                            rate = getInitialRate(),
                            onRate = onRate
                        )
                    }
                }
            }

            if (showConfirmButton) {
                TextButton(
                    modifier = Modifier.constrainAs(iconButton) {
                        top.linkTo(parent.top, 8.dp)
                        bottom.linkTo(parent.bottom, 8.dp)
                        start.linkTo(row.end)
                        end.linkTo(parent.end, 8.dp)
                        width = Dimension.wrapContent
                    },
                    onClick = onConfirmed
                ) {
                    Text(
                        text = stringResource(R.string.confirm),
                        fontFamily = proximaNovaFamily,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colorResource(R.color.deep_turquoise_blue_color)
                    )
                }
            }
        }
    }
}