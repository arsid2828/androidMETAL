package com.example.prova1.ui;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prova1.R;
import com.example.prova1.models.WindAlert;
import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    private List<WindAlert> alerts;

    public AlertAdapter(List<WindAlert> alerts) {
        this.alerts = alerts;
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
        holder.title.setText(alert.getTitle());
        holder.content.setText(alert.getContent());

        // Imposta il bordo colorato
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setStroke(10, alert.getColor()); // Spessore e colore del bordo
        gradientDrawable.setCornerRadius(16); // Raggio degli angoli
        holder.container.setBackground(gradientDrawable);
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView content;
        LinearLayout container;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.alert_title);
            content = itemView.findViewById(R.id.alert_content);
            container = itemView.findViewById(R.id.alert_container);
        }
    }
}
