package com.metal.idrogeo.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prova1.R;
import com.metal.idrogeo.api.NewsItem;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<NewsItem> newsList;

    public NewsAdapter(List<NewsItem> newsList) {
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_news, parent, false);
        return new NewsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem currentItem = newsList.get(position);
        holder.title.setText(currentItem.getTitle());
        holder.description.setText(currentItem.getDescription());
        holder.date.setText(currentItem.getPublicationDate());
    }

    @Override
    public int getItemCount() {
        return newsList != null ? newsList.size() : 0;
    }

    /**
     * Metodo FONDAMENTALE per aggiornare i dati nell'adapter senza rompere la UI.
     */
    public void updateData(List<NewsItem> newNewsList) {
        this.newsList.clear();
        this.newsList.addAll(newNewsList);
        notifyDataSetChanged(); // Notifica alla RecyclerView di aggiornarsi
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView description;
        final TextView date;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.news_title);
            description = itemView.findViewById(R.id.news_description);
            date = itemView.findViewById(R.id.news_date);
        }
    }
}
