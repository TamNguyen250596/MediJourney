package com.example.medijourney.modules.profile.delete_account

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.medijourney.R
import com.example.medijourney.common.managers.firebase_auth.AuthenticationResult
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.databinding.FragmentDeleteAccountBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAccountFragment : Fragment() {

    // Properties
    private lateinit var binding: FragmentDeleteAccountBinding
    private val viewModel: DeleteAccountViewModel by viewModels()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeleteAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUIComponents()
        observeViewModel()
    }

    // Functions
    private fun  observeUIComponents() {
        binding.deactivateButton.setOnClickListener {
            viewModel.deActiveUser(requireActivity())
        }

        binding.deleteButton.setOnClickListener {
            viewModel.deleteUser(requireActivity())
        }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(viewLifecycleOwner) {
            when(it) {
                AuthenticationResult.DEACTIVE_ACCOUNT_FAILED -> {
                    showDialog(getString(R.string.error), getString(R.string.error_user_deactivation_failed))
                }
                AuthenticationResult.DEACTIVE_ACCOUNT_SUCCESS -> {
                    showDialog(getString(R.string.success), getString(R.string.account_deactivated_message))
                }
                AuthenticationResult.DELETE_ACCOUNT_FAILED -> {
                    showDialog(getString(R.string.error), getString(R.string.error_user_deletion_failed))
                }
                else -> {}
            }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it) {
                IndicatorHandler.show(requireContext())
            } else {
                IndicatorHandler.hide()
            }
        }
    }

    private fun showDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage(message)
            .setTitle(title)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}