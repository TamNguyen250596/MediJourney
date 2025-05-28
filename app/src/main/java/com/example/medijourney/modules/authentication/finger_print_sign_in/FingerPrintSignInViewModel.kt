package com.example.medijourney.modules.authentication.finger_print_sign_in

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.helpers.DataStoreHelper
import com.example.medijourney.common.managers.bio_metric.CryptographyManager
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.TitleItemModel
import com.example.medijourney.common.models.ui_models.EdgePadding
import com.example.medijourney.common.models.ui_models.MTextStyle
import kotlinx.coroutines.launch

class FingerPrintSignInViewModel: ViewModel() {

    // Properties
    var userList = MutableLiveData<MutableList<TitleItemModel>>()
    var selectedUser: String = ""

    // Functions
    fun getUserList(context: Context) {
        viewModelScope.launch {
            val users = DataStoreHelper.getStringList(context, Constants.biometricAuthUsers)
            val list = users.map {
                TitleItemModel(
                    title = MTextStyle(text = it),
                    padding = EdgePadding(top = 32, bottom = 32, left = 16, right = 16),
                    data = it)
            }
            userList.postValue(list.toMutableList())
        }
    }

    fun handleSelectedViewModel(viewModel: BaseItemInterface?): String? {
        val titleViewModel = viewModel as? TitleItemModel ?: return null
        val currentUserEmail = titleViewModel.data as? String ?: return null

        selectedUser = currentUserEmail
        return currentUserEmail
    }

    fun deleteItem(position: Int, cryptographyManager: CryptographyManager, context: Context) {
        viewModelScope.launch {
            val keyName = userList.value?.get(position)?.data as? String
            keyName?.let {
                cryptographyManager.removeData(keyName)
                DataStoreHelper.removeStringInList(context, Constants.biometricAuthUsers, keyName)
                getUserList(context)
            }
        }
    }
}