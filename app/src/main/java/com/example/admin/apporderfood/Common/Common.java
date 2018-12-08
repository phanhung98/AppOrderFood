package com.example.admin.apporderfood.Common;

import com.example.admin.apporderfood.Model.Request;
import com.example.admin.apporderfood.Model.User;
import com.example.admin.apporderfood.Remote.APIServer;
import com.example.admin.apporderfood.Remote.IsGoogleService;
import com.example.admin.apporderfood.Remote.RetroifitClient;
import com.example.admin.apporderfood.Remote.RetroifitGoogleClient;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class Common {
    public static User currentUser;
    public static Request currentRequest;

    public static String topicName="News";


    private static final String BASE_URL="https://fcm.googleapis.com/";
    private static final String GOOGLE_API_URL="https://maps.googleapis.com/";

    public static APIServer getFCMService()
    {
        return RetroifitClient.getClient(BASE_URL).create(APIServer.class);
    }

    public static IsGoogleService getGoogleMapApi ()
    {
        return RetroifitGoogleClient.getGoogleClient(GOOGLE_API_URL).create(IsGoogleService.class);
    }

    public static String PHONE_TEXT="userPhone";

    public static final String INTENT_FOOD_ID="FoodIdComment";

    public static final String USer="User";
    public static final String PASSWORD="Password";

    public static final String DELETE="Delete";


    public static String convertCodetoStatus(String status) {

        if (status.equals("0"))
            return "Placed";
        else if (status.equals("1"))
            return "On my way";
        else
            return "Shipped";

    }

    public static BigDecimal formatCurrency(String amount, Locale locale) throws ParseException
    {
        NumberFormat format= NumberFormat.getCurrencyInstance(locale);
        if (format instanceof DecimalFormat)
            ((DecimalFormat)format).setParseBigDecimal(true);
        return (BigDecimal)format.parse(amount.replace("[^\\d.,]", ""));
    }



}
