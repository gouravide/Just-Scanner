package com.just.scanner.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.appcompat.app.AppCompatDelegate
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.just.scanner.R
import com.just.scanner.databinding.ActivityMainBinding
import com.just.scanner.output.OutputFragment

class MainActivity : AppCompatActivity() {

    private lateinit var firstPageView: ImageView
    private lateinit var binding: ActivityMainBinding
    private lateinit var pageLimitInputView: EditText
    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private val FULL_MODE = "FULL"
    private val BASE_MODE = "BASE"
    private val BASE_MODE_WITH_FILTER = "BASE_WITH_FILTER"
    private var selectedMode = FULL_MODE

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(binding.root)

        scannerLauncher = registerForActivityResult(StartIntentSenderForResult()) { result ->
            handleActivityResult(result)
        }

        listners()
    }

    private fun listners(){
        binding.cvShareApp.setOnClickListener {
            shareApp()
        }
        binding.cvRateApp.setOnClickListener {
            rateApp()
        }
    }


    @SuppressLint("SuspiciousIndentation")
    @Suppress("UNUSED_PARAMETER")
    fun onScanButtonClicked(unused: View) {
//    resultInfo.text = null
//        Glide.with(this).clear(firstPageView)

        val options = GmsDocumentScannerOptions.Builder()
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
            .setGalleryImportAllowed(true)

        options.setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)

        val pageLimitInputText = 50
        try {
            val pageLimit = pageLimitInputText.toInt()
            options.setPageLimit(pageLimit)
        } catch (e: Throwable) {
            Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            return
        }

        GmsDocumentScanning.getClient(options.build()).getStartScanIntent(this)
            .addOnSuccessListener { intentSender: IntentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }.addOnFailureListener() { e: Exception ->
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleActivityResult(activityResult: ActivityResult) {
        val resultCode = activityResult.resultCode
        val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
        if (resultCode == Activity.RESULT_OK && result != null) {
            // Assuming the scan was successful and you have a result

            result.pdf?.uri?.let { pdfUri ->
                val outputFragment = OutputFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("pdfUri", pdfUri) // Pass the PDF Uri to the fragment
                    }
                }

                // Perform a Fragment transaction to replace the current Fragment with OutputFragment
                supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, outputFragment, "OutputFragment")
                    .addToBackStack(null)
                    .commit()
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
          Toast.makeText(this, "Scanner is cancelled.", Toast.LENGTH_SHORT).show()
        } else {
          Toast.makeText(this, "Error in scanning document, please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareApp() {
        val shareMessage = "Check out this amazing PDF scanner app: https://play.google.com/store/apps/details?id=${this.packageName}"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        this.startActivity(Intent.createChooser(shareIntent, "Share App"))
    }

    fun rateApp() {
        val packageName = this.packageName
        try {
            // Try to open the app's Google Play page
            this.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: android.content.ActivityNotFoundException) {
            // Fallback to browser if the Google Play app is not installed
            this.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
