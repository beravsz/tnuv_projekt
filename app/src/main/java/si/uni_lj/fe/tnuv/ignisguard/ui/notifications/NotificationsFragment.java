package si.uni_lj.fe.tnuv.ignisguard.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import si.uni_lj.fe.tnuv.ignisguard.databinding.FragmentNotificationsBinding;
import si.uni_lj.fe.tnuv.ignisguard.ui.common.Sensor;
import si.uni_lj.fe.tnuv.ignisguard.ui.common.SensorManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import android.graphics.Color;
import si.uni_lj.fe.tnuv.ignisguard.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import si.uni_lj.fe.tnuv.ignisguard.ui.common.EventTimelineManager;
import java.util.HashSet;
import java.util.Set;

public class NotificationsFragment extends Fragment implements SensorManager.OnSensorUpdateListener {

    private FragmentNotificationsBinding binding;
    private RecyclerView recyclerView;
    private EventTimelineManager timelineManager;
    private EventAdapter adapter;
    private SensorManager sensorManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = SensorManager.getInstance();
        timelineManager = EventTimelineManager.getInstance(requireContext());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerEvents;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Filter events to only those relevant to current sensors
        List<Sensor> currentSensors = sensorManager.getSensors();
        Set<String> sensorNames = new HashSet<>();
        for (Sensor s : currentSensors) sensorNames.add(s.name);
        List<EventTimelineManager.Event> filteredEvents = new ArrayList<>();
        for (EventTimelineManager.Event event : timelineManager.getEvents()) {
            String sensorName = extractSensorName(event.description);
            if (sensorNames.contains(sensorName)) {
                filteredEvents.add(event);
            }
        }
        adapter = new EventAdapter(filteredEvents);
        recyclerView.setAdapter(adapter);

        // Initialize sensor manager and start updates
        sensorManager.addListener(this);
        sensorManager.startUpdates();

        return root;
    }

    // Helper to extract sensor name from event description
    private String extractSensorName(String description) {
        // Assumes format: "<SensorName> changed status from ..."
        int idx = description.indexOf(" changed status");
        if (idx > 0) {
            return description.substring(0, idx);
        }
        return description;
    }

    @Override
    public void onSensorUpdated(Sensor sensor, String oldStatus) {
        // Refresh filtered events on sensor update
        List<Sensor> currentSensors = sensorManager.getSensors();
        Set<String> sensorNames = new HashSet<>();
        for (Sensor s : currentSensors) sensorNames.add(s.name);
        List<EventTimelineManager.Event> filteredEvents = new ArrayList<>();
        for (EventTimelineManager.Event event : timelineManager.getEvents()) {
            String sensorName = extractSensorName(event.description);
            if (sensorNames.contains(sensorName)) {
                filteredEvents.add(event);
            }
        }
        adapter.events.clear();
        adapter.events.addAll(filteredEvents);
        adapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(0);
    }

    static class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
        final List<EventTimelineManager.Event> events;
        EventAdapter(List<EventTimelineManager.Event> events) {
            this.events = events;
        }
        @NonNull
        @Override
        public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
            return new EventViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
            EventTimelineManager.Event event = events.get(position);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");
            holder.time.setText(sdf.format(event.time.getTime()));
            holder.description.setText(event.description);
            if ("Fire".equals(event.status)) {
                holder.description.setTextColor(Color.parseColor("#FF9800"));
            } else {
                holder.description.setTextColor(Color.BLACK);
            }
        }
        @Override
        public int getItemCount() {
            return events.size();
        }
        static class EventViewHolder extends RecyclerView.ViewHolder {
            TextView time, description;
            EventViewHolder(@NonNull View itemView) {
                super(itemView);
                time = itemView.findViewById(R.id.text_event_time);
                description = itemView.findViewById(R.id.text_event_description);
            }
        }
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