package si.uni_lj.fe.tnuv.ignisguard.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import si.uni_lj.fe.tnuv.ignisguard.R;
import java.util.List;

public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.SensorViewHolder> {
    private final List<Sensor> sensors;
    public interface OnDeleteClickListener {
        void onDelete(int position);
    }
    private OnDeleteClickListener deleteClickListener;
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }
    public SensorAdapter(List<Sensor> sensors) {
        this.sensors = sensors;
    }
    @NonNull
    @Override
    public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sensor, parent, false);
        return new SensorViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {
        Sensor sensor = sensors.get(position);
        holder.name.setText(sensor.name);
        holder.battery.setText(holder.battery.getContext().getString(R.string.sensor_battery, sensor.battery));
        holder.status.setText(holder.status.getContext().getString(R.string.sensor_status, sensor.status));
        if (holder.status.getContext().getString(R.string.status_fire).equals(sensor.status)) {
            holder.status.setTextColor(holder.status.getContext().getResources().getColor(R.color.orange_500));
        } else {
            holder.status.setTextColor(holder.status.getContext().getResources().getColor(R.color.black));
        }
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteClickListener != null) deleteClickListener.onDelete(holder.getAdapterPosition());
        });
    }
    @Override
    public int getItemCount() {
        return sensors.size();
    }
    public static class SensorViewHolder extends RecyclerView.ViewHolder {
        TextView name, battery, status;
        ImageButton deleteButton;
        public SensorViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_sensor_name);
            battery = itemView.findViewById(R.id.text_sensor_battery);
            status = itemView.findViewById(R.id.text_sensor_status);
            deleteButton = itemView.findViewById(R.id.button_delete_sensor);
        }
    }
} 