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

import si.uni_lj.fe.tnuv.ignisguard.R;
import si.uni_lj.fe.tnuv.ignisguard.databinding.FragmentDashboardBinding;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView.OnEditorActionListener;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private final List<Sensor> sensors = new java.util.ArrayList<>();
    private SensorAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerSensors;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        sensors.clear();
        sensors.addAll(Arrays.asList(
                new Sensor(getString(R.string.sensor_tivoli), 88, getString(R.string.status_normal)),
                new Sensor(getString(R.string.sensor_roznik_sever), 71, getString(R.string.status_wind)),
                new Sensor(getString(R.string.sensor_roznik_jug), 69, getString(R.string.status_normal)),
                new Sensor(getString(R.string.sensor_bled), 93, getString(R.string.status_rain))
        ));
        adapter = new SensorAdapter(sensors);
        recyclerView.setAdapter(adapter);

        binding.buttonAddSensor.setOnClickListener(v -> showAddSensorDialog());
        return root;
    }

    private void showAddSensorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.dialog_add_sensor_title));
        final EditText input = new EditText(getContext());
        input.setHint(getString(R.string.dialog_sensor_name_hint));
        input.setSingleLine();
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        builder.setView(input);

        DialogInterface.OnClickListener addListener = (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                Random random = new Random();
                int battery = 50 + random.nextInt(51); // 50-100
                String[] statuses = {
                        getString(R.string.status_normal),
                        getString(R.string.status_wind),
                        getString(R.string.status_rain)
                };
                String status = statuses[random.nextInt(statuses.length)];
                sensors.add(new Sensor(name, battery, status));
                adapter.notifyItemInserted(sensors.size() - 1);
            }
        };

        builder.setPositiveButton(getString(R.string.dialog_add), addListener);
        builder.setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                dialog.dismiss();
                return true;
            }
            return false;
        });
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    static class Sensor {
        String name;
        int battery;
        String status;
        Sensor(String name, int battery, String status) {
            this.name = name;
            this.battery = battery;
            this.status = status;
        }
    }

    static class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.SensorViewHolder> {
        private final List<Sensor> sensors;
        SensorAdapter(List<Sensor> sensors) {
            this.sensors = sensors;
        }
        @NonNull
        @Override
        public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sensor, parent, false);
            return new SensorViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {
            Sensor sensor = sensors.get(position);
            holder.name.setText(sensor.name);
            holder.battery.setText(holder.battery.getContext().getString(R.string.sensor_battery, sensor.battery));
            holder.status.setText(holder.status.getContext().getString(R.string.sensor_status, sensor.status));
            if (holder.status.getContext().getString(R.string.status_fire).equals(sensor.status)) {
                holder.status.setTextColor(holder.status.getContext().getResources().getColor(R.color.orange_500));
            } else {
                holder.status.setTextColor(holder.status.getContext().getResources().getColor(R.color.black));
            }
        }
        @Override
        public int getItemCount() {
            return sensors.size();
        }
        static class SensorViewHolder extends RecyclerView.ViewHolder {
            TextView name, battery, status;
            SensorViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.text_sensor_name);
                battery = itemView.findViewById(R.id.text_sensor_battery);
                status = itemView.findViewById(R.id.text_sensor_status);
            }
        }
    }
} 