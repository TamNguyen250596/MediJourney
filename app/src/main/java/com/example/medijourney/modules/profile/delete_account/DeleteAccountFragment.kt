package com.example.medijourney.modules.profile.delete_account

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.medijourney.R
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.databinding.FragmentDeleteAccountBinding

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
        viewModel.onViewCreated(view)
    }

    // Functions
    private fun  observeUIComponents() {
        binding.deactivateButton.setOnClickListener {
            viewModel.deActiveUser(requireContext())
        }

        binding.deleteButton.setOnClickListener {
            viewModel.deleteUser(requireActivity())
        }
    }

    private fun observeViewModel() {
        viewModel.errorMessages.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                showDialog(getString(R.string.error), it)
            }
        }
        viewModel.shouldLogout.observe(viewLifecycleOwner) {
            if (it) {
                FirebaseAuthManager.logOut(requireActivity())
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