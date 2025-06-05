package com.example.tnuv_projekt.utils

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.tnuv_projekt.R
import com.google.android.material.button.MaterialButton

class FireAlertDialog : DialogFragment() {
    private var sensorName: String = ""

    companion object {
        fun newInstance(sensorName: String): FireAlertDialog {
            return FireAlertDialog().apply {
                this.sensorName = sensorName
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.Theme_AppCompat_Light_Dialog_Alert)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_fire_alert, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tv_sensor_name).text = sensorName

        view.findViewById<ImageButton>(R.id.btn_close).setOnClickListener {
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.btn_call_firefighters).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:112")
            }
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
} 