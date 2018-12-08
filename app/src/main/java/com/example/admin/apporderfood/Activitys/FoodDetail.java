package com.example.admin.apporderfood.Activitys;

import android.content.Context;
import android.content.Intent;
import android.media.Rating;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.admin.apporderfood.Common.Common;
import com.example.admin.apporderfood.Common.NetworkChangeReceiver;
import com.example.admin.apporderfood.Databases.Database;
import com.example.admin.apporderfood.Model.Food;
import com.example.admin.apporderfood.Model.Order;
import com.example.admin.apporderfood.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Arrays;

import info.hoang8f.widget.FButton;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {

    TextView food_name, food_price, food_discription;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnRate;
    CounterFab btncart;
    ElegantNumberButton btnPrice;
    RatingBar ratingBar;

    FButton btnshowComment;

    String foodId="";


    FirebaseDatabase database;
    DatabaseReference foods;
    DatabaseReference ratingbl;

    Food currentFood;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath).build());

        //firebase
        database= FirebaseDatabase.getInstance();
        foods= database.getReference("Food");
        ratingbl=database.getReference("Rating");

        food_name=(TextView)findViewById(R.id.food_name);
        food_price=(TextView)findViewById(R.id.food_price);
        food_discription=(TextView)findViewById(R.id.food_discription);
        food_image=(ImageView)findViewById(R.id.img_food);
        btnshowComment=(FButton)findViewById(R.id.btnShowComment);

        btncart=(CounterFab) findViewById(R.id.btnCart);
        btnPrice=(ElegantNumberButton)findViewById(R.id.price_button);
        btnRate=(FloatingActionButton)findViewById(R.id.btnRate);
        ratingBar=(RatingBar)findViewById(R.id.ratingBar);


        btnRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });

        btnshowComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent comment= new Intent(FoodDetail.this, ShowComment.class);
                comment.putExtra(Common.INTENT_FOOD_ID, foodId);
                startActivity(comment);
            }
        });

        btncart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            new Database(getBaseContext()).addToCart(new Order(
                    foodId,
                    currentFood.getName(),
                    btnPrice.getNumber(),
                    currentFood.getPrice(),
                    currentFood.getDiscount(),
                    currentFood.getImage()
            ));
                btncart.setCount(new Database(getBaseContext()).getCountCart());
                Toast.makeText(FoodDetail.this, "Added to cart", Toast.LENGTH_SHORT).show();

            }
        });

        btncart.setCount(new Database(this).getCountCart());

        collapsingToolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        if (getIntent() != null)
            foodId= getIntent().getStringExtra("FoodId");

            if (NetworkChangeReceiver.isOnline(getBaseContext())) {
                getDetailFood(foodId);
                getRatingFood(foodId);
            }


    }

    private void getRatingFood(String foodId) {

        Query foodRating= ratingbl.child(Common.currentUser.getPhone()).orderByChild("foodId").equalTo(foodId);
        foodRating.addValueEventListener(new ValueEventListener() {
            int count=0;
            int sum=0;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {
                    com.example.admin.apporderfood.Model.Rating item= postSnapShot.getValue(com.example.admin.apporderfood.Model.Rating.class);
                    sum=Integer.parseInt(item.getRateValue());
                    count++;

                }
                if (count != 0){
                    float average= sum/count;
                    ratingBar.setRating(average);
                    count--;
//                    Toast.makeText(FoodDetail.this, ""+sum, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {

        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad","Not Bad","Quite Ok","Very Good","Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Please select some stars and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment here...")
                .setHintTextColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetail.this)
                .show();

    }

    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);

                Picasso.with(FoodDetail.this).load(currentFood.getImage()).into(food_image);

                collapsingToolbarLayout.setTitle(currentFood.getName());
                food_price.setText(currentFood.getPrice());
                food_name.setText(currentFood.getName());
                food_discription.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {




    }

    @Override
    public void onPositiveButtonClicked(int i, @NotNull String comment) {

        final com.example.admin.apporderfood.Model.Rating rating = new com.example.admin.apporderfood.Model.Rating(Common.currentUser.getPhone(),
                foodId,
                String.valueOf(i),
                comment
                );
//        ratingbl.push().setValue(rating).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                Toast.makeText(FoodDetail.this, "Thank you for sumbmit rating!!!", Toast.LENGTH_SHORT).show();
//            }
//        });

        ratingbl.child(Common.currentUser.getPhone()).child(foodId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(Common.currentUser.getPhone()).child(foodId).exists())
                {
                    ratingbl.child(Common.currentUser.getPhone()).child(foodId).removeValue();

                    ratingbl.child(Common.currentUser.getPhone()).child(foodId).setValue(rating);

                }else {
                    ratingbl.child(Common.currentUser.getPhone()).child(foodId).setValue(rating);

                }
                Toast.makeText(FoodDetail.this, "Thank you for sumbmit rating!!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        btncart.setCount(new Database(this).getCountCart());
    }
}
