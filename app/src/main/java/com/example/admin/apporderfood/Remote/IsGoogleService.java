package com.example.admin.apporderfood.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface IsGoogleService {
    @GET
    Call<String> getAddressName(@Url String url);

}
