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

public class WeatherAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Define constants for the view types
    private static final int VIEW_TYPE_WEATHER = 0;
    private static final int VIEW_TYPE_MAP = 1;

    private List<WeatherItem> weatherList;

    public WeatherAdapter(List<WeatherItem> weatherList) {
        this.weatherList = weatherList != null ? weatherList : new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        // Use the title to distinguish the map item
        if ("(aggiungere mappa)".equals(weatherList.get(position).getDescription())) {
            return VIEW_TYPE_MAP;
        } else {
            return VIEW_TYPE_WEATHER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_MAP) {
            View mapView = inflater.inflate(R.layout.list_item_map, parent, false);
            return new MapViewHolder(mapView);
        } else {
            View weatherView = inflater.inflate(R.layout.list_item_weather, parent, false);
            return new WeatherViewHolder(weatherView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_WEATHER) {
            WeatherViewHolder weatherHolder = (WeatherViewHolder) holder;
            WeatherItem currentItem = weatherList.get(position);
            weatherHolder.title.setText(currentItem.getTitle());
            weatherHolder.description.setText(currentItem.getDescription());
            weatherHolder.date.setText(currentItem.getDate());
        }
        // No data needs to be bound for the MapViewHolder
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

    // ViewHolder for your regular weather items
    static class WeatherViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView description;
        final TextView date;

        WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.weather_title);
            description = itemView.findViewById(R.id.weather_description);
            date = itemView.findViewById(R.id.weather_date);
        }
    }

    // ViewHolder for the map placeholder
    static class MapViewHolder extends RecyclerView.ViewHolder {
        MapViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
