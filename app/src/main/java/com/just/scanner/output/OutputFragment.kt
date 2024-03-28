package com.just.scanner.output

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.just.scanner.base.BaseFragment
import com.just.scanner.databinding.FragmentOutputBinding
import com.just.scanner.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class OutputFragment : BaseFragment<HomeViewModel, FragmentOutputBinding>() {

    var filename = ""

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allPermissionsGranted = permissions.entries.all { it.value }
            if (allPermissionsGranted) {
                // All permissions are granted. Proceed with the action.
//                pdfUri?.let { downloadPdf(it) }
            } else {
                // Permission denied. Handle the case where permission is not granted.
                Toast.makeText(
                    requireContext(),
                    "Permission required to download the PDF.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentOutputBinding.inflate(inflater, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pdfUri: Uri? = arguments?.getParcelable("pdfUri")

        if (pdfUri != null) {
            Log.d("pdf", "$pdfUri")
            previewPdf(pdfUri)
            listners(pdfUri)
            filename = pdfUri.toString().substringAfterLast('/')
            binding.tvSkuName.setText(filename)
        }

    }

    private fun listners(pdfUri: Uri) {
        pdfUri?.let { uri ->
            binding.btShare.setOnClickListener {
                try {
                    val externalUri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().applicationContext.packageName}.provider",
                        File(uri.path)
                    )

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_STREAM, externalUri)
                        type = "application/pdf"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share PDF"))
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(), "Error sharing file: ${e.message}", Toast.LENGTH_SHORT
                    ).show()
                    Log.d("PDF", "viewPdf: ${e.message}")
                    e.printStackTrace()
                }
            }
//            binding.btDownload.setOnClickListener {
//                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                    // Permissions are already granted
//                    downloadPdf(pdfUri)
//                } else {
//                    // Request permissions
//                    requestPermissionLauncher.launch(
//                        arrayOf(
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                            Manifest.permission.READ_EXTERNAL_STORAGE
//                        )
//                    )
//                }
//            }
        }

        binding.ivBackButton.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this@OutputFragment).commit()
        }
    }

    private fun downloadPdf(pdfUri: Uri) {
        lifecycleScope.launch {
            copyPdfToJustScannerFolder(requireContext(), pdfUri, "$filename.pdf")
        }
    }


    private fun previewPdf(pdfUri: Uri?) {
        pdfUri?.let { uri ->
            binding.pdfView.initWithUrl(
                url = uri.toString(),
                lifecycleCoroutineScope = lifecycleScope,
                lifecycle = lifecycle
            )
        }
    }

    fun generateUUID(): String {
        val uuid: UUID = UUID.randomUUID()
        return uuid.toString()
    }

    private suspend fun copyPdfToJustScannerFolder(
        context: Context,
        sourceUri: Uri,
        fileName: String
    ) {
        val destinationFolder =
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Just Scanner")
        if (!destinationFolder.exists()) destinationFolder.mkdirs()
        val destinationFile = File(destinationFolder, fileName)

        try {
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw IOException("Unable to open input stream")
            // File copy successful
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Pdf downloaded successfully to Just Scanner folder.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error downloading PDF: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

}