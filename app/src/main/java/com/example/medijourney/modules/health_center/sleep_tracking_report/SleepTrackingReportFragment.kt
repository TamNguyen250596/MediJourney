package com.example.medijourney.modules.health_center.sleep_tracking_report

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medijourney.R
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.helpers.charts.ChartHelper
import com.example.medijourney.common.helpers.charts.DayAxisValueFormatter
import com.example.medijourney.common.ui_components.recycle_view_adapter.h_dual_image_text_view.HDualImageTextViewAdapter
import com.example.medijourney.databinding.FragmentSleepTrackingReportBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.util.Date

class SleepTrackingReportFragment : Fragment(), OnChartValueSelectedListener {

    // Properties
    private val viewModel: SleepTrackingReportViewModel by viewModels()
    private lateinit var binding: FragmentSleepTrackingReportBinding
    private val dateFormat = "dd/MM/yyyy"
    private val args : SleepTrackingReportFragmentArgs by navArgs()
    private val userFitnessTrackerId: String? by lazy { args.userFitnessTrackerId }

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSleepTrackingReportBinding.inflate(inflater, container, false)
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
        ChartHelper.setupBarChart(binding.barChart)
        binding.barChart.setOnChartValueSelectedListener(this)
        ChartHelper.setupXAxis(binding.barChart.xAxis)
        binding.barChart.xAxis.valueFormatter = DayAxisValueFormatter(binding.barChart, dateFormat)
        ChartHelper.setupAxisRight(binding.barChart.axisRight)
        binding.recycleView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
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
            viewModel.getHighlight()?.let { highlight ->
                binding.barChart.highlightValue(highlight, true)
                binding.barChart.moveViewToX(highlight.x)
            }
        }
        viewModel.barChartData.observe(viewLifecycleOwner) {
            binding.barChart.data = it
            binding.barChart.setVisibleXRangeMaximum(viewModel.visibleXRangeMaximum)
            viewModel.limitLine?.let { limitLine ->
                binding.barChart.axisRight.removeAllLimitLines()
                binding.barChart.axisRight.addLimitLine(limitLine)
            }
            binding.barChart.invalidate()
        }
        viewModel.segments.observe(viewLifecycleOwner) {
            binding.multiSegmentProgressBar.setup(it)
            binding.multiSegmentProgressBar.visibility = View.VISIBLE
        }
        viewModel.itemModels.observe(viewLifecycleOwner) {
            val adapter = binding.recycleView.adapter as? HDualImageTextViewAdapter
            if (adapter == null) {
                val newAdapter = HDualImageTextViewAdapter(it)
                binding.recycleView.adapter = newAdapter
            } else {
                adapter.updateItems(it)
            }
            binding.recycleView.visibility = View.VISIBLE
        }
        viewModel.description.observe(viewLifecycleOwner) {
            binding.descriptionTextView.text = it
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
    override fun onValueSelected(e: Entry?, h: Highlight?) {
        viewModel.handleValueSelected(e)
    }

    override fun onNothingSelected() {
        binding.multiSegmentProgressBar.visibility = View.GONE
        binding.recycleView.visibility = View.GONE
        binding.descriptionTextView.text = getString(R.string.na)
    }
}