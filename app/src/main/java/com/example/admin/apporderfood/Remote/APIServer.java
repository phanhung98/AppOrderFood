package com.example.admin.apporderfood.Remote;

import com.example.admin.apporderfood.Model.MyRespone;
import com.example.admin.apporderfood.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIServer {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAw5anXVY:APA91bHGwzy2BuFbB-79dQU6YMfiLz7APNO28MyHsIz40EG4o-1rdKo464cynDfkSI4HM01EOhdNC5-nDQp_DxAvRsvGxbR9-NslqFXz73tupXMMA4J9Lh9oSRc2S5RuirUjSLCA1TVz"
            }
    )
    @POST("fcm/send")
    Call<MyRespone> sendnotification(@Body Sender body);

}
