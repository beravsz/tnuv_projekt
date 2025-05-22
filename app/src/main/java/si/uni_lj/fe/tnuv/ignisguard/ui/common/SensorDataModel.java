package si.uni_lj.fe.tnuv.ignisguard.ui.common;

import org.json.JSONObject;

public class SensorDataModel {
    public String deviceId;
    public double temperature;
    public double humidity;
    public double pressure;
    public double battery;
    public String status;

    public static SensorDataModel parseSensorData(JSONObject response) {
        try {
            SensorDataModel data = new SensorDataModel();
            JSONObject uplinkMessage = response.getJSONObject("uplink_message");
            JSONObject decodedPayload = uplinkMessage.getJSONObject("decoded_payload");
            
            data.deviceId = response.getString("end_device_ids").split("\"")[3];
            data.temperature = decodedPayload.getDouble("temperature");
            data.humidity = decodedPayload.getDouble("humidity");
            data.pressure = decodedPayload.getDouble("pressure");
            data.battery = decodedPayload.getDouble("battery");
            
            // Determine status based on temperature
            if (data.temperature > 30) {
                data.status = "Fire";
            } else if (data.humidity > 80) {
                data.status = "Rain";
            } else if (data.pressure < 1000) {
                data.status = "Wind";
            } else {
                data.status = "Normal";
            }
            
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
} 