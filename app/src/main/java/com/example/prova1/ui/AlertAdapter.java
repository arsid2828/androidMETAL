package com.example.prova1.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prova1.R;
import com.example.prova1.models.WindAlert;
import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    private List<WindAlert> alerts;
    private final OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(WindAlert alert);
    }

    public AlertAdapter(List<WindAlert> alerts, OnDeleteClickListener deleteClickListener) {
        this.alerts = alerts;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        WindAlert alert = alerts.get(position);
        holder.bind(alert, deleteClickListener);
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    public void submitList(List<WindAlert> newAlerts) {
        this.alerts = newAlerts;
        notifyDataSetChanged();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView title, content, datetime;
        ImageView deleteButton;
        View colorBar;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.alert_title);
            content = itemView.findViewById(R.id.alert_content);
            datetime = itemView.findViewById(R.id.alert_datetime);
            deleteButton = itemView.findViewById(R.id.delete_button);
            colorBar = itemView.findViewById(R.id.alert_color_bar);
        }

        public void bind(final WindAlert alert, final OnDeleteClickListener clickListener) {
            title.setText(alert.getTitle());
            content.setText(alert.getContent());
            String dateTimeString = alert.getFormattedDate() + " - " + alert.getFormattedTime();
            datetime.setText(dateTimeString);

            // Imposta il colore della barra laterale
            colorBar.setBackgroundColor(alert.getColor());

            deleteButton.setOnClickListener(v -> clickListener.onDeleteClick(alert));
        }
    }
}
