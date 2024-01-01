package com.example.medijourney.modules.profile.edit_profile.adapter

import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface

interface EditProfileAdapterInterface: BaseAdapterInterface {
    fun checkErrorAtField(tag: String, text: String): String?
    fun getErrorAtField(tag: String): String?
    fun getTempValue(tag: String): String?
    fun updateValue(tag: String, text: String)
}