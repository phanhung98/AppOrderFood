package com.example.admin.apporderfood.Activitys;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.admin.apporderfood.Common.Common;
import com.example.admin.apporderfood.Common.NetworkChangeReceiver;
import com.example.admin.apporderfood.Model.User;
import com.example.admin.apporderfood.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {

    EditText edtPhone, edtPass;
    Button btnSignIn;
    CheckBox ckbRemember;

    FirebaseDatabase database;
    DatabaseReference table_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPass=(EditText)findViewById(R.id.edtPass);
        edtPhone=(EditText)findViewById(R.id.edtPhone);
        btnSignIn=(Button) findViewById(R.id.btnSignIn);
        ckbRemember=(CheckBox)findViewById(R.id.ckbRemember);

        Paper.init(this);


        database= FirebaseDatabase.getInstance();
       table_user= database.getReference("User");

        registerReceiver();



            btnSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (NetworkChangeReceiver.isOnline(getBaseContext())) {

                        if (ckbRemember.isChecked())
                        {
                            Paper.book().write(Common.USer,edtPhone.getText().toString());
                            Paper.book().write(Common.PASSWORD,edtPass.getText().toString());
                        }

                        final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                        mDialog.setMessage("Please Watting...");
                        mDialog.show();

                        table_user.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {

                                    mDialog.dismiss();
                                    User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                                    user.setPhone(edtPhone.getText().toString());
                                    if (user.getPassword().equals(edtPass.getText().toString())) {
                                        Intent home = new Intent(SignIn.this, Home.class);
                                        Common.currentUser = user;
                                        startActivity(home);
                                        finish();
                                    } else {
                                        Toast.makeText(SignIn.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    mDialog.dismiss();
                                    Toast.makeText(SignIn.this, "User not Exists", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }else {
                        Toast.makeText(SignIn.this, "Please check your internet", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    }

    private void registerReceiver()
    {
        try
        {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(NetworkChangeReceiver.NETWORK_CHANGE_ACTION);
            registerReceiver(internalNetworkChangeReceiver, intentFilter);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDestroy()
    {
        try
        {
            // Make sure to unregister internal receiver in onDestroy().
            unregisterReceiver(internalNetworkChangeReceiver);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * This is internal BroadcastReceiver which get status from external receiver(NetworkChangeReceiver)
     * */
    InternalNetworkChangeReceiver internalNetworkChangeReceiver = new InternalNetworkChangeReceiver();
    class InternalNetworkChangeReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {


              String status= intent.getStringExtra("status");

        }
    }

}
