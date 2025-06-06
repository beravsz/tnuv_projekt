package si.uni_lj.fe.tnuv.ignisguard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.view.Menu;
import android.view.MenuItem;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.os.PowerManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import si.uni_lj.fe.tnuv.ignisguard.databinding.ActivityMainBinding;
import si.uni_lj.fe.tnuv.ignisguard.service.SensorMonitoringService;
import si.uni_lj.fe.tnuv.ignisguard.ui.common.SensorManager;
import si.uni_lj.fe.tnuv.ignisguard.ui.common.FireAlertDialog;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences prefs;
    private static final String PREF_BACKGROUND_SERVICE = "background_service_enabled";
    private static final int PERMISSION_REQUEST_CODE = 123;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SensorManager.setGlobalContext(getApplicationContext());
        SensorManager.setCurrentActivity(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Initialize permission request launcher
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    startSensorMonitoringService();
                }
            }
        );

        // Set up navigation
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
            R.id.navigation_map, R.id.navigation_sensor_status, R.id.navigation_event_timeline
        ).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Request permissions and start service
        requestPermissionsAndStartService();

        // Handle notification click
        handleNotificationClick();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SensorManager.setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SensorManager.setCurrentActivity(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            showSettingsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_settings, null);
        
        SwitchMaterial switchBackgroundService = dialogView.findViewById(R.id.switch_background_service);
        switchBackgroundService.setChecked(prefs.getBoolean(PREF_BACKGROUND_SERVICE, false));
        
        switchBackgroundService.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(PREF_BACKGROUND_SERVICE, isChecked).apply();
            if (isChecked) {
                startSensorMonitoringService();
            } else {
                stopSensorMonitoringService();
            }
        });

        builder.setView(dialogView)
               .setTitle("Settings")
               .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void requestPermissionsAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                startSensorMonitoringService();
            }
        } else {
            startSensorMonitoringService();
        }

        // Request battery optimization exemption
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            if (!getSystemService(PowerManager.class).isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }

    private void startSensorMonitoringService() {
        Intent serviceIntent = new Intent(this, SensorMonitoringService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void stopSensorMonitoringService() {
        Intent serviceIntent = new Intent(this, SensorMonitoringService.class);
        stopService(serviceIntent);
    }

    private void handleNotificationClick() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("show_fire_alert", false)) {
            String sensorName = intent.getStringExtra("sensor_name");
            if (sensorName != null) {
                FireAlertDialog.newInstance(sensorName)
                    .show(getSupportFragmentManager(), "fire_alert");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}