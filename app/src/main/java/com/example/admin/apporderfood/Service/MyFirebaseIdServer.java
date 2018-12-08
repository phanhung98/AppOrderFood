package com.example.admin.apporderfood.Service;

import com.example.admin.apporderfood.Common.Common;
import com.example.admin.apporderfood.Model.Token;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

@SuppressWarnings("ALL")
public class MyFirebaseIdServer extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String tokenRefreshed= FirebaseInstanceId.getInstance().getToken();
        if (Common.currentUser != null)
        updateTokenToFirebase(tokenRefreshed);
    }

    private void updateTokenToFirebase(String tokenRefreshed) {
        FirebaseDatabase db= FirebaseDatabase.getInstance();
        DatabaseReference tokens= db.getReference("Token");
        Token token= new Token(tokenRefreshed,false);
        tokens.child(Common.currentUser.getPhone()).setValue(token);
    }
}
