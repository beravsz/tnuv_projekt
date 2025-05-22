package si.uni_lj.fe.tnuv.ignisguard.ui.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import si.uni_lj.fe.tnuv.ignisguard.R;
import si.uni_lj.fe.tnuv.ignisguard.ui.common.Sensor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import android.preference.PreferenceManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.model.BitmapDescriptor;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    // Example sensor data with locations (to be replaced with shared data)
    private final List<Sensor> sensors = new ArrayList<>();
    // For demo: static locations for initial sensors
    private final Map<String, LatLng> defaultLocations = new HashMap<String, LatLng>() {{
        put("Tivoli", new LatLng(46.0569, 14.5058));
        put("Rožnik Sever", new LatLng(46.065, 14.485));
        put("Rožnik Jug", new LatLng(46.052, 14.485));
        put("Bled", new LatLng(46.3692, 14.1136));
    }};
    private SharedPreferences prefs;
    private Gson gson = new Gson();
    private static final String SENSORS_KEY = "sensors_list";
    private final List<Marker> sensorMarkers = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        sensors.clear();
        sensors.addAll(loadSensors());
        // Setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return root;
    }

    private List<Sensor> loadSensors() {
        String json = prefs.getString(SENSORS_KEY, null);
        if (json != null) {
            Type type = new TypeToken<List<Sensor>>(){}.getType();
            return gson.fromJson(json, type);
        } else {
            List<Sensor> defaultSensors = new ArrayList<>();
            defaultSensors.add(new Sensor("Tivoli", 88, "Normal"));
            defaultSensors.add(new Sensor("Rožnik Sever", 71, "Wind"));
            defaultSensors.add(new Sensor("Rožnik Jug", 69, "Normal"));
            defaultSensors.add(new Sensor("Bled", 93, "Rain"));
            return defaultSensors;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        // Clear old markers
        for (Marker marker : sensorMarkers) {
            marker.remove();
        }
        sensorMarkers.clear();
        // Add pins for each sensor
        for (Sensor sensor : sensors) {
            LatLng pos;
            if (sensor.latitude != 0 || sensor.longitude != 0) {
                pos = new LatLng(sensor.latitude, sensor.longitude);
            } else {
                pos = defaultLocations.getOrDefault(sensor.name, new LatLng(46.05, 14.5));
            }
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(pos)
                    .title(sensor.name)
                    .icon(getMarkerIcon(sensor.status));
            Marker marker = mMap.addMarker(markerOptions);
            marker.setTag(sensor);
            sensorMarkers.add(marker);
        }
        // Move camera to first sensor
        if (!sensors.isEmpty()) {
            LatLng first;
            if (sensors.get(0).latitude != 0 || sensors.get(0).longitude != 0) {
                first = new LatLng(sensors.get(0).latitude, sensors.get(0).longitude);
            } else {
                first = defaultLocations.getOrDefault(sensors.get(0).name, new LatLng(46.05, 14.5));
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(first, 10f));
        }

        // Setup marker click and map click listeners for sensor details card
        View root = getView();
        if (root == null) return;
        final View detailsCard = root.findViewById(R.id.sensor_details_card);
        final android.widget.TextView nameView = root.findViewById(R.id.text_sensor_name);
        final android.widget.TextView batteryView = root.findViewById(R.id.text_sensor_battery);
        final android.widget.TextView statusView = root.findViewById(R.id.text_sensor_status);

        mMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof Sensor) {
                Sensor sensor = (Sensor) tag;
                nameView.setText(sensor.name);
                batteryView.setText("Battery: " + sensor.battery + "%");
                statusView.setText("Status: " + sensor.status);
                detailsCard.setVisibility(View.VISIBLE);
                marker.showInfoWindow();
            }
            return true;
        });

        mMap.setOnMapClickListener(latLng -> {
            detailsCard.setVisibility(View.GONE);
            for (Marker marker : sensorMarkers) {
                marker.hideInfoWindow();
            }
        });
    }

    private BitmapDescriptor getMarkerIcon(String status) {
        switch (status) {
            case "Fire":
                return getBitmapDescriptor(R.drawable.ic_fire);
            case "Normal":
                return getBitmapDescriptor(R.drawable.ic_location_pin_normal);
            case "Rain":
                return getBitmapDescriptor(R.drawable.ic_rain);
            case "Wind":
                return getBitmapDescriptor(R.drawable.ic_wind_flag);
            default:
                return getBitmapDescriptor(R.drawable.ic_location_pin_normal);
        }
    }

    private BitmapDescriptor getBitmapDescriptor(int id) {
        Drawable vectorDrawable = ContextCompat.getDrawable(requireContext(), id);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
} 