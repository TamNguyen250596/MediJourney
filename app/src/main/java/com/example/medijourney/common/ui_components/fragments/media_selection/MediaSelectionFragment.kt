package com.example.medijourney.common.ui_components.fragments.media_selection

import android.Manifest
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.medijourney.R
import com.example.medijourney.databinding.FragmentMediaSelectionBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MediaSelectionFragment : BottomSheetDialogFragment() {

    // Properties
    private var takePictureLauncher: ActivityResultLauncher<Intent>? = null
    private var pickMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private lateinit var binding: FragmentMediaSelectionBinding
    private val viewModel: MediaSelectionViewModel by activityViewModels()

    // Life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupResultLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMediaSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUIComponents()
    }

    override fun onStart() {
        super.onStart()
        val isAdequatePermissions = checkMediaPermissions()
        if (!isAdequatePermissions) {
            showRequestPermissionDialog()
        }
        updateUI(isAdequatePermissions)
    }


    // Functions
    private fun setupResultLauncher() {
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.handleResultDrawable(result, requireContext())
            findNavController().popBackStack()
        }

        pickMediaLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            viewModel.handleResultUri(it)
            findNavController().popBackStack()
        }
    }

    private fun observeUIComponents() {
        binding.cameraButton.setOnClickListener {
            val takePictureLauncher = takePictureLauncher ?: return@setOnClickListener

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureLauncher.launch(cameraIntent)
        }

        binding.galleryButton.setOnClickListener {
            val pickMediaLauncher = pickMediaLauncher ?: return@setOnClickListener

            pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }
    }

    private fun updateUI(isAdequatePermissions: Boolean) {
        binding.cameraButton.isEnabled = isAdequatePermissions
        binding.cameraButton.alpha = if (isAdequatePermissions) 1f else 0.5f
        binding.galleryButton.isEnabled = isAdequatePermissions
        binding.galleryButton.alpha = if (isAdequatePermissions) 1f else 0.5f
    }

    private fun checkMediaPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun showRequestPermissionDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle(getString(R.string.kindly_grant_the_necessary_permissions))
        dialogBuilder.setPositiveButton(getString(R.string.setting)) { _, _ ->
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }
        dialogBuilder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }.show()
    }
}