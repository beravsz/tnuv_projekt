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

public class SensorManager {
    private static SensorManager instance;
    private final List<Sensor> sensors = new ArrayList<>();
    private final List<OnSensorUpdateListener> listeners = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private boolean isRunning = false;
    private final Map<String, Long> lastStatusChangeTime = new HashMap<>();
    private static final long MIN_STATUS_DURATION = 30000; // 30 seconds in milliseconds
    private static final long UPDATE_INTERVAL = 10000; // 10 seconds in milliseconds

    public interface OnSensorUpdateListener {
        void onSensorUpdated(Sensor sensor, String oldStatus);
    }

    private SensorManager() {
        // Initialize with default sensors
        sensors.add(new Sensor("Tivoli", 88, "Normal"));
        sensors.add(new Sensor("Rožnik Sever", 71, "Wind"));
        sensors.add(new Sensor("Rožnik Jug", 69, "Normal"));
        sensors.add(new Sensor("Bled", 93, "Rain"));
        
        // Initialize last status change time for each sensor
        for (Sensor sensor : sensors) {
            lastStatusChangeTime.put(sensor.name, System.currentTimeMillis());
        }
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
        lastStatusChangeTime.put(sensor.name, System.currentTimeMillis());
        notifyListeners(sensor, null);
    }

    public void removeSensor(Sensor sensor) {
        sensors.remove(sensor);
        lastStatusChangeTime.remove(sensor.name);
    }

    public void addListener(OnSensorUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(OnSensorUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(Sensor sensor, String oldStatus) {
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
                long currentTime = System.currentTimeMillis();
                
                // Get list of sensors eligible for status change
                List<Sensor> eligibleSensors = sensors.stream()
                    .filter(sensor -> currentTime - lastStatusChangeTime.get(sensor.name) >= MIN_STATUS_DURATION)
                    .collect(Collectors.toList());
                
                // If there are eligible sensors, change one randomly
                if (!eligibleSensors.isEmpty()) {
                    Sensor sensor = eligibleSensors.get(random.nextInt(eligibleSensors.size()));
                    String oldStatus = sensor.status;
                    
                    // Generate new status (excluding Fire)
                    String[] statuses = {"Normal", "Wind", "Rain"};
                    String newStatus;
                    do {
                        newStatus = statuses[random.nextInt(statuses.length)];
                    } while (newStatus.equals(oldStatus));

                    // Update sensor status
                    sensor.status = newStatus;
                    lastStatusChangeTime.put(sensor.name, currentTime);
                    notifyListeners(sensor, oldStatus);
                }
            }
            scheduleNextUpdate();
        }, UPDATE_INTERVAL);
    }
} 