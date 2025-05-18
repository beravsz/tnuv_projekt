package si.uni_lj.fe.tnuv.ignisguard.ui.common;

public class Sensor {
    public String name;
    public int battery;
    public String status;
    public double latitude;
    public double longitude;

    public Sensor(String name, int battery, String status) {
        this.name = name;
        this.battery = battery;
        this.status = status;
        this.latitude = 0;
        this.longitude = 0;
    }

    public Sensor(String name, int battery, String status, double latitude, double longitude) {
        this.name = name;
        this.battery = battery;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }
} 