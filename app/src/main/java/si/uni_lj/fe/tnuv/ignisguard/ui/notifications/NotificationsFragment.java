package si.uni_lj.fe.tnuv.ignisguard.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import si.uni_lj.fe.tnuv.ignisguard.databinding.FragmentNotificationsBinding;
import si.uni_lj.fe.tnuv.ignisguard.ui.common.Sensor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import android.graphics.Color;
import si.uni_lj.fe.tnuv.ignisguard.R;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private RecyclerView recyclerView;
    private final List<Event> events = new ArrayList<>();
    private EventAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerEvents;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        generateRandomEvents();
        adapter = new EventAdapter(events);
        recyclerView.setAdapter(adapter);
        return root;
    }

    private void generateRandomEvents() {
        events.clear();
        String[] sensorNames = {"Tivoli", "Rožnik Sever", "Rožnik Jug", "Bled"};
        String[] statuses = {"Normal", "Wind", "Rain", "Fire"};
        Random random = new Random();
        Calendar now = Calendar.getInstance();
        for (String sensor : sensorNames) {
            int eventCount = 2 + random.nextInt(3); // 2-4 events per sensor
            String prevStatus = statuses[random.nextInt(statuses.length)];
            Calendar eventTime = (Calendar) now.clone();
            for (int i = 0; i < eventCount; i++) {
                String newStatus;
                do {
                    newStatus = statuses[random.nextInt(statuses.length)];
                } while (newStatus.equals(prevStatus));
                eventTime.add(Calendar.MINUTE, -random.nextInt(120) - 10); // randomize time
                String description = sensor + " changed status from " + prevStatus + " to " + newStatus;
                events.add(new Event((Calendar) eventTime.clone(), description, newStatus));
                prevStatus = newStatus;
            }
        }
        Collections.sort(events, (a, b) -> b.time.compareTo(a.time));
    }

    static class Event {
        Calendar time;
        String description;
        String status;
        Event(Calendar time, String description, String status) {
            this.time = time;
            this.description = description;
            this.status = status;
        }
    }

    static class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
        private final List<Event> events;
        EventAdapter(List<Event> events) {
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
            Event event = events.get(position);
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
        binding = null;
    }
} 