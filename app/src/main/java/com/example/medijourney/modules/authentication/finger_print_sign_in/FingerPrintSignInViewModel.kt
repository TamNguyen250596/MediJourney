package com.example.medijourney.modules.authentication.finger_print_sign_in

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.helpers.MDataStore
import com.example.medijourney.common.managers.bio_metric.CryptographyManager
import com.example.medijourney.common.managers.firebase_auth.AuthenticationResult
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.TitleItemModel
import com.example.medijourney.common.models.ui_models.EdgePadding
import com.example.medijourney.common.models.ui_models.MTextStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FingerPrintSignInViewModel @Inject constructor(
    private val dataStore: MDataStore,
    private val faManger: FAManger
) : ViewModel() {

    // Properties
    val signInState = MutableLiveData<AuthenticationResult>(null)
    val isLoading = MutableLiveData(false)
    val userList = MutableLiveData<MutableList<TitleItemModel>>()
    var selectedUser: String = ""

    // Life cycle
    init {
        viewModelScope.launch {
            userList.postValue(getUserList())
        }
    }

    // Functions
    private suspend fun getUserList(): MutableList<TitleItemModel>  {
        val users = dataStore.getStringList(Constants.biometricAuthUsers)
        return users.map {
            TitleItemModel(
                title = MTextStyle(text = it),
                padding = EdgePadding(top = 32, bottom = 32, left = 16, right = 16),
                data = it)
        }.toMutableList()
    }

    fun handleSelectedViewModel(viewModel: BaseItemInterface?): String? {
        val titleViewModel = viewModel as? TitleItemModel ?: return null
        val currentUserEmail = titleViewModel.data as? String ?: return null

        selectedUser = currentUserEmail
        return currentUserEmail
    }

    fun deleteItem(position: Int, cryptographyManager: CryptographyManager) {
        viewModelScope.launch {
            val keyName = userList.value?.get(position)?.data as? String ?: return@launch
            cryptographyManager.removeData(keyName)
            dataStore.removeStringInList(Constants.biometricAuthUsers, keyName)
            userList.postValue(getUserList())
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = faManger.signIn(email, password)
            isLoading.postValue(false)
            signInState.postValue(result)
        }
    }
}