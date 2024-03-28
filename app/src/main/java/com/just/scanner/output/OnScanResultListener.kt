package com.just.scanner.output

import android.net.Uri

interface OnScanResultListener {
    fun onPdfGenerated(pdfUri: Uri)
}
