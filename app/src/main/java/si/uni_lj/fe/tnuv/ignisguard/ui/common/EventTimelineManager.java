package si.uni_lj.fe.tnuv.ignisguard.ui.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventTimelineManager {
    private static EventTimelineManager instance;
    private static final String EVENTS_KEY = "events_list";
    private final List<Event> events = new ArrayList<>();
    private SharedPreferences prefs;
    private Gson gson = new Gson();

    public static class Event {
        public Calendar time;
        public String description;
        public String status;
        public Event(Calendar time, String description, String status) {
            this.time = time;
            this.description = description;
            this.status = status;
        }
    }

    private EventTimelineManager(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        loadEvents();
    }

    public static synchronized EventTimelineManager getInstance(Context context) {
        if (instance == null) {
            instance = new EventTimelineManager(context);
        }
        return instance;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void addEvent(Event event) {
        events.add(0, event);
        saveEvents();
    }

    private void loadEvents() {
        String json = prefs.getString(EVENTS_KEY, null);
        if (json != null) {
            Type type = new TypeToken<List<Event>>(){}.getType();
            List<Event> loadedEvents = gson.fromJson(json, type);
            events.clear();
            events.addAll(loadedEvents);
        }
    }

    private void saveEvents() {
        prefs.edit().putString(EVENTS_KEY, gson.toJson(events)).apply();
    }
} 