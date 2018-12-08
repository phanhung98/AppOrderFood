package com.example.admin.apporderfood.Activitys;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.admin.apporderfood.Common.Common;
import com.example.admin.apporderfood.Common.NetworkChangeReceiver;
import com.example.admin.apporderfood.Databases.Database;
import com.example.admin.apporderfood.Interface.ItemClickListener;
import com.example.admin.apporderfood.Model.Food;
import com.example.admin.apporderfood.Model.Order;
import com.example.admin.apporderfood.R;
import com.example.admin.apporderfood.ViewHolder.FoodViewHolder;
import com.example.admin.apporderfood.ViewHolder.MenuViewHolder;
import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodlist;

    Database localDb;

    CallbackManager callbackManager;
    ShareDialog shareDialog;

    SwipeRefreshLayout swipeRefreshLayout;

    Target target= new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            SharePhoto photo= new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (ShareDialog.canShow(SharePhotoContent.class))
            {
                SharePhotoContent content= new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    String CategoryId="";

    FirebaseRecyclerOptions<Food> options;
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    //Searchbar
    FirebaseRecyclerOptions<Food> searchOption;
    FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    List<String> suggestList= new ArrayList<>();

    MaterialSearchBar materialSearchBar;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath).build());

        //Init FaceBook
        callbackManager= CallbackManager.Factory.create();
        shareDialog=new ShareDialog(this);

        localDb= new Database(this);

        //FIrebase
        database= FirebaseDatabase.getInstance();
        foodlist= database.getReference("Food");


        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (getIntent() != null){
                    CategoryId= getIntent().getStringExtra("CategoryId");
                }
                if (!CategoryId.isEmpty() && CategoryId != null){
                    if (NetworkChangeReceiver.isOnline(getBaseContext())) {
                        LoadlistFood(CategoryId);
                    }else {
                        Toast.makeText(FoodList.this, "Please check your internet", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (getIntent() != null){
                    CategoryId= getIntent().getStringExtra("CategoryId");
                }
                if (!CategoryId.isEmpty() && CategoryId != null){
                    if (NetworkChangeReceiver.isOnline(getBaseContext())) {
                        LoadlistFood(CategoryId);
                    }else {
                        Toast.makeText(FoodList.this, "Please check your internet", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                materialSearchBar=(MaterialSearchBar)findViewById(R.id.searchBar);
                materialSearchBar.setHint("Enter your food");
                LoadSuggest();
                materialSearchBar.setLastSuggestions(suggestList);
                materialSearchBar.setCardViewElevation(10);
                materialSearchBar.addTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        List<String> suggest= new ArrayList<>();
                        for (String search:suggestList){
                            if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                                suggest.add(search);
                        }
                        materialSearchBar.setLastSuggestions(suggest);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                    @Override
                    public void onSearchStateChanged(boolean enabled) {
                        if (!enabled)
                            recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onSearchConfirmed(CharSequence text) {
                        startSearch(text);
                    }

                    @Override
                    public void onButtonClicked(int buttonCode) {

                    }
                });

            }
        });

        recyclerView=(RecyclerView)findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // get Intent
        if (getIntent() != null){
            CategoryId= getIntent().getStringExtra("CategoryId");
        }
        if (!CategoryId.isEmpty() && CategoryId != null){
            if (NetworkChangeReceiver.isOnline(getBaseContext())) {
                LoadlistFood(CategoryId);
            }else {
                Toast.makeText(FoodList.this, "Please check your internet", Toast.LENGTH_SHORT).show();
                return;
            }
        }


    }

    private void startSearch(CharSequence text) {
        searchOption= new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(foodlist.orderByChild("name").equalTo(text.toString()), Food.class).build();
        searchAdapter= new FirebaseRecyclerAdapter<Food, FoodViewHolder>(searchOption) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {

                holder.food_name.setText(model.getName());
                Picasso.with(FoodList.this).load(model.getImage()).into(holder.food_image);
                final Food local= model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail= new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", searchAdapter.getRef(position).getKey());
                        startActivity(foodDetail);
                    }
                });


            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.food_item,viewGroup,false);
                return new FoodViewHolder(view);
            }
        };
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter);
    }

    private void LoadSuggest() {

        foodlist.orderByChild("menuId").equalTo(CategoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               for (DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                   Food item= postSnapshot.getValue(Food.class);
                   suggestList.add(item.getName());
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void LoadlistFood(String categoryId) {

        options= new FirebaseRecyclerOptions.Builder<Food>().setQuery(foodlist.orderByChild("menuId").equalTo(categoryId), Food.class).build();

        adapter= new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder holder, final int position, @NonNull final Food model) {
                        holder.food_name.setText(model.getName());
                Picasso.with(FoodList.this).load(model.getImage()).into(holder.food_image);


                holder.imgquickcart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Database(getBaseContext()).addToCart(new Order(
                                adapter.getRef(position).getKey(),
                                model.getName(),
                                "1",
                                model.getPrice(),
                                model.getDiscount(),
                                model.getImage()
                        ));

                        Toast.makeText(FoodList.this, "Added to cart", Toast.LENGTH_SHORT).show();
                    }
                });

                if (localDb.isFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone()))
                {
                    holder.imgfav.setImageResource(R.drawable.ic_favorite_black_24dp);

                }
                holder.imgfav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!localDb.isFavorites(adapter.getRef(position).getKey(),Common.currentUser.getPhone()))
                        {
                            localDb.addToFavorites(adapter.getRef(position).getKey(),Common.currentUser.getPhone());
                            holder.imgfav.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(FoodList.this, ""+model.getName()+" was added to Favorites", Toast.LENGTH_SHORT).show();
                        }else {
                            localDb.deleteFavorite(adapter.getRef(position).getKey(),Common.currentUser.getPhone());
                            holder.imgfav.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(FoodList.this, ""+model.getName()+" was remote from Favorites", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                //btnShare
                holder.imgShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.with(FoodList.this).load(model.getImage())
                                .into(target);
                    }
                });

                final Food local= model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail= new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", adapter.getRef(position).getKey());
                        startActivity(foodDetail);
                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.food_item,viewGroup,false);
                return new FoodViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

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
