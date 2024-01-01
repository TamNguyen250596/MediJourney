package com.example.medijourney.common.ui_components.fragments.web_view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.medijourney.R
import com.example.medijourney.common.helpers.FileHelper
import com.example.medijourney.common.helpers.MediJourney
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.models.WebModel

class WebViewModel: ViewModel() {

    // Properties
    var uri = MutableLiveData<String>(null)
    var htmlString = MutableLiveData<String>(null)

    // Functions
    fun getWebActivityTitle(webModel: WebModel?): String {
        val context = MediJourney.getAppContext()

        return when(webModel?.webViewType) {
            WebViewConstant.PRIVACY_POLICY -> context.getString(R.string.policy_privacy)
            WebViewConstant.TERM_OF_SERVICE -> context.getString(R.string.term_of_service)
            else -> webModel?.title ?: context.getString(R.string.medi_journey)
        }
    }

    fun handleWebViewInputData(webModel: WebModel?) {
        webModel?.webViewType?.let {
            getHtmlString(it)
        }
        webModel?.url?.let {
            uri.postValue(it)
        }
    }

    private fun getHtmlString(webViewType: WebViewConstant) {
        val fileName = getHtmlName(webViewType)
        val localHtml = getLocalHtml(webViewType)
        htmlString.postValue(localHtml)

        FirebaseStorageManager.downloadHtml(fileName) { uri ->
            uri?.let {
                try {
                    this.uri.postValue(it.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } ?: run {
                htmlString.postValue(localHtml)
            }
        }
    }

    private fun getLocalHtml(webViewType: WebViewConstant): String {
        val context = MediJourney.getAppContext()
        val fileName = getHtmlName(webViewType)

        return FileHelper.getValueFromAssets(context, fileName)
    }

    private fun getHtmlName(webViewType: WebViewConstant): String {
        return when(webViewType) {
            WebViewConstant.PRIVACY_POLICY -> "privacy_policy.html"
            WebViewConstant.TERM_OF_SERVICE -> "term_of_service.html"
        }
    }
}