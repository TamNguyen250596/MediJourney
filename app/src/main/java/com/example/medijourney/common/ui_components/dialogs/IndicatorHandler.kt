package com.example.medijourney.common.ui_components.dialogs

import android.app.Dialog
import android.content.Context
import com.example.medijourney.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object IndicatorHandler {

    // Properties
    private var dialog: Dialog? = null

    // Functions
    fun show(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            dialog?.dismiss()
            dialog = Dialog(context)
            dialog?.setContentView(R.layout.dialog_spinner)
            dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog?.show()
        }
    }

    fun hide() {
        CoroutineScope(Dispatchers.Main).launch {
            dialog?.dismiss()
            dialog = null
        }
    }
}