package com.example.medijourney.modules.health_center.nutrition_tracking_report

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medijourney.R
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.helpers.charts.ChartHelper
import com.example.medijourney.common.helpers.charts.DayAxisValueFormatter
import com.example.medijourney.common.ui_components.recycle_view_adapter.h_dual_image_text_view.HDualImageTextViewAdapter
import com.example.medijourney.databinding.FragmentNutritionTrackingReportBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.util.Date

class NutritionTrackingReportFragment : Fragment() {

    // Properties
    private val viewModel: NutritionTrackingReportViewModel by viewModels()
    private lateinit var binding: FragmentNutritionTrackingReportBinding
    private val dateFormat = "dd/MM/yyyy"
    private val args : NutritionTrackingReportFragmentArgs by navArgs()
    private val userFitnessTrackerId: String? by lazy { args.userFitnessTrackerId }

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNutritionTrackingReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeUIComponents()
        observeViewModel()
        viewModel.onCreateView(userFitnessTrackerId)
    }

    // Functions
    private fun setupView() {
        binding.macronutrientsTextView.typeface = ResourcesCompat.getFont(requireContext(), R.font.proximanova_bold)
        ChartHelper.setupBarChart(binding.calorieBarChart)
        binding.calorieBarChart.setOnChartValueSelectedListener(selectedCalorieChartListener)
        ChartHelper.setupXAxis(binding.calorieBarChart.xAxis)
        binding.calorieBarChart.xAxis.valueFormatter = DayAxisValueFormatter(binding.calorieBarChart, dateFormat)
        ChartHelper.setupAxisRight(binding.calorieBarChart.axisRight)
        binding.macronutrientsRecycleView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.waterTextView.typeface = ResourcesCompat.getFont(requireContext(), R.font.proximanova_bold)
        ChartHelper.setupBarChart(binding.waterBarChart)
        ChartHelper.setupXAxis(binding.waterBarChart.xAxis)
        binding.waterBarChart.xAxis.valueFormatter = DayAxisValueFormatter(binding.waterBarChart, dateFormat)
        binding.waterBarChart.setOnChartValueSelectedListener(selectedWaterChartListener)
        ChartHelper.setupAxisRight(binding.waterBarChart.axisRight)

        binding.micronutrientsTextView.typeface = ResourcesCompat.getFont(requireContext(), R.font.proximanova_bold)
        ChartHelper.setupBarChart(binding.micronutrientsChart)
        binding.micronutrientsChart.setOnChartValueSelectedListener(selectedMicronutrientsChartListener)
        ChartHelper.setupXAxis(binding.micronutrientsChart.xAxis)
        binding.micronutrientsChart.xAxis.valueFormatter = DayAxisValueFormatter(binding.micronutrientsChart, dateFormat)
        ChartHelper.setupAxisRight(binding.micronutrientsChart.axisRight)
        binding.micronutrientsRecycleView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun observeUIComponents() {
        binding.datePickerButton.setOnClickListener {
            showDatePicker()
        }
    }

    private fun observeViewModel() {
        viewModel.selectedDate.observe(viewLifecycleOwner) {
            val date = it ?: return@observe

            binding.datePickerButton.text = DateHelper.convertDateToString(date, dateFormat)
            viewModel.getCalorieHighlight()?.let { highlight ->
                binding.calorieBarChart.highlightValue(highlight, true)
                binding.calorieBarChart.moveViewToX(highlight.x)
            }
            viewModel.getWaterHighlight()?.let { highlight ->
                binding.waterBarChart.highlightValue(highlight, true)
                binding.waterBarChart.moveViewToX(highlight.x)
            }
        }

        viewModel.calorieChartData.observe(viewLifecycleOwner) {
            binding.calorieBarChart.data = it
            binding.calorieBarChart.setVisibleXRangeMaximum(viewModel.calorieVisibleXRangeMaximum)
            binding.calorieBarChart.invalidate()
        }
        viewModel.macronutrientsItemModels.observe(viewLifecycleOwner) {
            val adapter = binding.macronutrientsRecycleView.adapter as? HDualImageTextViewAdapter
            if (adapter == null) {
                val newAdapter = HDualImageTextViewAdapter(it)
                binding.macronutrientsRecycleView.adapter = newAdapter
            } else {
                adapter.updateItems(it)
            }
            binding.macronutrientsRecycleView.visibility = View.VISIBLE
        }
        viewModel.macronutrientsDescription.observe(viewLifecycleOwner) {
            binding.macronutrientsDescriptionTextView.text = it
        }

        viewModel.waterChartData.observe(viewLifecycleOwner) {
            binding.waterBarChart.data = it
            binding.waterBarChart.setVisibleXRangeMaximum(viewModel.waterVisibleXRangeMaximum)
            binding.waterBarChart.invalidate()
        }
        viewModel.waterDescription.observe(viewLifecycleOwner) {
            binding.waterDescriptionTextView.text = it
        }

        viewModel.micronutrientsChartData.observe(viewLifecycleOwner) {
            binding.micronutrientsChart.xAxis.axisMinimum = viewModel.micronutrientsAxisMinimum
            viewModel.micronutrientsAxisMaximum?.let {
                binding.micronutrientsChart.xAxis.axisMaximum = it
            }
            binding.micronutrientsChart.data = it
            binding.micronutrientsChart.setVisibleXRangeMaximum(viewModel.micronutrientsVisibleXRangeMaximum)
            binding.micronutrientsChart.invalidate()
        }
        viewModel.micronutrientsItemModels.observe(viewLifecycleOwner) {
            var adapter = binding.micronutrientsRecycleView.adapter as? NutritionTrackingReportAdapter
            if (adapter == null) {
                adapter = NutritionTrackingReportAdapter()
                binding.micronutrientsRecycleView.adapter = adapter
            }
            adapter.submitList(it)
            binding.micronutrientsRecycleView.visibility = View.VISIBLE
        }
        viewModel.micronutrientsDescription.observe(viewLifecycleOwner) {
            binding.micronutrientsDescriptionTextView.text = it
        }
    }

    // Router
    private fun showDatePicker() {
        val selectedDate = viewModel.selectedDate.value ?: Date()
        val dateComponent = DateHelper.convertDateToDateComponent(selectedDate)

        val datePicker = DatePickerDialog(requireContext())
        datePicker.updateDate(dateComponent.year, dateComponent.month, dateComponent.day)
        datePicker.setOnDateSetListener { _, year, month, dayOfMonth ->
            viewModel.updateSelectedDate(month, year, dayOfMonth)
        }
        datePicker.show()
    }

    // OnChartValueSelectedListener
    private val selectedCalorieChartListener = object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry?, h: Highlight?) {
            viewModel.handleValueSelectedAtCalorieChart(e)
        }

        override fun onNothingSelected() {
            binding.macronutrientsRecycleView.visibility = View.GONE
            binding.macronutrientsDescriptionTextView.text = getString(R.string.na)
        }
    }

    private val selectedWaterChartListener = object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry?, h: Highlight?) {
            viewModel.handleValueSelectedAtWaterChart(e)
        }

        override fun onNothingSelected() {
            binding.waterDescriptionTextView.text = getString(R.string.na)
        }
    }

    private val selectedMicronutrientsChartListener = object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry?, h: Highlight?) {
            viewModel.handleValueSelectedAtMicronutrientsChartData(e, h)
        }

        override fun onNothingSelected() {
            binding.micronutrientsRecycleView.visibility = View.GONE
            binding.micronutrientsDescriptionTextView.text = getString(R.string.tap_bar_view_detailed_information)
        }
    }
}