package com.example.admin.apporderfood.Activitys;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.example.admin.apporderfood.Common.Common;
import com.example.admin.apporderfood.Common.NetworkChangeReceiver;
import com.example.admin.apporderfood.Databases.Database;
import com.example.admin.apporderfood.Interface.ItemClickListener;
import com.example.admin.apporderfood.Model.Banner;
import com.example.admin.apporderfood.Model.Category;
import com.example.admin.apporderfood.Model.Token;
import com.example.admin.apporderfood.R;

import com.example.admin.apporderfood.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


@SuppressWarnings("ALL")
public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category;

    TextView tvFullName;
    RecyclerView recyclerView_menu;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerOptions<Category> options;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;

    SwipeRefreshLayout swipeRefreshLayout;
    CounterFab fab;

    HashMap<String,String> image_list;
    SliderLayout mSlider;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath).build());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipelayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
                );
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetworkChangeReceiver.isOnline(getBaseContext())) {
                    LoadMenu();
                }else {
                    Toast.makeText(Home.this, "Please check your internet", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (NetworkChangeReceiver.isOnline(getBaseContext())) {
                    LoadMenu();
                }else {
                    Toast.makeText(Home.this, "Please check your internet", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });


        Paper.init(this);

        //Init firebase
        database=FirebaseDatabase.getInstance();
        category= database.getReference("Category");

         fab = (CounterFab) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              Intent cartintent= new Intent(Home.this, Cart.class);
              startActivity(cartintent);
            }
        });
        fab.setCount(new Database(this).getCountCart());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set name for user
        View headerView= navigationView.getHeaderView(0);
        tvFullName=(TextView)headerView.findViewById(R.id.tvFullName);
        tvFullName.setText(Common.currentUser.getName());

        //Load menu
        recyclerView_menu= (RecyclerView) findViewById(R.id.recyclerview_menu);
        recyclerView_menu.setHasFixedSize(true);
        layoutManager= new GridLayoutManager(this,2);
        recyclerView_menu.setLayoutManager(layoutManager);

        if (NetworkChangeReceiver.isOnline(getBaseContext())) {
            LoadMenu();
        }else {
            Toast.makeText(Home.this, "Please check your internet", Toast.LENGTH_SHORT).show();
            return;
        }

        updateToken(FirebaseInstanceId.getInstance().getToken());


        setupSlider();

    }

    private void setupSlider() {

        mSlider=(SliderLayout) findViewById(R.id.slider);
        image_list= new HashMap<>();

        final DatabaseReference baners= database.getReference("Banner");

        baners.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {
                    Banner banner= postSnapShot.getValue(Banner.class);

                    //we will contact String name and id like PIZZA_01 => PIZZA for show discription and id for foodid click
                    image_list.put(banner.getName()+"@@@"+banner.getId(),banner.getImage());

                }
                for (String key:image_list.keySet())
                {
                    String[] keySplit= key.split("@@@");
                    String nameofFood= keySplit[0];
                    String idofFood= keySplit[1];


                    final TextSliderView textSliderView= new TextSliderView(getBaseContext());
                    textSliderView.description(nameofFood)
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent intent= new Intent(Home.this,FoodDetail.class);
                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);
                                }
                            });

                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("FoodId",idofFood);

                    mSlider.addSlider(textSliderView);
                    //Remove event after finnish
                    baners.removeEventListener(this);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(4000);

    }

    private void updateToken(String token) {
        FirebaseDatabase db= FirebaseDatabase.getInstance();
        DatabaseReference tokens= db.getReference("Token");
        Token data= new Token(token,false);
        tokens.child(Common.currentUser.getPhone()).setValue(data);
    }

    private void LoadMenu() {

        options= new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category, Category.class).build();
        adapter= new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder holder, int position, @NonNull Category model) {
                    holder.tvMenuName.setText(model.getName());
                Picasso.with(Home.this).load(model.getImage()).into(holder.menuImage);
                final Category clicItem= model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodlist= new Intent(Home.this, FoodList.class);
                        foodlist.putExtra("CategoryId", adapter.getRef(position).getKey());
                        startActivity(foodlist);
                    }
                });
            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.menu_item,viewGroup,false);
                return new MenuViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount(new Database(this).getCountCart());

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        mSlider.stopAutoCycle();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_setting) {

            showSettingDialog();

        }  else if (id == R.id.nav_cart) {

            Intent cartInten= new Intent(Home.this, Cart.class);
            startActivity(cartInten);

        } else if (id == R.id.nav_order) {

            Intent orderInten= new Intent(Home.this, OrderStatus.class);
            startActivity(orderInten);
        } else if (id == R.id.nav_logout) {

            MainActivity.logout();

                Intent signout= new Intent(Home.this, MainActivity.class);
                signout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(signout);
        } else if (id == R.id.nav_homeaddress) {

            showDialogHomeAddress();

        }
        else if (id == R.id.nav_updateName) {

            showUpdateChangeUserName();

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showSettingDialog() {

        AlertDialog.Builder alertDialog= new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("SETTING");


        LayoutInflater inflater= this.getLayoutInflater();
        View setting_layout = inflater.inflate(R.layout.setting_layout,null);

        final CheckBox ckb_sub= (CheckBox)setting_layout.findViewById(R.id.ckb_sub_new);

        Paper.init(this);
        String isubscribe= Paper.book().read("sub_new");
        if (isubscribe == null || TextUtils.isEmpty(isubscribe) || isubscribe.equals("false"))
            ckb_sub.setChecked(false);
        else
            ckb_sub.setChecked(true);

        alertDialog.setView(setting_layout);

        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

               if (ckb_sub.isChecked())
               {
                   FirebaseMessaging.getInstance().subscribeToTopic(Common.topicName);
                   //Write values
                   Paper.book().write("sub_new","true");
               }else
               {
                   FirebaseMessaging.getInstance().subscribeToTopic(Common.topicName);
                   //Write values
                   Paper.book().write("sub_new","false");
               }

            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }

    private void showUpdateChangeUserName() {

        final AlertDialog.Builder alertDialog= new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("CHANGE User Name");
        alertDialog.setMessage("Enter your user name");

        LayoutInflater inflater= this.getLayoutInflater();
        View changeUsername_layout = inflater.inflate(R.layout.change_username_layout,null);

        final MaterialEditText edtChangeUsername=(MaterialEditText)changeUsername_layout.findViewById(R.id.edtchangeUsername);

        alertDialog.setView(changeUsername_layout);

        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final android.app.AlertDialog waitingDialog= new
                        SpotsDialog.Builder().setContext(Home.this).build();
                waitingDialog.show();

                Map<String, Object> updateName= new HashMap<>();
                updateName.put("name", edtChangeUsername.getText().toString());

                FirebaseDatabase.getInstance()
                        .getReference("User").child(Common.currentUser.getPhone())
                        .updateChildren(updateName)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                waitingDialog.dismiss();
                                if (task.isSuccessful())
                                    Toast.makeText(Home.this, "Name was updated", Toast.LENGTH_SHORT).show();
                            }
                        });



            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();

    }

    private void showDialogHomeAddress() {

        AlertDialog.Builder alertDialog= new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("CHANGE HOME ADDRESS");
        alertDialog.setMessage("Enter your home address");

        LayoutInflater inflater= this.getLayoutInflater();
        View home_address_layout = inflater.inflate(R.layout.home_address_layout,null);

        final MaterialEditText edtHomeaddress=(MaterialEditText)home_address_layout.findViewById(R.id.edthomeaddress);

        alertDialog.setView(home_address_layout);

        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                Common.currentUser.setHomeAddress(edtHomeaddress.getText().toString());

                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Home.this, "Update address Successful", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }
}
