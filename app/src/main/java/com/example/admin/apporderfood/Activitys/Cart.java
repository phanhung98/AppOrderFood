package com.example.admin.apporderfood.Activitys;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.apporderfood.Common.Common;
import com.example.admin.apporderfood.Common.Config;
import com.example.admin.apporderfood.Databases.Database;
import com.example.admin.apporderfood.Model.MyRespone;
import com.example.admin.apporderfood.Model.Notification;
import com.example.admin.apporderfood.Model.Order;
import com.example.admin.apporderfood.Model.Request;
import com.example.admin.apporderfood.Model.Sender;
import com.example.admin.apporderfood.Model.Token;
import com.example.admin.apporderfood.Model.User;
import com.example.admin.apporderfood.R;
import com.example.admin.apporderfood.Remote.APIServer;
import com.example.admin.apporderfood.Remote.IsGoogleService;
import com.example.admin.apporderfood.ViewHolder.CartAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

@SuppressWarnings("ALL")
public class Cart extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private static final int PAYPAL_REQUEST_CODE =9999 ;
    private static final int LOCATION_PERMISSION_REQUEST = 9999;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9997 ;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    public TextView tvTotalPrice;
    Button btnPlace;

    List<Order> cart= new ArrayList<>();

    CartAdapter adapter;

    APIServer mServer;
    String state;

    IsGoogleService mGoogleService;

    Place shipaddress;
   private Location mLastloction;

    //Paypal
    static PayPalConfiguration config= new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);
    String address, comment;


    private LocationRequest mLocationRequest;
//    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    private GoogleApiClient mGoogleApiClient;


    private static int UPDATE_INTERVAL=5000;
    private static int FASTEST_INTERVAL=3000;
    private static int DISPLACEMENT=10;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);


        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
        .setDefaultFontPath("fonts/restaurant_font.otf")
        .setFontAttrId(R.attr.fontPath).build());

        mGoogleService= Common.getGoogleMapApi();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                )
        {
            ActivityCompat.requestPermissions(this, new String[]
                    {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, LOCATION_PERMISSION_REQUEST);
        }else {
            if (checkPlayServices())
            {
                buildGoogleApiClient();
                createLocationRequest();
//                buildLocationCallBack();

//                fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);

            }
        }

        //Init Paypal
        Intent intent= new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        mServer= Common.getFCMService();

        Anhxa();

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cart.size() > 0) {
                    ShowAlertDialof();

                }else {
                    Toast.makeText(Cart.this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        LoadListFood();

    }

    private synchronized void buildGoogleApiClient() {

        mGoogleApiClient= new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();

    }



    private boolean checkPlayServices() {

        int resultcode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultcode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultcode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultcode,this, PLAY_SERVICES_RESOLUTION_REQUEST).show();

            }else {
                Toast.makeText(this, "This device is not support", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;


    }

    private void createLocationRequest() {

        mLocationRequest= LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void ShowAlertDialof() {

        AlertDialog.Builder alertDialog= new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more step");
        alertDialog.setMessage("Enter your address");

        LayoutInflater inflater= this.getLayoutInflater();
        View order_address_comment= inflater.inflate(R.layout.order_address_commet,null);

//        final MaterialEditText edtAddress= (MaterialEditText)order_address_comment.findViewById(R.id.edtAddress);
        final PlaceAutocompleteFragment edtAddress= (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);

        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Enter your address");

        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(14);

        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shipaddress= place;

            }

            @Override
            public void onError(Status status) {
                Log.e("ERROR",status.getStatusMessage());
            }
        });

        final MaterialEditText edtComment= (MaterialEditText)order_address_comment.findViewById(R.id.edtComment);


        final RadioButton rbShiptothisAddress= (RadioButton)order_address_comment.findViewById(R.id.rb_thisaddress);
        final RadioButton rbHome= (RadioButton)order_address_comment.findViewById(R.id.rb_home);
        final RadioButton rbCOD= (RadioButton)order_address_comment.findViewById(R.id.rb_Cod);
        final RadioButton rbpaypal= (RadioButton)order_address_comment.findViewById(R.id.rb_payment);
