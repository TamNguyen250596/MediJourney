package com.example.medijourney.modules.authentication.finger_print_sign_in

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants.CIPHERTEXT_WRAPPER
import com.example.medijourney.common.constants.Constants.SHARED_PREFS_FILENAME
import com.example.medijourney.common.managers.bio_metric.BiometricPromptUtils
import com.example.medijourney.common.managers.bio_metric.CiphertextWrapper
import com.example.medijourney.common.managers.bio_metric.CryptographyManager
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.text_view.TextViewListAdapter
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.common.ui_components.item_decoration.ItemBorderDecoration
import com.example.medijourney.common.ui_components.item_touch_helper.swipe_delete.SwipeToDeleteCallback
import com.example.medijourney.common.ui_components.item_touch_helper.swipe_delete.SwipeToDeleteCallbackInterface
import com.example.medijourney.databinding.FragmentFingerPrintSignInBinding

class FingerPrintSignInFragment : Fragment(), BaseAdapterInterface,
    SwipeToDeleteCallbackInterface {

    // Properties
    private lateinit var binding: FragmentFingerPrintSignInBinding
    private val viewModel: FingerPrintSignInViewModel by viewModels()
    private val cryptographyManager = CryptographyManager()
    private var ciphertextWrapper: CiphertextWrapper? = null

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFingerPrintSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        viewModel.getUserList(requireContext())
    }

    override fun onPause() {
        super.onPause()
        IndicatorHandler.hide()
    }

    // Functions
    private fun setupView() {
        binding.recycleView.layoutManager = LinearLayoutManager(requireContext())
        binding.recycleView.addItemDecoration(ItemBorderDecoration(requireContext()))
        val swipeController = SwipeToDeleteCallback(requireContext())
        swipeController.output = this
        val itemTouchHelper = ItemTouchHelper(swipeController)
        itemTouchHelper.attachToRecyclerView(binding.recycleView)
    }

    private fun observeViewModel() {
        viewModel.userList.observe(viewLifecycleOwner) {
            val adapter = binding.recycleView.adapter as? TextViewListAdapter
            if (adapter == null) {
                val newAdapter = TextViewListAdapter(it).apply {
                    output = this@FingerPrintSignInFragment
                }
                binding.recycleView.adapter = newAdapter
            } else {
                adapter.updateItems(it)
            }
        }
    }

    // BaseAdapterInterface
    override fun selectedItem(model: BaseItemInterface?) {
        val canAuthenticateWithBiometrics = BiometricManager.from(requireContext()).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS

        if (canAuthenticateWithBiometrics) {
            viewModel.handleSelectedViewModel(model)?.let {
                showBiometricPromptForDecryption(it)
            }
        } else {
            showBiometricAuthError()
        }
    }

    private fun showBiometricPromptForDecryption(secretKeyName: String) {
        if (ciphertextWrapper == null) {
            ciphertextWrapper = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
                requireActivity(),
                SHARED_PREFS_FILENAME,
                Context.MODE_PRIVATE,
                CIPHERTEXT_WRAPPER
            )
        }
        ciphertextWrapper?.let { textWrapper ->
            val cipher = cryptographyManager.getInitializedCipherForDecryption(
                secretKeyName, textWrapper.initializationVector
            )
            val biometricPrompt = BiometricPromptUtils.createBiometricPrompt(requireActivity(), ::decryptServerTokenFromStorage)
            val promptInfo = BiometricPromptUtils.createPromptInfo(requireActivity())
            biometricPrompt.authenticate(promptInfo, androidx.biometric.BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun decryptServerTokenFromStorage(authResult: androidx.biometric.BiometricPrompt.AuthenticationResult) {
        ciphertextWrapper?.let { textWrapper ->
            authResult.cryptoObject?.cipher?.let {
                val password = cryptographyManager.decryptData(textWrapper.ciphertext, it)
                FirebaseAuthManager.signIn(viewModel.selectedUser, password, requireContext())
            }
        }
    }

    private fun showBiometricAuthError() {
        Toast.makeText(
            requireContext(),
            getString(R.string.app_cant_authenticate_using_biometrics),
            Toast.LENGTH_SHORT
        ).show()
    }

    // SwipeToDeleteCallbackInterface
    override fun onSwiped(position: Int, direction: Int) {
        viewModel.deleteItem(position, cryptographyManager, requireContext())
    }
}