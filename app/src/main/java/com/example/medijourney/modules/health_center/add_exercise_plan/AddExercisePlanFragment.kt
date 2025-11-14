package com.example.medijourney.modules.health_center.add_exercise_plan

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.helpers.FragmentHelper
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.ui_components.composes.SaveButton
import com.example.medijourney.common.ui_components.composes.TextFieldItem
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.modules.health_center.add_exercise_plan.sub_views.ElevatedButtonItem
import com.example.medijourney.modules.health_center.add_exercise_plan.sub_views.ExerciseSequenceItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddExercisePlanFragment : Fragment() {

   // Properties
    private val viewModel: AddExercisePlanViewModel by viewModels()
    private val args: AddExercisePlanFragmentArgs by navArgs()
    private val userExercisePlanId: String? by lazy { args.userExercisePlanId }

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentHelper.createBaseComposeView(inflater, container) {
            AddExercisePlanForm(viewModel) {
                addExercisePlan()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    // Functions
    private fun setupView() {
        (activity as? AppCompatActivity)?.supportActionBar?.title = if (userExercisePlanId == null)
            getString(R.string.add_exercise_plan) else getString(R.string.edit_exercise_plan)
    }

    private fun addExercisePlan() {
        IndicatorHandler.show(requireContext())
        viewModel.saveUserPlanExercise {
            IndicatorHandler.hide()
            if (it) {
                findNavController().popBackStack()
            }
        }
    }
}

@Composable
fun AddExercisePlanForm(viewModel: AddExercisePlanViewModel,
                        onSaveClick: () -> Unit = {}) {

    // Properties
    var selectedSequenceIndex by rememberSaveable { mutableIntStateOf(-1) }
    var showExerciseSelectionSheet by remember { mutableStateOf(false) }
    val planNameErrorMessageId by viewModel.planNameErrorMessageId.collectAsState()
    val exerciseSequenceStateList by viewModel.exerciseSequenceStateList.collectAsState()
    val durationState by viewModel.durationState.collectAsState()
    val enableAddExerciseButton by viewModel.enableAddExerciseButton.collectAsState()
    val planeName by viewModel.planeName.collectAsState()
    val exercisesStateList by viewModel.exercisesStateList.collectAsState()

    // Content
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val error = planNameErrorMessageId?.let {
            stringResource(id = it)
        } ?: run {
            null
        }

        TextFieldItem(
            MTextStyle(text = stringResource(R.string.exercise_plan_name)),
            MTextStyle(text = planeName),
            error) {
            viewModel.updatePlanName(it)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.exercise_sequence),
                fontFamily = proximaNovaFamily,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(16f, TextUnitType.Sp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(
                    items =  exerciseSequenceStateList,
                    key = { _, item -> item.itemTag }
                ) { index, dynamicUIItem ->

                    ExerciseSequenceItem(
                        dynamicUIItem,
                        onAddExercise = {
                            showExerciseSelectionSheet = true
                            selectedSequenceIndex = index
                        },
                        onRemoveExercise = {
                            selectedSequenceIndex = -1
                            viewModel.removeExerciseSequenceItem(index)
                        }
                    )
                }
            }

            Text(
                text = stringResource(R.string.duration_min, durationState),
                fontFamily = proximaNovaFamily,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = colorResource(id = R.color.deep_turquoise_blue_color)
            )
        }

        SaveButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            enableSaveButton = enableAddExerciseButton,
            onSaveClick = onSaveClick
        )
    }

    if (showExerciseSelectionSheet) {
        ExerciseSelectionSheet(
            itemModels = exercisesStateList,
            onDismissRequest = { showExerciseSelectionSheet = false },
            onAddExercise = { exercise, level ->
                viewModel.updateExerciseSequenceItem(exercise, level, selectedSequenceIndex)
                showExerciseSelectionSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSelectionSheet(itemModels: List<DynamicUIItem>,
                           onDismissRequest: () -> Unit,
                           onAddExercise: (DynamicUIItem, DynamicUIItem) -> Unit) {

    // Properties
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedExercise by rememberSaveable {
        val itemModel = itemModels.firstOrNull {
            val additionalInfo = it.additionalData as? Map<*, *> ?: return@firstOrNull false
            additionalInfo[AddExercisePlanViewModel.IS_EXERCISE_SELECTED_KEY] as? Boolean ?: return@firstOrNull false
        }
        mutableStateOf(itemModel)
    }
    var selectedLevel by rememberSaveable {
        mutableStateOf(
            selectedExercise?.let {
                val additionalInfo = it.additionalData as? Map<*, *> ?: return@let null
                val levels = additionalInfo[AddExercisePlanViewModel.LEVELS_KEY] as? List<*> ?: return@let null
                val selectedLevelIndex = additionalInfo[AddExercisePlanViewModel.SELECTED_LEVEL_INDEX_KEY] as? Int ?: return@let null
                if (selectedLevelIndex in levels.indices) levels[selectedLevelIndex] as? DynamicUIItem else null
            }
        )
    }

    // Content
    ModalBottomSheet(
        modifier = Modifier.fillMaxHeight(),
        onDismissRequest = { onDismissRequest() },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            IconButton(
                onClick = { onDismissRequest() }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(colorResource(id = R.color.disable_grey_color))
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = itemModels,
                    key = { it.itemTag }
                ) { item ->

                    val levels by rememberSaveable {
                        val levels = (item.additionalData as? Map<*, *>)?.get("levels") as? List<*>
                        mutableStateOf(levels)
                    }

                    ExerciseItem(
                        model = item,
                        isSelectedExercise = (selectedExercise == item),
                        levels = levels,
                        selectedLevel = if (selectedExercise == item) selectedLevel else null,
                        onExerciseSelected = {
                            if (selectedExercise != it) {
                                selectedExercise = null
                                selectedExercise = it
                            }
                        },
                        onLevelSelected = { level ->
                            if (selectedLevel != level) {
                                selectedLevel = level
                            }
                        }
                    )
                }
            }

            TextButton(
                modifier = Modifier.padding(top = 16.dp),
                onClick = {
                    val exercise = selectedExercise ?: return@TextButton
                    val level = selectedLevel ?: return@TextButton

                    onAddExercise(exercise, level)
                }
            ) {
                Text(
                    text = stringResource(R.string.add),
                    fontFamily = proximaNovaFamily,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(18f, TextUnitType.Sp),
                    color = colorResource(id = R.color.deep_turquoise_blue_color)
                )
            }
        }
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ExerciseItem(model: DynamicUIItem,
                 isSelectedExercise: Boolean,
                 levels: List<*>?,
                 selectedLevel: DynamicUIItem?,
                 onExerciseSelected: (DynamicUIItem) -> Unit,
                 onLevelSelected: (DynamicUIItem) -> Unit) {

    // Content
    Card(
        onClick = { onExerciseSelected(model) },
        border = BorderStroke(1.dp, colorResource(
            id = if (isSelectedExercise) R.color.forest_green_color else R.color.white
        )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            model.image?.url?.let { url ->
                var bitmap by remember { mutableStateOf<Uri?>(null) }

                LaunchedEffect(url, model.itemTag) {
                    FirebaseStorageManager.downloadImage(url) { downloadedBitmap ->
                        bitmap = downloadedBitmap
                    }
                }
                GlideImage(
                    model = bitmap,
                    contentDescription = model.image?.name,
                    modifier = Modifier.size(80.dp)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                model.title?.let {
                    Text(
                        text = it.text,
                        fontFamily = proximaNovaFamily,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        fontSize = TextUnit(16f, TextUnitType.Sp)
                    )
                }

                model.description?.let {
                    Text(
                        text = it.text,
                        fontFamily = proximaNovaFamily,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Normal,
                        fontSize = TextUnit(16f, TextUnitType.Sp)
                    )
                }

                levels?.let {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(levels) { level ->
                            if (level !is DynamicUIItem) return@items
                            val title = level.title ?: return@items

                            ElevatedButtonItem(
                                title = title,
                                border = if (selectedLevel == level) BorderStroke(
                                    1.dp, colorResource(
                                        id = R.color.forest_green_color
                                    )
                                ) else null,
                                onClick = {
                                    if (!isSelectedExercise) return@ElevatedButtonItem
                                    onLevelSelected(level)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}