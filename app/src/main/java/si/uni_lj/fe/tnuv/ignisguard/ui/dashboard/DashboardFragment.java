package si.uni_lj.fe.tnuv.ignisguard.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.EditText;
import android.content.DialogInterface;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.graphics.Color;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import si.uni_lj.fe.tnuv.ignisguard.R;
import si.uni_lj.fe.tnuv.ignisguard.databinding.FragmentDashboardBinding;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView.OnEditorActionListener;
import si.uni_lj.fe.tnuv.ignisguard.ui.common.Sensor;
import si.uni_lj.fe.tnuv.ignisguard.ui.common.SensorAdapter;
import si.uni_lj.fe.tnuv.ignisguard.ui.common.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.IOException;
import java.util.Locale;

public class DashboardFragment extends Fragment implements SensorManager.OnSensorUpdateListener {

    private FragmentDashboardBinding binding;
    private List<Sensor> sensors;
    private SensorAdapter adapter;
    private SharedPreferences prefs;
    private Gson gson = new Gson();
    private static final String SENSORS_KEY = "sensors_list";
    private SensorManager sensorManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize sensor manager early
        sensorManager = SensorManager.getInstance();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        
        // Use the sensors list from SensorManager directly
        sensorManager = SensorManager.getInstance();
        // Load saved sensors into SensorManager if empty
        if (sensorManager.getSensors().isEmpty()) {
            List<Sensor> savedSensors = loadSensors();
            for (Sensor s : savedSensors) {
                sensorManager.addSensor(s);
            }
        }
        sensors = sensorManager.getSensors();
        
        RecyclerView recyclerView = binding.recyclerSensors;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SensorAdapter(sensors);
        recyclerView.setAdapter(adapter);

        // Add delete listener
        adapter.setOnDeleteClickListener(position -> {
            new AlertDialog.Builder(getContext())
                .setTitle("Delete this sensor?")
                .setMessage("Are you sure you want to delete this sensor?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Sensor sensorToRemove = sensors.get(position);
                    sensors.remove(position);
                    sensorManager.removeSensor(sensorToRemove);
                    adapter.notifyItemRemoved(position);
                    saveSensors();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
        });

        // Initialize sensor manager and start updates
        sensorManager.addListener(this);
        sensorManager.startUpdates();

        binding.buttonAddSensor.setOnClickListener(v -> showAddSensorDialog());
        return root;
    }

    @Override
    public void onSensorUpdated(Sensor sensor, String oldStatus) {
        // Find and update the sensor in our list
        for (int i = 0; i < sensors.size(); i++) {
            if (sensors.get(i).name.equals(sensor.name)) {
                // No need to set, as it's the same object; just notify the adapter
                adapter.notifyItemChanged(i);
                saveSensors(); // Save the updated values
                break;
            }
        }
    }

    private List<Sensor> loadSensors() {
        String json = prefs.getString(SENSORS_KEY, null);
        if (json != null) {
            Type type = new TypeToken<List<Sensor>>(){}.getType();
            return gson.fromJson(json, type);
        } else {
            // No default sensors. Start with empty list.
            return new ArrayList<>();
        }
    }

    private void saveSensors() {
        prefs.edit().putString(SENSORS_KEY, gson.toJson(sensors)).apply();
    }

    private void showAddSensorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.dialog_add_sensor_title));
        final EditText inputName = new EditText(getContext());
        inputName.setHint(getString(R.string.dialog_sensor_name_hint));
        inputName.setSingleLine();
        inputName.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        // Latitude row
        LinearLayout latRow = new LinearLayout(getContext());
        latRow.setOrientation(LinearLayout.HORIZONTAL);
        final EditText latDeg = new EditText(getContext());
        latDeg.setHint("deg");
        latDeg.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        latDeg.setEms(3);
        final EditText latMin = new EditText(getContext());
        latMin.setHint("min");
        latMin.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        latMin.setEms(2);
        final EditText latSec = new EditText(getContext());
        latSec.setHint("sec");
        latSec.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        latSec.setEms(4);
        latRow.addView(latDeg);
        latRow.addView(latMin);
        latRow.addView(latSec);

        // Longitude row
        LinearLayout lonRow = new LinearLayout(getContext());
        lonRow.setOrientation(LinearLayout.HORIZONTAL);
        final EditText lonDeg = new EditText(getContext());
        lonDeg.setHint("deg");
        lonDeg.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        lonDeg.setEms(3);
        final EditText lonMin = new EditText(getContext());
        lonMin.setHint("min");
        lonMin.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        lonMin.setEms(2);
        final EditText lonSec = new EditText(getContext());
        lonSec.setHint("sec");
        lonSec.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        lonSec.setEms(4);
        lonRow.addView(lonDeg);
        lonRow.addView(lonMin);
        lonRow.addView(lonSec);

        // Labels
        TextView latLabel = new TextView(getContext());
        latLabel.setText("Latitude");
        TextView lonLabel = new TextView(getContext());
        lonLabel.setText("Longitude");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(inputName);
        layout.addView(latLabel);
        layout.addView(latRow);
        layout.addView(lonLabel);
        layout.addView(lonRow);
        builder.setView(layout);

        DialogInterface.OnClickListener addListener = (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            if (!name.isEmpty()) {
                Random random = new Random();
                int battery = 50 + random.nextInt(51); // 50-100
                String[] statuses = {"Normal", "Wind", "Rain"};
                String status = statuses[random.nextInt(statuses.length)];
                double lat = 0, lon = 0;
                try {
                    int latD = Integer.parseInt(latDeg.getText().toString());
                    int latM = Integer.parseInt(latMin.getText().toString());
                    double latS = Double.parseDouble(latSec.getText().toString());
                    int lonD = Integer.parseInt(lonDeg.getText().toString());
                    int lonM = Integer.parseInt(lonMin.getText().toString());
                    double lonS = Double.parseDouble(lonSec.getText().toString());
                    lat = dmsToDecimal(latD, latM, latS);
                    lon = dmsToDecimal(lonD, lonM, lonS);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Invalid DMS input, using (0,0)", Toast.LENGTH_SHORT).show();
                }
                Sensor newSensor = new Sensor(name, battery, status, lat, lon);
                sensorManager.addSensor(newSensor);
                sensors.add(newSensor);
                adapter.notifyItemInserted(sensors.size() - 1);
                saveSensors();
            }
        };

        builder.setPositiveButton(getString(R.string.dialog_add), addListener);
        builder.setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private double dmsToDecimal(int deg, int min, double sec) {
        double dec = Math.abs(deg) + min / 60.0 + sec / 3600.0;
        return deg < 0 ? -dec : dec;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (sensorManager != null) {
            sensorManager.removeListener(this);
        }
        binding = null;
    }
} 