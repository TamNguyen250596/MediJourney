package com.example.medijourney.modules.health_center.nutrition_tracking_report

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.helpers.MediJourney
import com.example.medijourney.common.managers.AppNutritionTrackingReportDetails
import com.example.medijourney.common.managers.InternationManager
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.observe
import com.example.medijourney.common.managers.realm.Operator
import com.example.medijourney.common.managers.realm.RQuery
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.UserFitnessTracker
import com.example.medijourney.common.models.realm_models.UserNutritionTrackingReport
import com.example.medijourney.common.models.ui_models.EdgePadding
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.UserFitnessTrackerRepository
import com.example.medijourney.common.respositories.UserNutritionTrackingReportRepository
import com.example.medijourney.common.ui_components.recycle_view_adapter.h_dual_image_text_view.HDualImageTextViewHolder
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NutritionTrackingReportViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    @AppNutritionTrackingReportDetails private val appNutritionTrackingReportDetails: Map<String, *>,
    private val userFitnessTrackerRepository: UserFitnessTrackerRepository,
    private val userNutritionTrackingReportRepository: UserNutritionTrackingReportRepository
    ) : ViewModel() {

    // Properties
    var selectedDate = MutableLiveData<Date?>()
    var calorieChartData = MutableLiveData<BarData>()
    var calorieVisibleXRangeMaximum = 5f
    var macronutrientsItemModels = MutableLiveData<MutableList<DynamicUIItem>>()
    var macronutrientsDescription = MutableLiveData<String?>()
    var waterChartData = MutableLiveData<BarData>()
    var waterVisibleXRangeMaximum = 5f
    var waterDescription = MutableLiveData<String?>()
    var micronutrientsChartData = MutableLiveData<BarData>()
    val micronutrientsAxisMinimum: Float = 0f
    var micronutrientsAxisMaximum: Float? = null
    var micronutrientsVisibleXRangeMaximum = 5f
    var micronutrientsItemModels = MutableLiveData<MutableList<DynamicUIItem>>()
    var micronutrientsDescription = MutableLiveData<String?>()

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
            userNutritionTrackingReportRepository.observeUserNutritionTrackingReports(deviceId)
        }
        launch {
            userNutritionTrackingReportRepository
                .getUserNutritionTrackingReportsFlow(deviceId)
                .firstThenDebounce(500)
                .collect {
                    handleReports(it)
                }
        }
    }

    fun updateSelectedDate(month: Int, year: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        selectedDate.postValue(calendar.time)
    }

    private fun handleReports(userNutritionTrackingReports: List<UserNutritionTrackingReport>) {
        if (userNutritionTrackingReports.isEmpty()) return

        val context = MediJourney.getAppContext()
        var selectedDate: Date? = null
        val calorieEntries = mutableListOf<BarEntry>()
        val calorieColors = mutableListOf<Int>()
        val waterEntries = mutableListOf<BarEntry>()
        val waterColors = mutableListOf<Int>()
        val vitaminEntries = mutableListOf<BarEntry>()
        val vitaminColors = mutableListOf<Int>()
        val mineralEntries = mutableListOf<BarEntry>()
        val mineralColors = mutableListOf<Int>()

        userNutritionTrackingReports.forEachIndexed { index, userNutritionTrackingReport ->
            if (!userNutritionTrackingReport.isValid()) return@forEachIndexed
            val reportedAt = userNutritionTrackingReport.reportedAt ?: return@forEachIndexed

            if (index == 0 && this.selectedDate.value == null) {
                selectedDate = DateHelper.convertRealmInstantToDate(reportedAt)
            }

            val calorieEntry = generateCalorieEntry(index, userNutritionTrackingReport)
            calorieEntries.add(calorieEntry)
            val calorieColor = generateCalorieColor(context, userNutritionTrackingReport)
            calorieColors.add(calorieColor)

            val waterEntry = generateWaterEntry(index, userNutritionTrackingReport)
            waterEntries.add(waterEntry)
            val waterColor = generateWaterColor(context, userNutritionTrackingReport)
            waterColors.add(waterColor)

            val vitaminEntry = generateVitaminIntakeEntry(index, userNutritionTrackingReport)
            vitaminEntries.add(vitaminEntry)
            val vitaminColor = generateVitaminIntakeColors(context, userNutritionTrackingReport)
            vitaminColors.add(vitaminColor)

            val mineralEntry = generateMineralIntakeEntry(index, userNutritionTrackingReport)
            mineralEntries.add(mineralEntry)
            val mineralColor = generateMineralIntakeColor(context, userNutritionTrackingReport)
            mineralColors.add(mineralColor)
        }

        handleCalorieEntries(calorieEntries, calorieColors)
        handleWaterEntries(waterEntries, waterColors)
        handleMicronutrientsEntries(vitaminEntries, vitaminColors, mineralEntries, mineralColors)
        selectedDate?.let {
            this.selectedDate.postValue(it)
        }
    }

    // Calorie Bar Chart
    private fun generateCalorieEntry(index: Int, userNutritionTrackingReport: UserNutritionTrackingReport): BarEntry {
        val x = index.toFloat()
        val y = userNutritionTrackingReport.calorieIntake.toFloat()
        return BarEntry(x, y, userNutritionTrackingReport)
    }

    private fun generateCalorieColor(context: Context, userNutritionTrackingReport: UserNutritionTrackingReport): Int {
        return if (userNutritionTrackingReport.calorieIntakeStatus == "good") {
            ResourcesCompat.getColor(context.resources, R.color.light_bar_chart_green_color, null)
        } else {
            ResourcesCompat.getColor(context.resources, R.color.primary_red_color, null)
        }
    }

    private fun handleCalorieEntries(calorieEntries: MutableList<BarEntry>, calorieColors: MutableList<Int>) {
        val calorieBarDataSet = BarDataSet(calorieEntries, "")
        calorieBarDataSet.colors = calorieColors
        val calorieBarData = BarData(calorieBarDataSet).apply {
            setDrawValues(false)
            barWidth = 0.5f

        }
        calorieVisibleXRangeMaximum = if (calorieEntries.size < 5) {
            calorieEntries.size.toFloat()
        } else {
            5f
        }

        calorieChartData.postValue(calorieBarData)
    }

    fun getCalorieHighlight(): Highlight? {
        val selectedDate = selectedDate.value ?: return null
        val calorieBarData = calorieChartData.value ?: return null
        val dataSet = calorieBarData.dataSets.firstOrNull() ?: return null

        for (i in 0 until dataSet.entryCount) {
            val data = dataSet.getEntryForIndex(i).data as? UserNutritionTrackingReport ?: continue
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

    @Suppress("UNCHECKED_CAST")
    fun handleValueSelectedAtCalorieChart(e: Entry?) {
        val data = e?.data as? UserNutritionTrackingReport ?: return
        if (!data.isValid()) return
        val macronutrients = appNutritionTrackingReportDetails["macronutrients"] as? List<Map<String, Any>> ?: return
        val unit = "mg"

        val items = macronutrients.map {
            val item = DynamicUIItem.fromMap(it)

            when(item.itemTag) {
                "carbohydrates" -> {
                    item.description = MTextStyle(data.carbohydratesIntake.toString() + unit)
                }
                "lipids" -> {
                    item.description = MTextStyle(data.lipidsIntake.toString() + unit)
                }
                "proteins" -> {
                    item.description = MTextStyle(data.proteinsIntake.toString() + unit)
                }
            }
            item.image?.size = Pair(24, 24)
            item.padding = EdgePadding(0, 0, 0, 0)
            item.additionalData = mapOf(HDualImageTextViewHolder.IS_HIDE_SEPARATOR_KEY to true)
            return@map item
        }
        macronutrientsItemModels.postValue(items.toMutableList())
        macronutrientsDescription.postValue(data.macronutrientsIntakeDescription)
    }

    // Water Bar Chart
    private fun generateWaterEntry(index: Int, userNutritionTrackingReport: UserNutritionTrackingReport): BarEntry {
        val x = index.toFloat()
        val y = userNutritionTrackingReport.calorieIntake.toFloat()
        return BarEntry(x, y, userNutritionTrackingReport)
    }

    private fun generateWaterColor(context: Context, userNutritionTrackingReport: UserNutritionTrackingReport): Int {
        return if (userNutritionTrackingReport.waterIntakeStatus == "good") {
            ResourcesCompat.getColor(context.resources, R.color.light_bar_chart_green_color, null)
        } else {
            ResourcesCompat.getColor(context.resources, R.color.primary_red_color, null)
        }
    }

    private fun handleWaterEntries(waterEntries: MutableList<BarEntry>, waterColors: MutableList<Int>) {
        val waterBarDataSet = BarDataSet(waterEntries, "")
        waterBarDataSet.colors = waterColors
        val waterBarData = BarData(waterBarDataSet).apply {
            setDrawValues(false)
            barWidth = 0.5f
        }
        waterVisibleXRangeMaximum = if (waterEntries.size < 5) {
            waterEntries.size.toFloat()
        } else {
            5f
        }
        waterChartData.postValue(waterBarData)
    }

    fun getWaterHighlight(): Highlight? {
        val selectedDate = selectedDate.value ?: return null
        val waterBarData = waterChartData.value ?: return null
        val dataSet = waterBarData.dataSets.firstOrNull() ?: return null

        for (i in 0 until dataSet.entryCount) {
            val data = dataSet.getEntryForIndex(i).data as? UserNutritionTrackingReport ?: continue
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

    fun handleValueSelectedAtWaterChart(e: Entry?) {
        val data = e?.data as? UserNutritionTrackingReport ?: return
        if (!data.isValid()) return

        waterDescription.postValue(data.waterIntakeDescription)
    }

    // Micronutrients Bar Chart
    private fun generateVitaminIntakeEntry(index: Int, userNutritionTrackingReport: UserNutritionTrackingReport): BarEntry {
        val x = index.toFloat()
        val y = userNutritionTrackingReport.vitaminIntake.toFloat()
        return BarEntry(x, y, userNutritionTrackingReport)
    }

    private fun generateVitaminIntakeColors(context: Context, userNutritionTrackingReport: UserNutritionTrackingReport): Int {
        return if (userNutritionTrackingReport.vitaminIntakeStatus == "good") {
            ResourcesCompat.getColor(context.resources, R.color.light_bar_chart_green_color, null)
        } else {
            ResourcesCompat.getColor(context.resources, R.color.primary_red_color, null)
        }
    }

    private fun generateMineralIntakeEntry(index: Int, userNutritionTrackingReport: UserNutritionTrackingReport): BarEntry {
        val x = index.toFloat()
        val y = userNutritionTrackingReport.mineralIntake.toFloat()
        return BarEntry(x, y, userNutritionTrackingReport)
    }

    private fun generateMineralIntakeColor(context: Context, userNutritionTrackingReport: UserNutritionTrackingReport): Int {
        return if (userNutritionTrackingReport.mineralIntakeStatus == "good") {
            ResourcesCompat.getColor(context.resources, R.color.light_bar_chart_green_color, null)
        } else {
            ResourcesCompat.getColor(context.resources, R.color.primary_red_color, null)
        }
    }

    private fun handleMicronutrientsEntries(vitaminEntries: MutableList<BarEntry>,
                                            vitaminColors: MutableList<Int>,
                                            mineralEntries: MutableList<BarEntry>,
                                            mineralColors: MutableList<Int>) {
        val groupSpace = 0.8f
        val barSpace = 0.3f

        val vitaminDataSet = BarDataSet(vitaminEntries, "")
        vitaminDataSet.colors = vitaminColors
        val mineralDataSet = BarDataSet(mineralEntries, "")
        mineralDataSet.colors = mineralColors
        val sum = vitaminEntries.size + mineralEntries.size

        val micronutrientsBarData = BarData(vitaminDataSet, mineralDataSet).apply {
            setDrawValues(false)
            barWidth = 0.5f
            groupBars(micronutrientsAxisMinimum, groupSpace, barSpace)
        }

        micronutrientsAxisMaximum = micronutrientsAxisMinimum + micronutrientsBarData.getGroupWidth(groupSpace, barSpace) * (sum + 1)
        micronutrientsVisibleXRangeMaximum = if (sum < 5) {
            sum.toFloat()
        } else {
            5f
        }

        micronutrientsChartData.postValue(micronutrientsBarData)
    }

    @Suppress("UNCHECKED_CAST")
    fun handleValueSelectedAtMicronutrientsChartData(e: Entry?, h: Highlight?) {
        val data = e?.data as? UserNutritionTrackingReport ?: return
        if (!data.isValid()) return
        val highlight = h ?: return
        val micronutrients = appNutritionTrackingReportDetails["micronutrients"] as? Map<String, Any> ?: return

        when (highlight.dataSetIndex) {
            0 -> handleMicronutrientItems(data, micronutrients, "vitamins", data.vitaminIntakeDescription)
            1 -> handleMicronutrientItems(data, micronutrients, "minerals", data.mineralIntakeDescription)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleMicronutrientItems(
        data: UserNutritionTrackingReport,
        micronutrients: Map<String, Any>,
        category: String,
        intakeDescription: String?
    ) {
        if (!data.isValid()) return
        val itemsList = micronutrients[category] as? List<Map<String, Any>> ?: return
        val dataList = mutableListOf<DynamicUIItem>()

        for ((sectionIndex, attribute) in itemsList.withIndex()) {
            val items = attribute["items"] as? List<Map<String, Any>> ?: continue

            val headerModel = DynamicUIItem.fromMap(attribute).apply {
                title?.font = R.font.proximanova_bold
                groupIndex = sectionIndex
                additionalData = mapOf("itemsSize" to items.size)
            }
            dataList.add(headerModel)

            items.forEach { item ->
                val itemModel = DynamicUIItem.fromMap(item).apply {
                    groupIndex = sectionIndex
                    description = MTextStyle(getMicronutrientIntake(data, itemTag))
                    image?.size = Pair(24, 24)
                    padding = EdgePadding(8, 8, 56, 0)
                }
                dataList.add(itemModel)
            }
        }
        micronutrientsItemModels.postValue(dataList)
        micronutrientsDescription.postValue(intakeDescription)
    }

    private fun getMicronutrientIntake(data: UserNutritionTrackingReport, itemTag: String): String {
        val context = MediJourney.getAppContext()
        val unit = " mg"

        val micronutrientKeys = mapOf(
            "vitamin_b1" to data.vitaminB1Intake,
            "vitamin_b2" to data.vitaminB2Intake,
            "vitamin_b3" to data.vitaminB3Intake,
            "vitamin_b5" to data.vitaminB5Intake,
            "vitamin_b6" to data.vitaminB6Intake,
            "vitamin_b7" to data.vitaminB7Intake,
            "vitamin_b9" to data.vitaminB9Intake,
            "vitamin_b12" to data.vitaminB12Intake,
            "vitamin_c" to data.vitaminCIntake,
            "vitamin_a" to data.vitaminAIntake,
            "vitamin_d" to data.vitaminDIntake,
            "vitamin_e" to data.vitaminEIntake,
            "vitamin_k" to data.vitaminKIntake,
            "calcium" to data.calciumIntake,
            "phosphorous" to data.phosphorousIntake,
            "magnesium" to data.magnesiumIntake,
            "sodium" to data.sodiumIntake,
            "potassium" to data.potassiumIntake,
            "chloride" to data.chlorideIntake,
            "iron" to data.ironIntake,
            "copper" to data.copperIntake,
            "zinc" to data.zincIntake,
            "selenium" to data.seleniumIntake,
            "iodine" to data.iodineIntake
        )
        return micronutrientKeys[itemTag]?.let {
            it.toString() + unit
        } ?: run {
            context.getString(R.string.na)
        }
    }
}