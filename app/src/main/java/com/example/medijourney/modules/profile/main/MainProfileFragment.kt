package com.example.medijourney.modules.profile.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medijourney.R
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.ui_components.fragments.user_avatar_section.UserAvatarSectionFragment
import com.example.medijourney.common.ui_components.fragments.user_avatar_section.UserAvatarSectionViewModel
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.h_dual_image_text_view.HDualImageTextViewAdapter
import com.example.medijourney.databinding.FragmentMainProfileBinding

class MainProfileFragment : Fragment(), BaseAdapterInterface {

    // Properties
    private lateinit var binding: FragmentMainProfileBinding
    private val viewModel: MainProfileViewModel by viewModels()
    private val userAvatarSectionViewModel: UserAvatarSectionViewModel by viewModels()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        viewModel.onCreateView()
    }

    // Functions
    private fun setupView() {
        val userAvatarSectionFragmentTag = UserAvatarSectionFragment::class.simpleName
        val userAvatarSectionFragment = childFragmentManager.findFragmentByTag(userAvatarSectionFragmentTag)
                as? UserAvatarSectionFragment ?: UserAvatarSectionFragment.newInstance(
            displayEditProfileBGButton = true,
            displayUserNameTextView = false,
            displayEditAvatarButton = true,
            displayMembershipLinearLayout = false
        )
        if (userAvatarSectionFragment.isDetached.not()) {
            childFragmentManager.beginTransaction()
                .replace(R.id.fragment_user_avatar_section, userAvatarSectionFragment, userAvatarSectionFragmentTag)
                .commit()
        }
        binding.profileRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun observeViewModel() {
        viewModel.bgImageUri.observe(viewLifecycleOwner) { uri ->
            userAvatarSectionViewModel.bgImageUri.postValue(uri)
        }
        viewModel.avatarImageUri.observe(viewLifecycleOwner) { uri ->
            userAvatarSectionViewModel.avatarImageUri.postValue(uri)
        }
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            userAvatarSectionViewModel.userNameText.postValue(name)
        }
        viewModel.userMembershipInfo.observe(viewLifecycleOwner) {
            userAvatarSectionViewModel.membershipInfo.postValue(it)
        }
        viewModel.itemModels.observe(viewLifecycleOwner) {
            val adapter = HDualImageTextViewAdapter(it)
            binding.profileRecyclerView.adapter = adapter
            adapter.output = this@MainProfileFragment
        }
    }

    // ProfileAdapterListener
    override fun selectedItem(model: BaseItemInterface?) {
        when (model?.itemTag) {
            "edit_profile" -> {
                findNavController().navigate(R.id.action_mainProfileFragment_to_editProfileFragment)
            }
            "settings" -> {
                findNavController().navigate(R.id.action_mainProfileFragment_to_manageProfileFragment)
            }
            "log_out" -> {
                handleLogOut()
            }
            else -> {
                return
            }
        }
    }

    private fun handleLogOut() {
        FirebaseAuthManager.logOut(requireActivity())
    }
}