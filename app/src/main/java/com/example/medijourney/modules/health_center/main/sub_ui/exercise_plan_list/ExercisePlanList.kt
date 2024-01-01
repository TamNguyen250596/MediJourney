package com.example.medijourney.modules.health_center.main.sub_ui.exercise_plan_list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.composes.EmptyPlaceholder

@Composable
fun ExercisePlanList(viewModel: ExercisePlanListViewModel,
                     onAddNewPlan: () -> Unit,
                     onPlanSelected: (DynamicUIItem) -> Unit,
                     onShowPlanInfo: (DynamicUIItem) -> Unit) {

    // Content
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.exercise_plans),
                modifier = Modifier.weight(1f).wrapContentWidth(Alignment.Start),
                fontFamily = proximaNovaFamily,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(16f, TextUnitType.Sp)
            )

            OutlinedButton(
                onClick = { onAddNewPlan() },
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.End),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colorResource(id = R.color.light_blue_color)
                ),
                border = BorderStroke(1.dp, colorResource(id = R.color.deep_turquoise_blue_color)),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_add_plus),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorResource(id = R.color.deep_turquoise_blue_color))
                    )
                    Text(
                        text = stringResource(R.string.add),
                        fontFamily = proximaNovaFamily,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.deep_turquoise_blue_color)
                    )
                }
            }
        }

        if (viewModel.userPlanItemStateList.isEmpty()) {
            EmptyPlaceholder(
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                text = stringResource(R.string.empty_exercise_plan_placeholder)
            )
        } else {
            val configuration = LocalConfiguration.current
            val screenWidthDp = configuration.screenWidthDp
            val width = if (viewModel.userPlanItemStateList.size == 1) {
                (screenWidthDp - 16.dp.value.toInt() * 2)
            } else {
                (screenWidthDp * 0.8 - 16.dp.value.toInt()).toInt()
            }
            val modifier = Modifier.width(width.dp)

            LazyRow(
                modifier = Modifier.padding(top = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.userPlanItemStateList) { plan ->
                    ExercisePlanItem(
                        planItemModel = plan,
                        modifier = modifier,
                        onPlanSelected = onPlanSelected,
                        onShowPlanInfo = onShowPlanInfo
                    )
                }
            }
        }
    }
}

@Composable
fun ExercisePlanItem(planItemModel: DynamicUIItem,
                     modifier: Modifier = Modifier,
                     onPlanSelected: (DynamicUIItem) -> Unit,
                     onShowPlanInfo: (DynamicUIItem) -> Unit) {

    // Content
    OutlinedCard(
        onClick = { onPlanSelected(planItemModel) },
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.white)
        ),
        border = BorderStroke(1.dp, colorResource(R.color.deep_turquoise_blue_color))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            planItemModel.title?.let {
                Text(
                    text = it.text,
                    fontFamily = proximaNovaFamily,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_clock),
                    contentDescription = null
                )
                planItemModel.description?.let {
                    Text(
                        text = it.text,
                        fontFamily = proximaNovaFamily,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            }

            ElevatedButton(
                onClick = { onShowPlanInfo(planItemModel) },
                modifier = Modifier.padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier.size(12.dp),
                        painter = painterResource(id = R.drawable.ic_list),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorResource(id = R.color.forest_green_color))
                    )
                    Text(
                        text = stringResource(R.string.tap_check_out_exercise_plan),
                        fontFamily = proximaNovaFamily,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.forest_green_color)
                    )
                }
            }
        }
    }
}