//        final RadioButton rbbalance= (RadioButton)order_address_comment.findViewById(R.id.rb_balance);

        rbShiptothisAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    mGoogleService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=AIzaSyCzfUALdU8T49vZlfy5qAvXrqTI7k6XBhY",
                            mLastloction.getLatitude(),
                            mLastloction.getLongitude()))
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    try{
                                        JSONObject jsonObject= new JSONObject(response.body().toString());
                                        JSONArray resultArray= jsonObject.getJSONArray("results");

                                        JSONObject firstObject= resultArray.getJSONObject(0);

                                        address= firstObject.getString("formatted_address");


                                        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setText(address);


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(Cart.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        rbHome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    if (Common.currentUser.getHomeAddress() != null || !TextUtils.isEmpty(Common.currentUser.getHomeAddress()))
                    {
                        address= Common.currentUser.getHomeAddress();
                        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setText(address);
                    }else {
                        Toast.makeText(Cart.this, "Please update your Home Address", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });



        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (!rbShiptothisAddress.isChecked() && !rbHome.isChecked())
                {
                    if (shipaddress != null)
                        address=shipaddress.getAddress().toString();
                    else {
                        Toast.makeText(Cart.this, "Please enter address or select option address", Toast.LENGTH_SHORT).show();

                        //remove fragment
                        getFragmentManager().beginTransaction()
                                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();
                        return;

                    }
                }
                if (TextUtils.isEmpty(address)){
                    Toast.makeText(Cart.this, "Please enter address or select option address", Toast.LENGTH_SHORT).show();

                    //remove fragment
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;

                }

        //Show paypal to payment
                //first het address, comment from dialog
//                address=shipaddress.getAddress().toString();
                comment=edtComment.getText().toString();


                if (!rbCOD.isChecked() && !rbpaypal.isChecked())
                    {
                    Toast.makeText(Cart.this, "Please select payment option", Toast.LENGTH_SHORT).show();

                    //remove fragment
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;
                }else if (rbpaypal.isChecked()) {

                    String fomatamount = tvTotalPrice.getText().toString()
                            .replace("$", "")
                            .replace(",", "");

                    PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(fomatamount),
                            "USD",
                            "App Order food",
                            PayPalPayment.PAYMENT_INTENT_SALE);
                    Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                    intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                    intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
                    startActivityForResult(intent, PAYPAL_REQUEST_CODE);
                }else if (rbCOD.isChecked())
                {

                    Request request= new Request(
                            Common.currentUser.getPhone(),
                            Common.currentUser.getName(),
                            address,
                            tvTotalPrice.getText().toString(),
                            "0",
                            comment,
                            "COD",
                            "Unpaid",
                            String.format("%s,%s", mLastloction.getLatitude(), mLastloction.getLongitude()),
                            cart
                    );

                    String order_number= String.valueOf(System.currentTimeMillis());

                    requests.child(order_number)
                            .setValue(request);
                    //Delete cart
                    new Database(getBaseContext()).CleanCart();

                    sendNoticationOrder(order_number);


                }
//                else if (rbbalance.isChecked())
//                {
//                    double amount= 0;
//
//                    try {
//                        amount= Common.formatCurrency(tvTotalPrice.getText().toString(),Locale.US).doubleValue();
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                    if (Common.currentUser.getBalance() >= amount)
//                    {
//
//                        Request request= new Request(
//                                Common.currentUser.getPhone(),
//                                Common.currentUser.getName(),
//                                address,
//                                tvTotalPrice.getText().toString(),
//                                "0",
//                                comment,
//                                "GrapFood Balance",
//                                "Paid",
//                                String.format("%s,%s", mLastloction.getLatitude(), mLastloction.getLongitude()),
//                                cart
//                        );
//
//                        final String order_number= String.valueOf(System.currentTimeMillis());
//
//                        requests.child(order_number)
//                                .setValue(request);
//                        //Delete cart
//                        new Database(getBaseContext()).CleanCart();
//
//                       //update balance
//                        double balance= Common.currentUser.getBalance() - amount;
//                        Map<String,Object> update_balance=  new HashMap<>();
//                        update_balance.put("balance",balance);
//
//                        FirebaseDatabase.getInstance().getReference("User")
//                                .child(Common.currentUser.getPhone())
//                                .updateChildren(update_balance)
//                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful())
//                                        {
//
//                                            FirebaseDatabase.getInstance().getReference("User")
//                                                    .child(Common.currentUser.getPhone())
//                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                                                        @Override
//                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                                            Common.currentUser= dataSnapshot.getValue(User.class);
//
//                                                            //Delete cart
//                                                            new Database(getBaseContext()).CleanCart();
//
//                                                            sendNoticationOrder(order_number);
//                                                        }
//
//                                                        @Override
//                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                                        }
//                                                    });
//
//                                        }
//                                    }
//                                });
//
//
//                    }else {
//                        Toast.makeText(Cart.this, "Your balance not enough, please choose other payment", Toast.LENGTH_SHORT).show();
//                    }
//                }





                //remove fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();

            }

        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //remove fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();
            }
        });
        alertDialog.show();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST:
            {
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (checkPlayServices())
                    {
                        buildGoogleApiClient();
                        createLocationRequest();
//                buildLocationCallBack();

//                fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);

                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
      if (requestCode == PAYPAL_REQUEST_CODE)
      {
          if (resultCode == RESULT_OK)
          {
              PaymentConfirmation confirmation= data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
              if (confirmation != null)
              {
                  try {
                      String paymentdetail= confirmation.toJSONObject().toString(4);

                      JSONObject jsonObject= new JSONObject(paymentdetail);


                Request request= new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                       address,
                        tvTotalPrice.getText().toString(),
                        "0",
                       comment,
                        "Paypal",
                        jsonObject.getJSONObject("response").getString("state"),
                        String.format("%s,%s", mLastloction.getLatitude(), mLastloction.getLongitude()),
                        cart
                );

                String order_number= String.valueOf(System.currentTimeMillis());

                requests.child(order_number)
                        .setValue(request);
                //Delete cart
                new Database(getBaseContext()).CleanCart();

                sendNoticationOrder(order_number);


                  } catch (JSONException e) {
                      e.printStackTrace();
                  }
              }
          }else if (resultCode == Activity.RESULT_CANCELED)
              Toast.makeText(this, "Payment cancel", Toast.LENGTH_SHORT).show();
          else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID)
              Toast.makeText(this, "Invalid payment", Toast.LENGTH_SHORT).show();
      }
    }

    private void sendNoticationOrder(final String order_number) {

        DatabaseReference tokens= FirebaseDatabase.getInstance().getReference("Token");
        Query data=  tokens.orderByChild("isServerToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
            {
                Token ServerToken= postSnapshot.getValue(Token.class);

                //create raw payload to send
                Notification notification = new Notification("Phan Hung", "You have new order "+order_number);
                Sender content= new Sender(ServerToken.getToken(), notification);

                mServer.sendnotification(content)
                        .enqueue(new Callback<MyRespone>() {
                            @Override
                            public void onResponse(Call<MyRespone> call, Response<MyRespone> response) {
                                if (response.code() == 200) {
                                    if (response.body().success == 1) {
                                        Toast.makeText(Cart.this, "Thank you, Order Place", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(Cart.this, "Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<MyRespone> call, Throwable t) {
                                Log.e("ERROR",t.getMessage());
                            }
                        });

            }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void LoadListFood() {

        cart= new Database(this).getCarts();
        adapter= new CartAdapter(cart,this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        int total=0;
        for (Order order:cart)
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantily()));
        tvTotalPrice.setText(total+"");

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {

        cart.remove(position);

        new Database(this).CleanCart();

        for (Order item:cart)
            new Database(this).addToCart(item);

        LoadListFood();

    }

    private void Anhxa() {
        database=FirebaseDatabase.getInstance();
        requests=database.getReference("Requests");

        recyclerView=(RecyclerView)findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        tvTotalPrice=(TextView)findViewById(R.id.total);
        btnPlace=(Button)findViewById(R.id.btnPlaceOrder);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
            disPlayLocation();
            startLocationUpdate();
    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                )
        {
            return;
        }
//        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback,Looper.myLooper());
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);

    }

    private void disPlayLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                )
        {
            return;
        }
        mLastloction= LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastloction != null){
            Log.d("LOCATION", "Your location "+mLastloction.getLatitude()+ " , "+mLastloction.getLongitude() );
        }else {
            Log.d("LOCATION", "Could not get your location");
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
            mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastloction=location;
        disPlayLocation();
    }
}
