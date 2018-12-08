package com.example.admin.apporderfood.Remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetroifitClient {
    private static Retrofit retrofit=null;

    public static Retrofit getClient(String baseURL)
    {
        if (retrofit == null)
        {
            retrofit= new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }


}


