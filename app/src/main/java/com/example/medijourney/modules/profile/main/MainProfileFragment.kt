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
import com.example.medijourney.common.helpers.ImageHelper
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.h_dual_image_text_view.HDualImageTextViewAdapter
import com.example.medijourney.databinding.FragmentMainProfileBinding

class MainProfileFragment : Fragment(), BaseAdapterInterface {

    // Properties
    private lateinit var binding: FragmentMainProfileBinding
    private val viewModel: MainProfileViewModel by viewModels()

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
    }

    // Functions
    private fun setupView() {
        binding.userAvatarView.editAvatarButton.visibility = View.GONE
        binding.userAvatarView.editProfileBGButton.visibility = View.GONE
        binding.profileRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun observeViewModel() {
        viewModel.bgImageUri.observe(viewLifecycleOwner) { uri ->
            if (uri == null) return@observe

            binding.userAvatarView.profileBGImageView.setImageURI(uri)
            binding.userAvatarView.profileBGImageView.background = null
        }
        viewModel.avatarImageUri.observe(viewLifecycleOwner) { uri ->
            if (uri == null) return@observe

            binding.userAvatarView.avatarImageView.setImageURI(uri)
        }
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            if (name == null) return@observe

            binding.userAvatarView.userNameTextView.text = name
        }
        viewModel.userMembershipInfo.observe(viewLifecycleOwner) { info ->
            if (info == null) return@observe

            ImageHelper.getResourceIdByName(info.first)?.let {
                binding.userAvatarView.membershipImageView.setImageResource(it)
            }
            binding.userAvatarView.membershipTextView.text = info.second
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