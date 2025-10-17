package com.example.medijourney.modules.health_center.sleep_tracking_report

import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.helpers.MediJourney
import com.example.medijourney.common.managers.AppSleepTrackingReportDetails
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.UserSleepTrackingReport
import com.example.medijourney.common.models.ui_models.EdgePadding
import com.example.medijourney.common.models.ui_models.Segment
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.UserFitnessTrackerRepo
import com.example.medijourney.common.respositories.UserSleepTrackingReportRepo
import com.example.medijourney.common.ui_components.recycle_view_adapter.h_dual_image_text_view.HDualImageTextViewHolder
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SleepTrackingReportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @AppSleepTrackingReportDetails private val currentAppSleepTrackingReportDetails: Map<String, *>,
    private val userFitnessTrackerRepository: UserFitnessTrackerRepo,
    private val userSleepTrackingReportRepository: UserSleepTrackingReportRepo
) : ViewModel() {

    // Properties
    var selectedDate = MutableLiveData<Date?>()
    var barChartData = MutableLiveData<BarData>()
    var segments = MutableLiveData<List<Segment>>()
    var itemModels = MutableLiveData<MutableList<DynamicUIItem>>()
    var description = MutableLiveData<String?>()
    var visibleXRangeMaximum = 5f
    var limitLine: LimitLine? = null

    // Life cycle
    init {
        val userFitnessTrackerId = savedStateHandle.get<String>("userFitnessTrackerId")
        viewModelScope.launch {
            observeData(userFitnessTrackerId)
        }
    }

    // Functions
    private suspend fun observeData(id: String?) = supervisorScope {
        if (id == null) return@supervisorScope

        val deviceId = userFitnessTrackerRepository
            .getUserFitnessTrackerFlow(id)
            .first {
                it != null && it.isValid()
            }?.deviceId ?: return@supervisorScope

        launch {
            userSleepTrackingReportRepository.observeUserSleepTrackingReports(deviceId)
        }
        launch {
            userSleepTrackingReportRepository
                .getUserSleepTrackingReportsFlow(deviceId)
                .firstThenDebounce(500)
                .collect {
                    handleReports(it)
                }
        }
    }

    private fun handleReports(userSleepTrackingReports: List<UserSleepTrackingReport>) {
        if (userSleepTrackingReports.isEmpty()) return

        val context = MediJourney.getAppContext()
        var selectedDate: Date? = null
        val entries = mutableListOf<BarEntry>()
        val colors = mutableListOf<Int>()

        userSleepTrackingReports.forEachIndexed { index, userSleepTrackingReport ->
            if (!userSleepTrackingReport.isValid()) return@forEachIndexed
            val reportedAt = userSleepTrackingReport.reportedAt ?: return@forEachIndexed

            if (index == 0 && this.selectedDate.value == null) {
                selectedDate = DateHelper.convertRealmInstantToDate(reportedAt)
            }
            if (limitLine == null) {
                limitLine = generateLimitLine(userSleepTrackingReport.healthyStageDuration)
            }

            val x = index.toFloat()
            val y = userSleepTrackingReport.sleepDuration.toFloat()
            val barEntry = BarEntry(x, y, userSleepTrackingReport)
            entries.add(barEntry)

            val color = if (userSleepTrackingReport.sleepHealthStatus == "good") {
                ResourcesCompat.getColor(context.resources, R.color.light_bar_chart_green_color, null)
            } else {
                ResourcesCompat.getColor(context.resources, R.color.primary_red_color, null)
            }
            colors.add(color)
        }

        val barDataSet = BarDataSet(entries, "")
        barDataSet.colors = colors
        val barData = BarData(barDataSet).apply {
            setDrawValues(false)
            barWidth = 0.5f

        }
        visibleXRangeMaximum = if (entries.size < 5) {
             entries.size.toFloat()
        } else {
            5f
        }

        barChartData.postValue(barData)
        selectedDate?.let {
            this.selectedDate.postValue(it)
        }
    }

    private fun generateLimitLine(value: Double): LimitLine {
        val context = MediJourney.getAppContext()
        val limitLine = LimitLine(value.toFloat(), "")
        limitLine.lineWidth = 1f
        limitLine.enableDashedLine(10f, 2f,0f)
        limitLine.lineColor = ResourcesCompat.getColor(context.resources, R.color.deep_turquoise_blue_color, null)

        return limitLine
    }

    fun getHighlight(): Highlight? {
        val selectedDate = selectedDate.value ?: return null
        val barData = barChartData.value ?: return null
        val dataSet = barData.dataSets.firstOrNull() ?: return null

        for (i in 0 until dataSet.entryCount) {
            val data = dataSet.getEntryForIndex(i).data as? UserSleepTrackingReport ?: continue
            if (!data.isValid()) continue
            val reportedAt = data.reportedAt ?: continue
            if (!DateHelper.compareEqualDates(
                    DateHelper.convertRealmInstantToDate(reportedAt),
                    selectedDate,
                    setOf(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH))
                ) continue

            val entry = dataSet.getEntryForIndex(i)
            return Highlight(entry.x, entry.y, 0)
        }
        return null
    }

    fun updateSelectedDate(month: Int, year: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        selectedDate.postValue(calendar.time)
    }

    @Suppress("UNCHECKED_CAST")
    fun handleValueSelected(e: Entry?) {
        val data = e?.data as? UserSleepTrackingReport ?: return
        if (!data.isValid()) return
        val attributes = currentAppSleepTrackingReportDetails["attributes"] as? List<Map<String, Any>> ?: return
        val segments = mutableListOf<Segment>()
        val itemModels = mutableListOf<DynamicUIItem>()
        val unit = "H"

        val totalSleepCycleDuration = (data.n1StageDuration +
                data.n2StageDuration +
                data.n3StageDuration +
                data.remStageDuration).toFloat()
        if (totalSleepCycleDuration == 0f) return

        for (attribute in attributes) {
            val item = DynamicUIItem.fromMap(attribute)
            var segmentPercentage = 0.0
            var segmentColor = R.color.white

            when (item.itemTag) {
                "n1_stage" -> {
                    segmentPercentage = data.n1StageDuration / totalSleepCycleDuration
                    segmentColor = R.color.sunrise_orange_color
                    item.description = MTextStyle( data.n1StageDuration.toString() + unit)
                }
                "n2_stage" -> {
                    segmentPercentage = data.n2StageDuration / totalSleepCycleDuration
                    segmentColor = R.color.ocean_blue_color
                    item.description = MTextStyle( data.n2StageDuration.toString() + unit)
                }
                "n3_stage" -> {
                    segmentPercentage = data.n3StageDuration / totalSleepCycleDuration
                    segmentColor = R.color.forest_green_color
                    item.description = MTextStyle(data.n3StageDuration.toString() + unit)
                }
                "rem_stage" -> {
                    segmentPercentage = data.remStageDuration / totalSleepCycleDuration
                    segmentColor = R.color.lavender_purple_color
                    item.description = MTextStyle(data.remStageDuration.toString() + unit)
                }
            }
            segments.add(Segment(segmentPercentage.toFloat(), segmentColor))
            item.image?.color = segmentColor
            item.padding = EdgePadding(0, 0, 0, 0)
            item.additionalData = mapOf(HDualImageTextViewHolder.IS_HIDE_SEPARATOR_KEY to true)
            itemModels.add(item)
        }

        this.segments.postValue(segments)
        this.itemModels.postValue(itemModels)
        description.postValue(data.description)
    }
}