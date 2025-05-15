package si.uni_lj.fe.tnuv.ignisguard.ui.home;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        // Example: populate sensors (should be shared with DashboardFragment)
        sensors.clear();
        sensors.add(new Sensor("Tivoli", 88, "Normal"));
        sensors.add(new Sensor("Rožnik Sever", 71, "Wind"));
        sensors.add(new Sensor("Rožnik Jug", 69, "Normal"));
        sensors.add(new Sensor("Bled", 93, "Rain"));
        // Setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        // Add pins for each sensor
        for (Sensor sensor : sensors) {
            LatLng pos = defaultLocations.getOrDefault(sensor.name, new LatLng(46.05, 14.5));
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(pos)
                    .title(sensor.name)
                    .icon(getMarkerIcon(sensor.status));
            Marker marker = mMap.addMarker(markerOptions);
            marker.setTag(sensor);
        }
        // Move camera to first sensor
        if (!sensors.isEmpty()) {
            LatLng first = defaultLocations.getOrDefault(sensors.get(0).name, new LatLng(46.05, 14.5));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(first, 10f));
        }
    }

    private com.google.android.gms.maps.model.BitmapDescriptor getMarkerIcon(String status) {
        switch (status) {
            case "Fire":
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE); // Replace with fire icon if available
            case "Rain":
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN); // Replace with rain icon if available
            case "Wind":
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE); // Replace with wind icon if available
            default:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        }
    }

    // Example Sensor class (should be shared)
    static class Sensor {
        String name;
        int battery;
        String status;
        // Add LatLng for real implementation
        Sensor(String name, int battery, String status) {
            this.name = name;
            this.battery = battery;
            this.status = status;
        }
    }
} 