package com.example.medijourney.common.ui_components.recycle_view_adapter.otp

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.databinding.ItemOptViewBinding

class OTPAdapter(private var digits: List<String>) : RecyclerView.Adapter<OTPAdapter.OTPViewHolder>() {

    // Properties
    var listener: OTPAdapterListener? = null

    // Life cycle
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OTPViewHolder {
        val binding = ItemOptViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OTPViewHolder(binding)
    }

    inner class OTPViewHolder(binding: ItemOptViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val editTextDigit = binding.editTextDigit

        init {
            setupTextWatcher()
            setupKeyListener()
        }

        private fun setupTextWatcher() {
            editTextDigit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (editTextDigit.text.isNotEmpty()) {
                        listener?.textViewDidChange(editTextDigit.text.toString(), adapterPosition)
                        editTextDigit.setSelection(editTextDigit.text.length)
                    }
                }

                override fun afterTextChanged(p0: Editable?) {}
            })
        }

        private fun setupKeyListener() {
            editTextDigit.setOnKeyListener { _, i, event ->
                if (i == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_UP) {
                    listener?.selectedDeleteButton("", adapterPosition)
                } else if (editTextDigit.text.isNotEmpty() && event.action == KeyEvent.ACTION_UP) {
                    editTextDigit.setText(event.displayLabel.toString())
                }
                false
            }
        }
    }

    override fun onBindViewHolder(holder: OTPViewHolder, position: Int) {
        val digit = digits[position]
        holder.editTextDigit.setText(digit)
    }

    override fun getItemCount(): Int {
        return digits.size
    }
}