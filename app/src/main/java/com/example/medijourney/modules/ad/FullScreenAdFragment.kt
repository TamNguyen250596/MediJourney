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
    private val imageUrlString: String by lazy {
        requireArguments().getString(ARG_IMAGE_URL).orEmpty()
    }
    private val actionUrlString: String? by lazy {
        requireArguments().getString(ARG_ACTION_URL)
    }

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

    companion object {
        private const val ARG_IMAGE_URL = "arg_image_url"
        private const val ARG_ACTION_URL = "arg_action_url"

        fun newInstance(imageUrl: String, actionUrl: String? = null): FullScreenAdFragment {
            return FullScreenAdFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_IMAGE_URL, imageUrl)
                    putString(ARG_ACTION_URL, actionUrl)
                }
            }
        }
    }
}