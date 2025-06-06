package si.uni_lj.fe.tnuv.ignisguard.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import si.uni_lj.fe.tnuv.ignisguard.MainActivity;
import si.uni_lj.fe.tnuv.ignisguard.R;
import si.uni_lj.fe.tnuv.ignisguard.ui.common.Sensor;
import si.uni_lj.fe.tnuv.ignisguard.ui.common.SensorManager;
import si.uni_lj.fe.tnuv.ignisguard.ui.common.FireAlertDialog;
import android.os.PowerManager;
import android.provider.Settings;

public class SensorMonitoringService extends Service implements SensorManager.OnSensorUpdateListener {
    private static final String CHANNEL_ID = "SensorMonitoringChannel";
    private static final int NOTIFICATION_ID = 1;
    private SensorManager sensorManager;
    private static final String TAG = "SensorMonitoringService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate started");
        checkFireIconResource();
        sensorManager = SensorManager.getInstance();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification("Monitoring sensors..."));
        sensorManager.addListener(this);
        sensorManager.startUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service onStartCommand");
        // If the service gets killed, restart it
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy");
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.removeListener(this);
        }
        // Restart the service if it gets destroyed
        Intent broadcastIntent = new Intent("si.uni_lj.fe.tnuv.ignisguard.RestartService");
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "Service onTaskRemoved");
        super.onTaskRemoved(rootIntent);
        // Restart the service if the app is removed from recent apps
        Intent broadcastIntent = new Intent("si.uni_lj.fe.tnuv.ignisguard.RestartService");
        sendBroadcast(broadcastIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorUpdated(Sensor sensor, String oldStatus) {
        Log.d(TAG, "Sensor updated: " + sensor.name + " - " + sensor.status);
        if ("Fire".equals(sensor.status)) {
            showFireNotification(sensor);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Sensor Monitoring",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for sensor status changes");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String contentText) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("IgnisGuard")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_fire)
            .setContentIntent(pendingIntent)
            .setOngoing(true)  // Make the notification persistent
            .build();
    }

    private void showFireNotification(Sensor sensor) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra("show_fire_alert", true);
        notificationIntent.putExtra("sensor_name", sensor.name);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            sensor.name.hashCode(),
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Fire Alert!")
            .setContentText("Sensor " + sensor.name + " has detected a fire!")
            .setSmallIcon(R.drawable.ic_fire)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(sensor.name.hashCode(), notification);
    }

    private void checkFireIconResource() {
        int fireIconId = getResources().getIdentifier("ic_fire", "drawable", getPackageName());
        if (fireIconId == 0) {
            Log.e(TAG, "ic_fire drawable resource is missing!");
        } else {
            Log.d(TAG, "ic_fire drawable resource found");
        }
    }

    // Uncomment and call from an Activity context if you want to prompt the user
    // private void checkAndPromptBatteryOptimization() {
    //     PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    //     String packageName = getPackageName();
    //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    //         if (!pm.isIgnoringBatteryOptimizations(packageName)) {
    //             Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
    //             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //             startActivity(intent);
    //         }
    //     }
    // }
} 