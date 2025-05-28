package com.example.medijourney.common.ui_components.fragments.web_view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.medijourney.databinding.FragmentWebViewBinding

class WebViewFragment : Fragment() {

    // Properties
    private lateinit var binding: FragmentWebViewBinding
    private lateinit var viewModel: WebViewModel
    private val webModel: WebViewFragmentArgs by navArgs()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = WebViewModel()
        binding = FragmentWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        (activity as? AppCompatActivity)?.supportActionBar?.title = viewModel.getWebActivityTitle(webModel.webModel)
        viewModel.handleWebViewInputData(webModel.webModel)
    }

    // Function
    private fun observeViewModel() {
        viewModel.uri.observe(viewLifecycleOwner) {
            binding.webView.webViewClient = CustomWebViewClient()
            binding.webView.loadUrl(it)
        }
        viewModel.htmlString.observe(viewLifecycleOwner) {
            binding.webView.loadDataWithBaseURL(
                null, it,
                "text/html", "UTF-8",
                null
            )
        }
    }
}

private class CustomWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val uri = request.url
        view.loadUrl(uri.toString())
        return true
    }
}