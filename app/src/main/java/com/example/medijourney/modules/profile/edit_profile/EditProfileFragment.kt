package com.example.medijourney.modules.profile.edit_profile

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.constants.EditProfileField
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.common.ui_components.fragments.media_selection.MediaSelectionViewModel
import com.example.medijourney.databinding.FragmentEditProfileBinding
import com.example.medijourney.modules.profile.edit_profile.adapter.EditProfileAdapter
import com.example.medijourney.modules.profile.edit_profile.adapter.EditProfileAdapterInterface

class EditProfileFragment : Fragment(), EditProfileAdapterInterface,
    UserAvatarSectionFragmentInterface {

    // Properties
    private lateinit var binding: FragmentEditProfileBinding
    private val viewModel: EditProfileViewModel by viewModels()
    private val mediaSelectionViewModel: MediaSelectionViewModel by activityViewModels()
    private var openMediaField: EditProfileField? = null

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        observeUIComponents()
    }

    // Functions
    private fun setupView() {
        binding.userAvatarView.editAvatarButton.setOnClickListener {
            selectedEditAvatarButton()
        }
        binding.userAvatarView.editProfileBGButton.setOnClickListener {
            selectedEditProfileBGButton()
        }
        binding.profileRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun observeViewModel() {
        viewModel.bgImageUri.observe(viewLifecycleOwner) { uri ->
            if (uri == null) return@observe

            updateProfileBGImageView(uri)
        }

        viewModel.avatarImageUri.observe(viewLifecycleOwner) { uri ->
            if (uri == null) return@observe

            binding.userAvatarView.avatarImageView.setImageURI(uri)
        }

        mediaSelectionViewModel.uri.observe(viewLifecycleOwner) {
            val uri = it ?: return@observe
            val openMediaField = openMediaField ?: return@observe

            when (openMediaField) {
                EditProfileField.BACKGROUND -> updateProfileBGImageView(uri)
                EditProfileField.AVATAR -> updateAvatarImageView(uri)
                else -> return@observe
            }
            IndicatorHandler.show(requireContext())
            viewModel.handleGalleryLauncherResult(uri, openMediaField) {
                IndicatorHandler.hide()
            }
        }

        viewModel.itemModels.observe(viewLifecycleOwner) { itemModels ->
            val adapter = binding.profileRecyclerView.adapter as? EditProfileAdapter
            if (adapter == null) {
                val newAdapter = EditProfileAdapter(itemModels).apply {
                    output = this@EditProfileFragment
                }
                binding.profileRecyclerView.adapter = newAdapter
            } else {
                adapter.updateItems(itemModels)
            }
        }

        viewModel.changedItemPosition.observe(viewLifecycleOwner) { index ->
            index?.let {
                val adapter = binding.profileRecyclerView.adapter as? EditProfileAdapter ?: return@observe
                val userInfoList = viewModel.itemModels.value ?: return@observe
                val model = userInfoList.getOrNull(index) ?: return@observe
                adapter.updateItem(model, index)
            }
        }
    }

    private fun updateProfileBGImageView(uri: Uri) {
        binding.userAvatarView.profileBGImageView.setImageURI(uri)
        binding.userAvatarView.profileBGImageView.background = null
    }

    private fun updateAvatarImageView(uri: Uri) {
        binding.userAvatarView.avatarImageView.setImageURI(uri)
        binding.userAvatarView.avatarImageView.background = null
    }

    private fun observeUIComponents() {
        binding.editProfileButton.setOnClickListener {
            IndicatorHandler.show(requireContext())
            viewModel.saveUser {
                IndicatorHandler.hide()
            }
        }
    }


    // UserAvatarSectionFragmentInterface
    override fun selectedEditProfileBGButton() {
        openMediaField = EditProfileField.BACKGROUND
        findNavController().navigate(R.id.action_editProfileFragment_to_mediaSelectionFragment)
    }

    override fun selectedEditAvatarButton() {
        openMediaField = EditProfileField.AVATAR
        findNavController().navigate(R.id.action_editProfileFragment_to_mediaSelectionFragment)
    }

    // EditProfileAdapterInterface
    override fun checkErrorAtField(tag: String, text: String): String? {
        return viewModel.checkErrorAtField(tag, text)
    }

    override fun getErrorAtField(tag: String): String? {
        return viewModel.getErrorAtField(tag)
    }

    override fun getTempValue(tag: String): String? {
        return viewModel.getTempTitle(tag)
    }

    override fun updateValue(tag: String, text: String) {
        viewModel.updateTempInfoAtTag(tag, text)
    }

    override fun selectedItem(model: BaseItemInterface?) {
        val itemTag = model?.itemTag ?: return
        val fieldTag = EditProfileField.valueOf(itemTag.uppercase())

        when (fieldTag) {
            EditProfileField.BIRTH_DATE -> showDatePicker(itemTag)
            EditProfileField.GENDER -> showGenderSelectionDialog(itemTag)
            else -> {}
        }
    }

    // Router
    private fun showDatePicker(itemTag: String) {
        DatePickerDialog(requireContext()).apply {
            setOnDateSetListener { _, year, month, dayOfMonth ->
                val date = viewModel.createDate(year, month, dayOfMonth)
                viewModel.updateTempInfoAtTag(itemTag, date)
            }
            show()
        }
    }

    private fun showGenderSelectionDialog(itemTag: String) {
        AlertDialog.Builder(requireContext()).apply {
            setSingleChoiceItems(Constants.genderArray, viewModel.getGenderIndex().toInt()) { dialogInterface, i ->
                viewModel.updateTempInfoAtTag(itemTag, i)
                Handler(Looper.getMainLooper())
                    .postDelayed({
                        dialogInterface.dismiss()
                    }, 2000)
            }
            show()
        }
    }
}