package com.example.admin.apporderfood.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin.apporderfood.Interface.ItemClickListener;
import com.example.admin.apporderfood.R;
import com.google.android.gms.actions.ItemListIntents;

public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView tvMenuName;
    public ImageView menuImage;

    private ItemClickListener itemClickListener;

    public MenuViewHolder(@NonNull View itemView) {
        super(itemView);

        tvMenuName=(TextView)itemView.findViewById(R.id.menu_name);
        menuImage=(ImageView)itemView.findViewById(R.id.menu_image);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false );
    }
}
