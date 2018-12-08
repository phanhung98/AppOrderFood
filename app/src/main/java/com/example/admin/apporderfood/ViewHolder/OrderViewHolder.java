package com.example.admin.apporderfood.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.admin.apporderfood.Interface.ItemClickListener;
import com.example.admin.apporderfood.R;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView tvOrderId, tvOrderStatus, tvOrderPhone, tvOrderAddress;

    private ItemClickListener itemClickListener;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);

        tvOrderAddress=(TextView)itemView.findViewById(R.id.order_address);
        tvOrderId=(TextView)itemView.findViewById(R.id.order_id);
        tvOrderPhone=(TextView)itemView.findViewById(R.id.order_phone);
        tvOrderStatus=(TextView)itemView.findViewById(R.id.order_status);

        itemView.setOnClickListener(this);


    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition(),false);
    }
}
