package com.example.medijourney.common.ui_components.fragments.user_avatar_section

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.medijourney.common.helpers.ImageHelper
import com.example.medijourney.databinding.FragmentUserAvatarSectionBinding

class UserAvatarSectionFragment : Fragment() {

    // Properties
    private lateinit var binding: FragmentUserAvatarSectionBinding
    private var displayEditProfileBGButton = false
    private var displayUserNameTextView: Boolean = false
    private var displayEditAvatarButton: Boolean = false
    private var displayMembershipLinearLayout: Boolean = false
    private val viewModel: UserAvatarSectionViewModel by viewModels({requireParentFragment()})

    // Life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            displayEditProfileBGButton = it.getBoolean(DISPLAY_EDIT_PROFILE_BG_BUTTON)
            displayUserNameTextView = it.getBoolean(DISPLAY_USER_NAME_TEXT_VIEW)
            displayEditAvatarButton = it.getBoolean(DISPLAY_EDIT_AVATAR_BUTTON)
            displayMembershipLinearLayout = it.getBoolean(DISPLAY_MEMBERSHIP_LINEAR_LAYOUT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserAvatarSectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeUIComponents()
        observeViewModel()
    }

    // Function
    private fun setupView() {
        binding.editProfileBGButton.visibility = if (displayEditProfileBGButton) View.GONE else View.VISIBLE
        binding.editAvatarButton.visibility = if (displayEditAvatarButton) View.GONE else View.VISIBLE
        binding.membershipLinearLayout.visibility = if (displayMembershipLinearLayout) View.GONE else View.VISIBLE
        binding.userNameTextView.visibility = if (displayUserNameTextView) View.GONE else View.VISIBLE
    }

    private fun observeUIComponents() {
        binding.editProfileBGButton.setOnClickListener {
            (parentFragment as? UserAvatarSectionFragmentInterface)?.selectedEditProfileBGButton()
        }
        binding.editAvatarButton.setOnClickListener {
            (parentFragment as? UserAvatarSectionFragmentInterface)?.selectedEditAvatarButton()
        }
    }

    private fun observeViewModel() {
        viewModel.bgImageUri.observe(viewLifecycleOwner) {
            val bgImageUri = it ?: return@observe

            binding.profileBGImageView.setImageURI(bgImageUri)
            binding.profileBGImageView.background = null
        }

        viewModel.avatarImageUri.observe(viewLifecycleOwner) {
            val avatarImageUri = it ?: return@observe

            binding.avatarImageView.setImageURI(avatarImageUri)
        }

        viewModel.userNameText.observe(viewLifecycleOwner) {
            val userNameText = it ?: return@observe

            binding.userNameTextView.text = userNameText
        }

        viewModel.membershipInfo.observe(viewLifecycleOwner) {
            val info = it ?: return@observe

            ImageHelper.getResourceIdByName(info.first)?.let {
                binding.membershipImageView.setImageResource(it)
            }
            binding.membershipTextView.text = info.second
        }
    }

    companion object {

        const val DISPLAY_EDIT_PROFILE_BG_BUTTON = "DISPLAY_EDIT_PROFILE_BG_BUTTON"
        const val DISPLAY_USER_NAME_TEXT_VIEW = "DISPLAY_USER_NAME_TEXT_VIEW"
        const val DISPLAY_EDIT_AVATAR_BUTTON = "DISPLAY_EDIT_AVATAR_BUTTON"
        const val DISPLAY_MEMBERSHIP_LINEAR_LAYOUT = "DISPLAY_MEMBERSHIP_LINEAR_LAYOUT"

        @JvmStatic
        fun newInstance(displayEditProfileBGButton: Boolean,
                        displayUserNameTextView: Boolean,
                        displayEditAvatarButton: Boolean,
                        displayMembershipLinearLayout: Boolean) =
            UserAvatarSectionFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(DISPLAY_EDIT_PROFILE_BG_BUTTON, displayEditProfileBGButton)
                    putBoolean(DISPLAY_USER_NAME_TEXT_VIEW, displayUserNameTextView)
                    putBoolean(DISPLAY_EDIT_AVATAR_BUTTON, displayEditAvatarButton)
                    putBoolean(DISPLAY_MEMBERSHIP_LINEAR_LAYOUT, displayMembershipLinearLayout)
                }
            }
    }
}