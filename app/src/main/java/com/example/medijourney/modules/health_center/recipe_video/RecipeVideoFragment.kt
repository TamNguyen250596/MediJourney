package com.example.medijourney.modules.health_center.recipe_video

import android.annotation.SuppressLint
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.navArgs
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.ui_components.composes.VideoPlayer
import com.example.medijourney.databinding.FragmentRecipeVideoBinding
import com.example.medijourney.modules.health_center.exercise_video.VideoPlayerFragmentArgs

class RecipeVideoFragment : Fragment() {

    // Properties
    private val viewModel: RecipeVideoViewModel by viewModels()
    private lateinit var binding: FragmentRecipeVideoBinding
    private val args : RecipeVideoFragmentArgs by navArgs()
    private val recipeId: Int by lazy { args.recipeId }

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecipeVideoBinding.inflate(inflater, container, false)
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                RecipeVideo(viewModel)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onViewCreated(recipeId)
    }
}

@Composable
fun RecipeVideo(viewModel: RecipeVideoViewModel) {

    // Properties
    val mediaItems by viewModel.mediaItems.collectAsState()
    val htmlDescription by viewModel.htmlDescription.collectAsState()

    // Content
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VideoPlayer(
            mediaItems = mediaItems.toMutableStateList(),
            modifier = Modifier.fillMaxWidth().height(300.dp)
        )

        Card(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
        ) {
            Text(
                AnnotatedString.fromHtml(
                    htmlDescription,
                    linkStyles = TextLinkStyles(
                        style = SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            fontStyle = FontStyle.Italic,
                            color = colorResource(id = R.color.deep_turquoise_blue_color),
                            fontFamily = proximaNovaFamily,
                            fontSize = TextUnit(16f, TextUnitType.Sp)
                        )
                    )
                ),
                fontFamily = proximaNovaFamily,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                fontSize = TextUnit(16f, TextUnitType.Sp),
                modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())
            )
        }
    }
}