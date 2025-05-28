package com.example.medijourney.common.ui_components.fragments.phone_number_view

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.medijourney.R
import com.example.medijourney.databinding.FragmentPhoneNumberBinding

class PhoneNumberFragment : Fragment() {

    // Properties
    private lateinit var binding: FragmentPhoneNumberBinding
    private val viewModel: PhoneNumberViewModel by viewModels()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhoneNumberBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        observeUIComponents()
    }

    // Functions
    private fun observeViewModel() {
        viewModel.enableSendButton.observe(viewLifecycleOwner) { enable ->
            binding.sendButton.isEnabled = enable
            val backgroundColor = when (enable) {
                true -> ContextCompat.getColor(requireContext(), R.color.deep_turquoise_blue_color)
                false -> ContextCompat.getColor(requireContext(), R.color.disable_grey_color)
            }
            binding.sendButton.setBackgroundColor(backgroundColor)
            binding.sendButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    private fun observeUIComponents() {
        binding.sendButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("phone_number", binding.phoneNoTextInputEditText.text.toString())
            }
            parentFragmentManager.setFragmentResult("phone_number_result", bundle)
        }
        binding.phoneNoTextInputEditText.addTextChangedListener(textWatcher)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            val errorMessage = viewModel.validatePhoneNumber(binding.phoneNoTextInputEditText.text.toString(), requireContext())
            binding.phoneNoTextInputLayout.error = errorMessage
        }

        override fun afterTextChanged(p0: Editable?) {}
    }
}