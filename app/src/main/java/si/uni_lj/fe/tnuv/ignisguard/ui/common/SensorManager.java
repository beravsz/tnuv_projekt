package si.uni_lj.fe.tnuv.ignisguard.ui.common;

import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import android.content.Context;
import android.util.Log;
import androidx.fragment.app.FragmentActivity;

public class SensorManager {
    private static SensorManager instance;
    private final List<Sensor> sensors = new ArrayList<>();
    private final List<OnSensorUpdateListener> listeners = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private boolean isRunning = false;
    private static final long UPDATE_INTERVAL = 10000; // 10 seconds in milliseconds
    private static Context globalContext;
    private static FragmentActivity currentActivity;

    public interface OnSensorUpdateListener {
        void onSensorUpdated(Sensor sensor, String oldStatus);
    }

    private SensorManager() {
        // No default sensors. Sensors list starts empty.
    }

    public static synchronized SensorManager getInstance() {
        if (instance == null) {
            instance = new SensorManager();
        }
        return instance;
    }

    public List<Sensor> getSensors() {
        return new ArrayList<>(sensors);
    }

    public void addSensor(Sensor sensor) {
        sensors.add(sensor);
        notifyListeners(sensor, null);
    }

    public void removeSensor(Sensor sensor) {
        sensors.remove(sensor);
    }

    public void addListener(OnSensorUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(OnSensorUpdateListener listener) {
        listeners.remove(listener);
    }

    public static void setGlobalContext(Context context) {
        globalContext = context.getApplicationContext();
    }

    public static void setCurrentActivity(FragmentActivity activity) {
        currentActivity = activity;
    }

    private void notifyListeners(Sensor sensor, String oldStatus) {
        if (oldStatus != null && !oldStatus.equals(sensor.status) && globalContext != null) {
            EventTimelineManager timeline = EventTimelineManager.getInstance(globalContext);
            Calendar now = Calendar.getInstance();
            String description = sensor.name + " changed status from " + oldStatus + " to " + sensor.status;
            timeline.addEvent(new EventTimelineManager.Event(now, description, sensor.status));
        }
        for (OnSensorUpdateListener listener : listeners) {
            listener.onSensorUpdated(sensor, oldStatus);
        }
    }

    public void startUpdates() {
        if (!isRunning) {
            isRunning = true;
            scheduleNextUpdate();
        }
    }

    public void stopUpdates() {
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
    }

    private void scheduleNextUpdate() {
        if (!isRunning) return;

        handler.postDelayed(() -> {
            if (!sensors.isEmpty()) {
                // Change status for all sensors
                for (int i = 0; i < sensors.size(); i++) {
                    Sensor sensor = sensors.get(i);
                    String oldStatus = sensor.status;
                    // Generate new status (including Fire)
                    String[] statuses = {"Normal", "Wind", "Rain", "Fire"};
                    String newStatus;
                    do {
                        newStatus = statuses[random.nextInt(statuses.length)];
                    } while (newStatus.equals(oldStatus));
                    // Update sensor status
                    sensor.status = newStatus;
                    Log.d("SensorManager", "Changed status for sensor " + (i + 1) + " (" + sensor.name + ") from " + oldStatus + " to " + newStatus);
                    
                    // Show fire alert dialog if status changed to Fire
                    if (newStatus.equals("Fire") && currentActivity != null) {
                        handler.post(() -> {
                            FireAlertDialog.newInstance(sensor.name)
                                .show(currentActivity.getSupportFragmentManager(), "fire_alert");
                        });
                    }
                    
                    notifyListeners(sensor, oldStatus);
                }
            } else {
                Log.d("SensorManager", "No sensors in list.");
            }
            scheduleNextUpdate();
        }, UPDATE_INTERVAL);
    }
} 