package com.example.medijourney.common.helpers.charts

import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.models.realm_models.UserExerciseTrackingReport
import com.example.medijourney.common.models.realm_models.UserNutritionTrackingReport
import com.example.medijourney.common.models.realm_models.UserSleepTrackingReport
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import io.realm.kotlin.ext.isValid

class DayAxisValueFormatter(private val barChart: BarChart, private val dateFormat: String) :
    ValueFormatter() {

    // Functions
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val defaultLabel = super.getAxisLabel(value, axis)
        val chartData = barChart.data
        val barDataSet = chartData.dataSets.firstOrNull() ?: return defaultLabel
        val barEntry = barDataSet.getEntriesForXValue(value).firstOrNull() ?: return defaultLabel
        val data = barEntry.data ?: return defaultLabel

        return getLabel(data, defaultLabel)
    }

    private fun getLabel(value: Any, defaultLabel: String): String {
        return when (value) {
            is UserSleepTrackingReport -> {
                if (!value.isValid()) return defaultLabel
                val reportedAt = value.reportedAt ?: return defaultLabel
                DateHelper.convertRealmInstantToString(reportedAt, dateFormat)
            }
            is UserNutritionTrackingReport -> {
                if (!value.isValid()) return defaultLabel
                val reportedAt = value.reportedAt ?: return defaultLabel
                DateHelper.convertRealmInstantToString(reportedAt, dateFormat)
            }
            is UserExerciseTrackingReport -> {
                if (!value.isValid()) return defaultLabel
                val reportedAt = value.reportedAt ?: return defaultLabel
                DateHelper.convertRealmInstantToString(reportedAt, dateFormat)
            }
            else -> defaultLabel
        }
    }
}