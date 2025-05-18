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
import android.location.Address;
import android.location.Geocoder;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.IOException;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private final List<Sensor> sensors = new java.util.ArrayList<>();
    private SensorAdapter adapter;
    private SharedPreferences prefs;
    private Gson gson = new Gson();
    private static final String SENSORS_KEY = "sensors_list";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        sensors.clear();
        sensors.addAll(loadSensors());
        adapter = new SensorAdapter(sensors);
        adapter.setOnDeleteClickListener(position -> {
            new AlertDialog.Builder(getContext())
                .setTitle("Delete this sensor?")
                .setMessage("Are you sure you want to delete this sensor?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    sensors.remove(position);
                    adapter.notifyItemRemoved(position);
                    saveSensors();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
        });
        RecyclerView recyclerView = binding.recyclerSensors;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        binding.buttonAddSensor.setOnClickListener(v -> showAddSensorDialog());
        return root;
    }

    private List<Sensor> loadSensors() {
        String json = prefs.getString(SENSORS_KEY, null);
        if (json != null) {
            Type type = new TypeToken<List<Sensor>>(){}.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>(Arrays.asList(
                new Sensor(getString(R.string.sensor_tivoli), 88, getString(R.string.status_normal)),
                new Sensor(getString(R.string.sensor_roznik_sever), 71, getString(R.string.status_wind)),
                new Sensor(getString(R.string.sensor_roznik_jug), 69, getString(R.string.status_normal)),
                new Sensor(getString(R.string.sensor_bled), 93, getString(R.string.status_rain))
            ));
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

        final EditText inputAddress = new EditText(getContext());
        inputAddress.setHint("Sensor Address");
        inputAddress.setSingleLine();
        inputAddress.setImeOptions(EditorInfo.IME_ACTION_DONE);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(inputName);
        layout.addView(inputAddress);
        builder.setView(layout);

        DialogInterface.OnClickListener addListener = (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String address = inputAddress.getText().toString().trim();
            if (!name.isEmpty() && !address.isEmpty()) {
                Random random = new Random();
                int battery = 50 + random.nextInt(51); // 50-100
                String[] statuses = {"Normal", "Wind", "Rain"};
                String status = statuses[random.nextInt(statuses.length)];
                double lat = 0, lon = 0;
                double[] dmsCoords = parseDMS(address);
                if (dmsCoords != null) {
                    lat = dmsCoords[0];
                    lon = dmsCoords[1];
                } else {
                    try {
                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                        java.util.List<Address> addresses = geocoder.getFromLocationName(address, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            lat = addresses.get(0).getLatitude();
                            lon = addresses.get(0).getLongitude();
                        } else {
                            Toast.makeText(getContext(), "Address not found, using (0,0)", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Geocoding failed, using (0,0)", Toast.LENGTH_SHORT).show();
                    }
                }
                sensors.add(new Sensor(name, battery, status, lat, lon));
                adapter.notifyItemInserted(sensors.size() - 1);
                saveSensors();
            }
        };

        builder.setPositiveButton(getString(R.string.dialog_add), addListener);
        builder.setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        inputAddress.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                dialog.dismiss();
                return true;
            }
            return false;
        });
        dialog.show();
    }

    private double[] parseDMS(String input) {
        // Example: 46°10'12.0"N 14°19'53.8"E
        try {
            String[] parts = input.split(" ");
            if (parts.length != 2) return null;
            double lat = dmsToDecimal(parts[0]);
            double lon = dmsToDecimal(parts[1]);
            return new double[]{lat, lon};
        } catch (Exception e) {
            return null;
        }
    }

    private double dmsToDecimal(String dms) {
        // Example: 46°10'12.0"N or 14°19'53.8"E
        dms = dms.replace("\u00B0", "°"); // normalize degree symbol
        String regex = "(\\d+)°(\\d+)'(\\d+(?:\\.\\d+)?)\"?([NSEW])";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(dms);
        if (!matcher.matches()) throw new IllegalArgumentException("Invalid DMS");
        int deg = Integer.parseInt(matcher.group(1));
        int min = Integer.parseInt(matcher.group(2));
        double sec = Double.parseDouble(matcher.group(3));
        String dir = matcher.group(4);
        double dec = deg + min / 60.0 + sec / 3600.0;
        if (dir.equals("S") || dir.equals("W")) dec = -dec;
        return dec;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 