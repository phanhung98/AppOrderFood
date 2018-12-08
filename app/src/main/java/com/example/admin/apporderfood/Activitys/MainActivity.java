package com.example.admin.apporderfood.Activitys;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.apporderfood.Common.Common;
import com.example.admin.apporderfood.Common.NetworkChangeReceiver;
import com.example.admin.apporderfood.Model.User;
import com.example.admin.apporderfood.R;

import com.facebook.FacebookSdk;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 7171;
    Button btnContinue;
    TextView tvSologan;

    FirebaseDatabase database;
    DatabaseReference table_user;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath).build());


        FacebookSdk.sdkInitialize(getApplicationContext());
        AccountKit.initialize(this);

        database= FirebaseDatabase.getInstance();
        table_user= database.getReference("User");

        btnContinue=(Button)findViewById(R.id.btncontinue);

        tvSologan=(TextView)findViewById(R.id.tv_discription);




        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startLoginSystem();
            }
        });



            if (AccountKit.getCurrentAccessToken() != null)
            {
                final AlertDialog waitingDialog= new
                        SpotsDialog.Builder().setContext(this).build();
                waitingDialog.show();
                waitingDialog.setMessage("Please");
                waitingDialog.setCancelable(false);

                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        table_user.child(account.getPhoneNumber().toString())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        User localuser= dataSnapshot.getValue(User.class);

                                        Intent home = new Intent(MainActivity.this, Home.class);
                                        Common.currentUser = localuser;
                                        startActivity(home);
                                        waitingDialog.dismiss();
                                        finish();

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                    }
                });

        }





    }

    public static void logout(){
        AccountKit.logOut();
    }

    private void startLoginSystem() {

        final Intent intent = new Intent(MainActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN); // or .ResponseType.TOKEN
        // ... perform additional configuration ...
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        startActivityForResult(intent, REQUEST_CODE);

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE)
        {
            AccountKitLoginResult result= data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (result.getError() != null){
                Toast.makeText(this, ""+result.getError().getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
            }else if (result.wasCancelled())
            {
                Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
                return;
            }
            else
            {
                if (result.getAccessToken() != null)
                {
                    final AlertDialog waitingDialog= new
                            SpotsDialog.Builder().setContext(this).build();
                    waitingDialog.setMessage("Please");
                    waitingDialog.setCancelable(false);

                    waitingDialog.show();

                    //get current phone
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            final String userPhone= account.getPhoneNumber().toString();

                            table_user.orderByKey().equalTo(userPhone)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.child(userPhone).exists())
                                            {
                                                User newuser = new User();
                                                newuser.setPhone(userPhone);
                                                newuser.setName("");
//                                                newuser.setBalance(0.0);

                                                table_user.child(userPhone).setValue(newuser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                            Toast.makeText(MainActivity.this, "User register successful", Toast.LENGTH_SHORT).show();

                                                        //Login
                                                        table_user.child(userPhone)
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        User localuser= dataSnapshot.getValue(User.class);

                                                                        Intent home = new Intent(MainActivity.this, Home.class);
                                                                        Common.currentUser = localuser;
                                                                        startActivity(home);
                                                                        waitingDialog.dismiss();
                                                                        finish();

                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                    }
                                                                });

                                                    }
                                                });

                                            }else //If exit
                                                {
                                                    table_user.child(userPhone)
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    User localuser= dataSnapshot.getValue(User.class);

                                                                    Intent home = new Intent(MainActivity.this, Home.class);
                                                                    Common.currentUser = localuser;
                                                                    startActivity(home);
                                                                    waitingDialog.dismiss();
                                                                    finish();

                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Toast.makeText(MainActivity.this, ""+accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


                }
            }
        }
    }
}
