package com.example.medijourney.modules.profile.share_app

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.content_image.ContentImageAdapter
import com.example.medijourney.databinding.FragmentShareAppBinding
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ShareAppFragment : BottomSheetDialogFragment(), BaseAdapterInterface {

    // Properties
    private lateinit var binding: FragmentShareAppBinding
    private val viewModel: ShareAppViewModel by viewModels()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShareAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
    }

    // Functions
    private fun setupView() {
        binding.recycleView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun observeViewModel() {
        viewModel.items.observe(viewLifecycleOwner) {
            val adapter = binding.recycleView.adapter as? ContentImageAdapter
            if (adapter == null) {
                val newAdapter = ContentImageAdapter(it).apply {
                    output = this@ShareAppFragment
                }
                binding.recycleView.adapter = newAdapter
            } else {
                adapter.updateItems(it)
            }
        }
    }

    // BaseAdapterInterface
    @Suppress("UNCHECKED_CAST")
    override fun selectedItem(model: BaseItemInterface?) {
        val data = model?.data as? Map<String, Any> ?: return

        when (model.itemTag) {
            "share_via_facebook" -> {
                handleShareViaFacebook(data)
            }
            "share_via_telegram" -> {
                handleShareViaTelegram(data)
            }
            "copy_share_link" -> {
                handleCopyShareLink(data)
            }
            else -> {}
        }
    }

    private fun handleShareViaFacebook(data: Map<String, Any>) {
        val shareUrl = viewModel.getShareUri(data)
        val shareContent = viewModel.getShareContent(data)
        val content = ShareLinkContent.Builder()
            .setContentUrl(shareUrl)
            .setQuote(shareContent)
            .build()
        val shareDialog = ShareDialog()
        shareDialog.show(content)
    }

    private fun handleShareViaTelegram(data: Map<String, Any>) {
        val text = viewModel.getTelegramShareContent(data)
        val telegramIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("org.telegram.messenger")
        }

        try {
            requireContext().startActivity(telegramIntent)
        } catch (_: ActivityNotFoundException) { }
    }

    private fun handleCopyShareLink(data: Map<String, Any>) {
        val shareUrl = viewModel.getShareUri(data)
        val clipboard = getSystemService(requireContext(), ClipboardManager::class.java) ?: return
        val clip = ClipData.newUri(requireContext().contentResolver, "URI", shareUrl)
        clipboard.setPrimaryClip(clip)
    }
}