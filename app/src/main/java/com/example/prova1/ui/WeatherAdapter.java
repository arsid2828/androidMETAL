package com.example.prova1.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prova1.R;
import com.example.prova1.models.WeatherItem;

import java.util.ArrayList;
import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {

    private List<WeatherItem> weatherList;

    public WeatherAdapter(List<WeatherItem> weatherList) {
        this.weatherList = weatherList != null ? weatherList : new ArrayList<>();
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_news, parent, false); // Reusing the layout for now
        return new WeatherViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        WeatherItem currentItem = weatherList.get(position);
        holder.title.setText(currentItem.getTitle());
        holder.description.setText(currentItem.getDescription());
        holder.date.setText(currentItem.getDate());
    }

    @Override
    public int getItemCount() {
        return weatherList.size();
    }

    public void updateData(List<WeatherItem> newWeatherList) {
        this.weatherList.clear();
        this.weatherList.addAll(newWeatherList);
        notifyDataSetChanged();
    }

    static class WeatherViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView description;
        final TextView date;

        WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.news_title);
            description = itemView.findViewById(R.id.news_description);
            date = itemView.findViewById(R.id.news_date);
        }
    }
}
