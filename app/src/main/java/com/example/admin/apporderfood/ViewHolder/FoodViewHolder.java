package com.example.admin.apporderfood.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin.apporderfood.Interface.ItemClickListener;
import com.example.admin.apporderfood.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView food_name,food_price_fooditem;
    public ImageView food_image, imgfav,imgShare,imgquickcart;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);

        food_name=(TextView)itemView.findViewById(R.id.food_name);
        food_price_fooditem=(TextView)itemView.findViewById(R.id.food_price_fooditem);
        food_image=(ImageView)itemView.findViewById(R.id.food_image);
        imgfav=(ImageView)itemView.findViewById(R.id.imgfav);
        imgShare=(ImageView)itemView.findViewById(R.id.imgShare);
        imgquickcart=(ImageView)itemView.findViewById(R.id.imgquickcart);

        itemView.setOnClickListener(this);
    }



    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }
}
