package com.example.medijourney.modules.health_center.exercise_tracking_report

import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.helpers.MediJourney
import com.example.medijourney.common.managers.AppExerciseTrackingReportDetails
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.UserExerciseTrackingReport
import com.example.medijourney.common.models.ui_models.EdgePadding
import com.example.medijourney.common.models.ui_models.Segment
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.UserExerciseTrackingReportRepo
import com.example.medijourney.common.respositories.UserFitnessTrackerRepo
import com.example.medijourney.common.ui_components.recycle_view_adapter.h_dual_image_text_view.HDualImageTextViewHolder
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
class ExerciseTrackingReportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @AppExerciseTrackingReportDetails private val currentAppExerciseTrackingReportDetails: Map<String, *>,
    private val userFitnessTrackerRepository: UserFitnessTrackerRepo,
    private val userExerciseTrackingReportRepository: UserExerciseTrackingReportRepo
) : ViewModel() {

    // Properties
    var selectedDate = MutableLiveData<Date?>()
    var barChartData = MutableLiveData<BarData>()
    var segments = MutableLiveData<List<Segment>>()
    var itemModels = MutableLiveData<MutableList<DynamicUIItem>>()
    var description = MutableLiveData<String?>()
    var visibleXRangeMaximum = 5f

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
            userExerciseTrackingReportRepository.observeUserExerciseTrackingReports(deviceId)
        }
        launch {
            userExerciseTrackingReportRepository
                .getUserExerciseTrackingReportsFlow(deviceId)
                .firstThenDebounce(500)
                .collect {
                    handleReports(it)
                }
        }
    }

    private fun handleReports(userExerciseTrackingReports: List<UserExerciseTrackingReport>) {
        if (userExerciseTrackingReports.isEmpty()) return

        val context = MediJourney.getAppContext()
        var selectedDate: Date? = null
        val entries = mutableListOf<BarEntry>()
        val colors = mutableListOf<Int>()

        userExerciseTrackingReports.forEachIndexed { index, userExerciseTrackingReport ->
            if (!userExerciseTrackingReport.isValid()) return@forEachIndexed
            val reportedAt = userExerciseTrackingReport.reportedAt ?: return@forEachIndexed

            if (index == 0 && this.selectedDate.value == null) {
                selectedDate = DateHelper.convertRealmInstantToDate(reportedAt)
            }

            val x = index.toFloat()
            val y = userExerciseTrackingReport.burnedCalories.toFloat()
            val barEntry = BarEntry(x, y, userExerciseTrackingReport)
            entries.add(barEntry)
            val color = ResourcesCompat.getColor(context.resources, R.color.light_bar_chart_green_color, null)
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

    fun getHighlight(): Highlight? {
        val selectedDate = selectedDate.value ?: return null
        val barData = barChartData.value ?: return null
        val dataSet = barData.dataSets.firstOrNull() ?: return null

        for (i in 0 until dataSet.entryCount) {
            val data = dataSet.getEntryForIndex(i).data as? UserExerciseTrackingReport ?: continue
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
        val data = e?.data as? UserExerciseTrackingReport ?: return
        if (!data.isValid()) return
        val attributes = currentAppExerciseTrackingReportDetails["attributes"] as? List<Map<String, Any>> ?: return
        val segments = mutableListOf<Segment>()
        val itemModels = mutableListOf<DynamicUIItem>()
        val unit = "min"

        val totalDuration = (data.absExerciseDuration +
                data.chestExerciseDuration +
                data.armsExerciseDuration +
                data.legsExerciseDuration +
                data.shoulderBackExerciseDuration).toFloat()
        if (totalDuration == 0f) return

        for (attribute in attributes) {
            val item = DynamicUIItem.fromMap(attribute)
            var segmentPercentage = 0.0
            var segmentColor = R.color.white

            when (item.itemTag) {
                "abs_exercise_duration" -> {
                    segmentPercentage = data.absExerciseDuration / totalDuration
                    segmentColor = R.color.sunrise_orange_color
                    item.description = MTextStyle( data.absExerciseDuration.toString() + unit)
                }
                "chest_exercise_duration" -> {
                    segmentPercentage = data.chestExerciseDuration / totalDuration
                    segmentColor = R.color.ocean_blue_color
                    item.description = MTextStyle( data.chestExerciseDuration.toString() + unit)
                }
                "arms_exercise_duration" -> {
                    segmentPercentage = data.armsExerciseDuration / totalDuration
                    segmentColor = R.color.forest_green_color
                    item.description = MTextStyle(data.absExerciseDuration.toString() + unit)
                }
                "legs_exercise_duration" -> {
                    segmentPercentage = data.legsExerciseDuration / totalDuration
                    segmentColor = R.color.lavender_purple_color
                    item.description = MTextStyle(data.legsExerciseDuration.toString() + unit)
                }
                "shoulder_back_exercise_duration" -> {
                    segmentPercentage = data.shoulderBackExerciseDuration / totalDuration
                    segmentColor = R.color.primary_red_color
                    item.description = MTextStyle(data.shoulderBackExerciseDuration.toString() + unit)
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
        description.postValue(data.exerciseDescription)
    }
}