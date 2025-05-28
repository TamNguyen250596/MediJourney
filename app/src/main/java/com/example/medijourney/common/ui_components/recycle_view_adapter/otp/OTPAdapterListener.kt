package com.example.medijourney.common.ui_components.recycle_view_adapter.otp

interface OTPAdapterListener {
    fun textViewDidChange(text: String, position: Int)
    fun selectedDeleteButton(text: String, position: Int)
}