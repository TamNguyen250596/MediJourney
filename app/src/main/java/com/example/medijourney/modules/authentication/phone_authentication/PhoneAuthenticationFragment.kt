package com.example.medijourney.modules.authentication.phone_authentication

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.navArgs
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.common.ui_components.fragments.otp_view.OtpFragment
import com.example.medijourney.common.ui_components.fragments.phone_number_view.PhoneNumberFragment
import com.example.medijourney.databinding.FragmentPhoneAuthenticationBinding

class PhoneAuthenticationFragment : Fragment() {

    // Properties
    private val viewModel: PhoneAuthenticationViewModel by viewModels()
    private lateinit var biding: FragmentPhoneAuthenticationBinding
    private lateinit var phoneNumberFragment: PhoneNumberFragment
    private lateinit var otpFragment: OtpFragment
    private val args: PhoneAuthenticationFragmentArgs by navArgs()
    private val viewType: String by lazy { args.viewType }

    // Life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        phoneNumberFragment = PhoneNumberFragment()
        otpFragment = OtpFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.phoneNumberFragmentContainerView, phoneNumberFragment)
            .replace(R.id.otpFragmentContainerView, otpFragment)
            .commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        biding = FragmentPhoneAuthenticationBinding.inflate(inflater, container, false)
        return biding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    override fun onPause() {
        super.onPause()
        IndicatorHandler.hide()
    }

    // Functions
    private fun setupUI() {
        val title = when(viewType) {
            Constants.FORGOT_PASSWORD -> getString(R.string.forgot_password)
            Constants.ENABLE_OTP_AUTH -> getString(R.string.otp_authentication)
            else -> getString(R.string.sign_in)
        }
        (activity as? AppCompatActivity)?.supportActionBar?.title = title
        childFragmentManager.setFragmentResultListener("phone_number_result", this) { _, bundle ->
            viewModel.phoneNumber = bundle.getString("phone_number")
        }
        childFragmentManager.setFragmentResultListener("otp_string_result", this) { _, bundle ->
            viewModel.otp = bundle.getString("otp_string")
            IndicatorHandler.show(requireContext())
            when(viewType) {
                Constants.FORGOT_PASSWORD -> {
                    viewModel.requestAPIToGetTempLogInToken {}
                }
                Constants.ENABLE_OTP_AUTH -> {
                    IndicatorHandler.show(requireContext())
                    viewModel.enableOTPAuth {
                        IndicatorHandler.hide()
                    }
                }
                Constants.SIGN_IN_BY_PHONE_NUMBER -> {
                    FirebaseAuthManager.signInByPhoneNumber(requireActivity())
                }
                else -> {}
            }
        }
    }

    private fun observeViewModel() {
        viewModel.errorMessages.observe(viewLifecycleOwner) {
            it?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}