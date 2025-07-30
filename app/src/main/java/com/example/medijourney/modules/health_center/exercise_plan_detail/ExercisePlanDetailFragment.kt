package com.example.medijourney.modules.health_center.exercise_plan_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.helpers.FragmentHelper
import com.example.medijourney.common.ui_components.composes.HorizontalRow
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler

class ExercisePlanDetailFragment : Fragment(), MenuProvider {

    // Properties
    private val viewModel: ExercisePlanDetailViewModel by viewModels()
    private val args : ExercisePlanDetailFragmentArgs by navArgs()
    private val userExercisePlanId: String by lazy { args.userExercisePlanId }

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentHelper.createBaseComposeView(inflater, container) {
            ExercisePlanDetail(viewModel)
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.edit_remove_action_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_edit -> {
                openAddUserExercisePlanFragment()
            }
            R.id.action_delete -> {
                deleteUserPlanExercise()
            }
            else -> {
                findNavController().popBackStack()
            }
        }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewModel.onViewCreated(userExercisePlanId)
    }

    // Functions
    private fun setupView() {
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    private fun openAddUserExercisePlanFragment() {
        val action = ExercisePlanDetailFragmentDirections.actionExercisePlanDetailFragmentToAddExercisePlanFragment()
        action.userExercisePlanId = userExercisePlanId
        findNavController().navigate(action)
    }

    private fun deleteUserPlanExercise() {
        IndicatorHandler.show(requireContext())
        viewModel.deleteUserExercisePlanId {
            IndicatorHandler.hide()
            findNavController().popBackStack()
        }
    }
}

@Composable
fun ExercisePlanDetail(viewModel: ExercisePlanDetailViewModel) {

    // Properties
    val planInfoList by viewModel.planInfoList.collectAsState()

    // Content
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(R.string.exercise_plan_information),
            fontFamily = proximaNovaFamily,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            fontSize = TextUnit(16f, TextUnitType.Sp)
        )

        Card(
            modifier = Modifier.padding(top = 16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            LazyColumn (
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(planInfoList) { info ->
                    HorizontalRow(info)
                }
            }
        }
        }
}