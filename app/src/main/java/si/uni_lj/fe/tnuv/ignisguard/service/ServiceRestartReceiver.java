package si.uni_lj.fe.tnuv.ignisguard.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ServiceRestartReceiver extends BroadcastReceiver {
    private static final String TAG = "ServiceRestartReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Service Stops! Restarting service...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, SensorMonitoringService.class));
        } else {
            context.startService(new Intent(context, SensorMonitoringService.class));
        }
    }
} 