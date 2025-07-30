package com.example.medijourney.common.helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.example.medijourney.R

object FragmentHelper {

    fun createBaseComposeView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              content: @Composable () -> Unit): View? {
        val composeView = inflater.inflate(R.layout.fragment_base_compose, container, false) as? ComposeView ?: return null
        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent(content)
        }
        return composeView
    }
}