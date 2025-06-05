package si.uni_lj.fe.tnuv.ignisguard.ui.common;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;
import si.uni_lj.fe.tnuv.ignisguard.R;
import com.google.android.material.button.MaterialButton;

public class FireAlertDialog extends DialogFragment {
    private String sensorName = "";

    public static FireAlertDialog newInstance(String sensorName) {
        FireAlertDialog dialog = new FireAlertDialog();
        dialog.sensorName = sensorName;
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_IgnisGuard);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fire_alert, container, false);
        
        // Set sensor name
        TextView sensorNameView = view.findViewById(R.id.tv_sensor_name);
        sensorNameView.setText(sensorName);

        // Set up close button
        ImageButton closeButton = view.findViewById(R.id.btn_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // Set up call button
        MaterialButton callButton = view.findViewById(R.id.btn_call_firefighters);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:112"));
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
} 