package com.example.admin.apporderfood.Activitys;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.admin.apporderfood.Common.Common;
import com.example.admin.apporderfood.Interface.ItemClickListener;
import com.example.admin.apporderfood.Model.Request;
import com.example.admin.apporderfood.R;
import com.example.admin.apporderfood.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OrderStatus extends AppCompatActivity {

        public RecyclerView recyclerView;
        public RecyclerView.LayoutManager layoutManager;

        FirebaseRecyclerOptions<Request> options;
        FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

        FirebaseDatabase database;
        DatabaseReference requests;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath).build());

        Anhxa();
        if (Common.currentUser.getPhone()== null)
            LoadOrders(getIntent().getStringExtra("userPhone"));
        else
            LoadOrders(Common.currentUser.getPhone());

    }

    private void LoadOrders(String phone) {

        options= new FirebaseRecyclerOptions.Builder<Request>().setQuery(requests.orderByChild("phone").equalTo(phone),Request.class).build();
        adapter= new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, int position, @NonNull Request model) {

                holder.tvOrderId.setText(adapter.getRef(position).getKey());
                holder.tvOrderStatus.setText(convertCodetoStatus(model.getStatus()));
                holder.tvOrderAddress.setText(model.getAddress());
                holder.tvOrderPhone.setText(model.getPhone());

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Toast.makeText(OrderStatus.this, "Why you clik here?", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_layout,viewGroup,false);
                return new OrderViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }

    private String convertCodetoStatus(String status) {

        if (status.equals("0"))
            return "Placed";
        else if (status.equals("1"))
            return "On my way";
        else
            return "Shipped";

    }

    private void Anhxa() {
        database=FirebaseDatabase.getInstance();
        requests= database.getReference("Requests");

        recyclerView=(RecyclerView)findViewById(R.id.listOrder);
        recyclerView.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
