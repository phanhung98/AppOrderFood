package com.example.admin.apporderfood.Activitys;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.admin.apporderfood.Model.User;
import com.example.admin.apporderfood.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Sign_up extends AppCompatActivity {

    EditText edtPhoneSU, edtNameSU, edtPasswordSU;
    Button btnSignInSU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtNameSU=(EditText)findViewById(R.id.edtNameSU);
        edtPhoneSU=(EditText)findViewById(R.id.edtPhoneSU);
        edtPasswordSU=(EditText)findViewById(R.id.edtPassSU);
        btnSignInSU=(Button)findViewById(R.id.btnSignInSU);

        final FirebaseDatabase database= FirebaseDatabase.getInstance();
        final DatabaseReference table_user= database.getReference("User");

        btnSignInSU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog mDialog= new ProgressDialog(Sign_up.this);
                mDialog.setMessage("Please Watting...");
                mDialog.show();

                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //check if already user phone
                        if (dataSnapshot.child(edtPhoneSU.getText().toString()).exists()){
                            mDialog.dismiss();
                            Toast.makeText(Sign_up.this, "Phone Number Already Register", Toast.LENGTH_SHORT).show();
                        }else {
                            mDialog.dismiss();
                            User user= new User(edtNameSU.getText().toString(), edtPasswordSU.getText().toString());
                             table_user.child(edtPhoneSU.getText().toString()).setValue(user);
                            Toast.makeText(Sign_up.this, "Sign up successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });
    }
}
