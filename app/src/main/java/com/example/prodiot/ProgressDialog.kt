package com.example.prodiot

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater

class CustomProgressDialog(private val context: Context?) {
    private var dialog: AlertDialog? = null

    fun show() {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_progress, null)
        builder.setView(dialogView)
        builder.setCancelable(false)

        dialog = builder.create()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}

class answerProgressDialog(private val context: Context?) {
    private var dialog: AlertDialog? = null

    fun show() {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.answer_progress, null)
        builder.setView(dialogView)
        builder.setCancelable(true)

        // 다이얼로그를 클릭하면 닫히도록 클릭 리스너 추가
        dialog?.setOnCancelListener {
            dismiss()
        }

        dialog = builder.create()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}

class wrong_answerProgressDialog(private val context: Context?) {
    private var dialog: AlertDialog? = null

    fun show() {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.wrong_answer_progress, null)
        builder.setView(dialogView)
        builder.setCancelable(true)

        // 다이얼로그를 클릭하면 닫히도록 클릭 리스너 추가
        dialog?.setOnCancelListener {
            dismiss()
        }

        dialog = builder.create()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}
