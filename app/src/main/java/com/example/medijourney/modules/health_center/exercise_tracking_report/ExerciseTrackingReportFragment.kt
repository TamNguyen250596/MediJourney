package com.example.medijourney.modules.health_center.exercise_tracking_report

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medijourney.R
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.helpers.charts.ChartHelper
import com.example.medijourney.common.helpers.charts.DayAxisValueFormatter
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.models.realm_models.Exercise
import com.example.medijourney.common.ui_components.recycle_view_adapter.h_dual_image_text_view.HDualImageTextViewAdapter
import com.example.medijourney.databinding.FragmentExerciseTrackingReportBinding
import com.example.medijourney.modules.health_center.main.sub_ui.recommend_exercises.RecommendedExerciseList
import com.example.medijourney.modules.health_center.main.sub_ui.recommend_exercises.RecommendedExerciseViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.ext.isValid
import java.util.Date

@AndroidEntryPoint
class ExerciseTrackingReportFragment : Fragment(), OnChartValueSelectedListener {

    // Properties
    private val exerciseTrackingReportViewModel: ExerciseTrackingReportViewModel by viewModels()
    private val recommendedExerciseViewModel: RecommendedExerciseViewModel by viewModels()
    private lateinit var binding: FragmentExerciseTrackingReportBinding
    private val dateFormat = "dd/MM/yyyy"
    private val args : ExerciseTrackingReportFragmentArgs by navArgs()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExerciseTrackingReportBinding.inflate(inflater, container, false)
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                RecommendedExerciseList(recommendedExerciseViewModel, onClick = {
                    openSelectExerciseLevelFragment(it)
                })
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeUIComponents()
        observeViewModel()
        recommendedExerciseViewModel.onViewCreated()
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
        exerciseTrackingReportViewModel.selectedDate.observe(viewLifecycleOwner) {
            val date = it ?: return@observe

            binding.datePickerButton.text = DateHelper.convertDateToString(date, dateFormat)
            exerciseTrackingReportViewModel.getHighlight()?.let { highlight ->
                binding.barChart.highlightValue(highlight, true)
                binding.barChart.moveViewToX(highlight.x)
            }
        }
        exerciseTrackingReportViewModel.barChartData.observe(viewLifecycleOwner) {
            binding.barChart.data = it
            binding.barChart.setVisibleXRangeMaximum(exerciseTrackingReportViewModel.visibleXRangeMaximum)
            binding.barChart.invalidate()
        }
        exerciseTrackingReportViewModel.segments.observe(viewLifecycleOwner) {
            binding.multiSegmentProgressBar.setup(it)
            binding.multiSegmentProgressBar.visibility = View.VISIBLE
        }
        exerciseTrackingReportViewModel.itemModels.observe(viewLifecycleOwner) {
            val adapter = binding.recycleView.adapter as? HDualImageTextViewAdapter
            if (adapter == null) {
                val newAdapter = HDualImageTextViewAdapter(it)
                binding.recycleView.adapter = newAdapter
            } else {
                adapter.updateItems(it)
            }
            binding.recycleView.visibility = View.VISIBLE
        }
        exerciseTrackingReportViewModel.description.observe(viewLifecycleOwner) {
            binding.descriptionTextView.text = it
        }
    }

    // Router
    private fun showDatePicker() {
        val selectedDate = exerciseTrackingReportViewModel.selectedDate.value ?: Date()
        val dateComponent = DateHelper.convertDateToDateComponent(selectedDate)

        val datePicker = DatePickerDialog(requireContext())
        datePicker.updateDate(dateComponent.year, dateComponent.month, dateComponent.day)
        datePicker.setOnDateSetListener { _, year, month, dayOfMonth ->
            exerciseTrackingReportViewModel.updateSelectedDate(month, year, dayOfMonth)
        }
        datePicker.show()
    }

    private fun openSelectExerciseLevelFragment(itemModel: ImageItemModel) {
        val exercise = itemModel.data as? Exercise ?: return
        if (!exercise.isValid()) return

        val action = ExerciseTrackingReportFragmentDirections.actionExerciseTrackingReportFragmentToSelectExerciseLevelFragment()
        action.exerciseId = exercise.id
        action.showAppBar = true
        findNavController().navigate(action)
    }

    // OnChartValueSelectedListener
    override fun onValueSelected(e: Entry?, h: Highlight?) {
        exerciseTrackingReportViewModel.handleValueSelected(e)
    }

    override fun onNothingSelected() {
        binding.multiSegmentProgressBar.visibility = View.GONE
        binding.recycleView.visibility = View.GONE
        binding.descriptionTextView.text = getString(R.string.na)
    }
}
