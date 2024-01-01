package com.example.medijourney.common.helpers.charts

import androidx.core.content.res.ResourcesCompat
import com.example.medijourney.R
import com.example.medijourney.common.helpers.MediJourney
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis

object ChartHelper {

    // Bar chart
    fun setupBarChart(barChart: BarChart) {
        barChart.setDrawBarShadow(false)
        barChart.description.isEnabled = false
        barChart.setPinchZoom(false)
        barChart.setDrawGridBackground(false)
        barChart.legend.isEnabled = false
        barChart.setDrawValueAboveBar(false)
        barChart.axisLeft.isEnabled = false
        barChart.extraBottomOffset = 8f
        barChart.extraTopOffset = 24f
        barChart.isScrollContainer = true
        barChart.isDragEnabled = true
        barChart.setScaleEnabled(false)
    }

    fun setupXAxis(xAxis: XAxis) {
        val context = MediJourney.getAppContext()
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.typeface = ResourcesCompat.getFont(context, R.font.proximanova_regular)
        xAxis.textSize = 14f
        xAxis.axisLineWidth = 1f
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.axisLineWidth = 2f
        xAxis.axisLineColor = ResourcesCompat.getColor(context.resources, R.color.deep_turquoise_blue_color, null)
    }

    fun setupAxisRight(yAxis: YAxis) {
        val context = MediJourney.getAppContext()
        yAxis.setDrawGridLines(false)
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        yAxis.typeface = ResourcesCompat.getFont(context, R.font.proximanova_regular)
        yAxis.textSize = 14f
        yAxis.axisLineWidth = 1f
        yAxis.granularity = 1f
        yAxis.axisLineWidth = 2f
        yAxis.axisLineColor = ResourcesCompat.getColor(context.resources, R.color.deep_turquoise_blue_color, null)
    }
}