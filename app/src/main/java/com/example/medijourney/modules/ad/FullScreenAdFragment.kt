package com.example.medijourney.modules.ad

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.medijourney.common.helpers.FragmentHelper

class FullScreenAdFragment : DialogFragment() {

    // Properties
    var imageUrlString: String = ""
    var actionUrlString: String? = null

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentHelper.createBaseComposeView(inflater, container) {
            FullScreenAdView(
                imageUrlString = imageUrlString,
                countDownSecond = 5,
                onClickImage = {
                    
                },
                onClickSkip = {
                    dismiss()
                }
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), android.R.style.Theme_Material_NoActionBar_Fullscreen)
    }
}