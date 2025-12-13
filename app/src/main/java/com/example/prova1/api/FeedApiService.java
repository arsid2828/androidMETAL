package com.example.prova1.api;

import com.example.prova1.models.AtomFeed;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface FeedApiService {
    @GET
    Call<AtomFeed> getFeed(@Url String url);
}
