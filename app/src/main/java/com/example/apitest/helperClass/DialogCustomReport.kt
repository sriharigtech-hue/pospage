package com.example.apitest.helperClass

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.example.apitest.R
import com.google.android.material.bottomsheet.BottomSheetDialog

object DialogCustomReport {

    fun showCustomizedReportDialog(context: Context) {
        val dialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_customized_report_support, null)
        dialog.setContentView(view)
        dialog.setCancelable(true)

        val closeDialog = view.findViewById<androidx.appcompat.widget.AppCompatImageView>(R.id.closeDialog)
        val callSupport = view.findViewById<LinearLayout>(R.id.callSupport)
        val whatsAppSupport = view.findViewById<LinearLayout>(R.id.whatsAppSupport)
        val emailSupport = view.findViewById<LinearLayout>(R.id.emailSupport)

        closeDialog.setOnClickListener { dialog.dismiss() }

        //  Call
        callSupport.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:9987654356")
            context.startActivity(intent)
        }

        //  WhatsApp
        whatsAppSupport.setOnClickListener {
            val url = "https://wa.me/919987654356"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        }

        //  Email
        emailSupport.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:support@gtechsolution.com")
            intent.putExtra(Intent.EXTRA_SUBJECT, "Customized Report Request")
            context.startActivity(intent)
        }

        dialog.show()
    }
}
