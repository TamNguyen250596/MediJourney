package com.example.medijourney.common.ui_components.fragments.otp_view

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medijourney.R
import com.example.medijourney.databinding.FragmentOtpBinding
import com.example.medijourney.common.ui_components.recycle_view_adapter.otp.OTPAdapter
import com.example.medijourney.common.ui_components.recycle_view_adapter.otp.OTPAdapterListener

class OtpFragment : Fragment(), OTPAdapterListener {

    // Properties
    private lateinit var binding: FragmentOtpBinding
    private lateinit var viewModel: OtpViewModel
    private val otpDigits = MutableList(6) {""}
    private var countdownTimer: CountDownTimer? = null
    val otpString: String?
        get() {
            return viewModel.otpString
        }

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = OtpViewModel()
        binding = FragmentOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        observeUIComponents()
    }

    // Functions
    fun updateDescriptionTextView(text: String) {
        binding.descriptionTextView.text = text
    }

    fun startCountdown(duration: Long, interval: Long) {
        countdownTimer = object : CountDownTimer(duration, interval) {
            override fun onTick(millisUntilFinished: Long) {
                binding.timerTextView.text = viewModel.generateTimerString(millisUntilFinished)
            }

            override fun onFinish() {
                binding.timerTextView.text = "00:00"
                enableResendButton(true)
            }
        }.start()
    }

    fun cancelCountdown() {
        countdownTimer?.cancel()
        countdownTimer = null
    }

    private fun setupView() {
        binding.otpRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val otpAdapter = OTPAdapter(otpDigits)
        binding.otpRecyclerView.adapter = otpAdapter
        otpAdapter.listener = this
    }

    private fun observeViewModel() {
        viewModel.enableResendButton.observe(viewLifecycleOwner) { enable ->
            enableResendButton(enable)
        }
        viewModel.enableConfirmButton.observe(viewLifecycleOwner) { enable ->
            binding.confirmOtpButton.isEnabled = enable
            val backgroundColor = when (enable) {
                true -> ContextCompat.getColor(requireContext(), R.color.deep_turquoise_blue_color)
                false -> ContextCompat.getColor(requireContext(), R.color.disable_grey_color)
            }
            binding.confirmOtpButton.setBackgroundColor(backgroundColor)
            binding.confirmOtpButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    private fun enableResendButton(enable: Boolean) {
        binding.resendOtpButton.isEnabled = enable
        val backgroundColor = when (enable) {
            true -> ContextCompat.getColor(requireContext(), R.color.primary_red_color)
            false -> ContextCompat.getColor(requireContext(), R.color.disable_grey_color)
        }
        binding.resendOtpButton.setBackgroundColor(backgroundColor)
        binding.resendOtpButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun observeUIComponents() {
        binding.resendOtpButton.setOnClickListener {
            enableResendButton(false)
        }
        binding.confirmOtpButton.setOnClickListener {
            val result = Bundle().apply {
                putString("otp_string", viewModel.otpString)
            }
            parentFragmentManager.setFragmentResult("otp_string_result", result)
        }
    }

    // OTPAdapterListener
    override fun textViewDidChange(text: String, position: Int) {
        if (text.isNotEmpty()) {
            viewModel.oldValuesAtPosition[position] = text
            viewModel.generateOTPString(position, text)
            if (position < otpDigits.size) {
                val otpViewHolder = binding.otpRecyclerView.findViewHolderForAdapterPosition(position + 1) as? OTPAdapter.OTPViewHolder
                otpViewHolder?.editTextDigit?.requestFocus()
            }
        }
    }

    override fun selectedDeleteButton(text: String, position: Int) {
        if (position - 1 >= 0) {
            val previousPosition = position - 1
            val previousOtpViewHolder = binding.otpRecyclerView.findViewHolderForAdapterPosition(previousPosition) as? OTPAdapter.OTPViewHolder

            if (viewModel.oldValuesAtPosition[position] != null
                && viewModel.oldValuesAtPosition[position] != "") {
                viewModel.generateOTPString(position, "")
                viewModel.oldValuesAtPosition[position] = ""
            } else {
                previousOtpViewHolder?.editTextDigit?.requestFocus()
                previousOtpViewHolder?.editTextDigit?.text = null
                viewModel.generateOTPString(previousPosition, "")
                viewModel.oldValuesAtPosition[previousPosition] = ""
            }
        }
    }
}