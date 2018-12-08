package com.example.admin.apporderfood.ViewHolder;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.admin.apporderfood.Activitys.Cart;
import com.example.admin.apporderfood.Common.Common;
import com.example.admin.apporderfood.Databases.Database;
import com.example.admin.apporderfood.Interface.ItemClickListener;
import com.example.admin.apporderfood.Model.Order;
import com.example.admin.apporderfood.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
View.OnCreateContextMenuListener
{

    public TextView tv_cart_name, tv_cart_price;
    public ImageView img_cart;
    public ElegantNumberButton btn_quantily;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);

        tv_cart_name=(TextView)itemView.findViewById(R.id.cart_item_name);
        tv_cart_price=(TextView)itemView.findViewById(R.id.cart_item_price);
        img_cart=(ImageView)itemView.findViewById(R.id.img_cart);
        btn_quantily=(ElegantNumberButton)itemView.findViewById(R.id.btn_quantily);


        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select the Action");
            menu.add(0,0,getAdapterPosition(),Common.DELETE);
    }
}

public class CartAdapter extends RecyclerView.Adapter<CartViewHolder>{

    private List<Order> listData= new ArrayList<>();
    private Cart cart;

    public CartAdapter(List<Order> listData, Cart cart) {
        this.listData = listData;
        this.cart = cart;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater= LayoutInflater.from(cart );
        View itemView= inflater.inflate(R.layout.cart_layout,viewGroup,false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder cartViewHolder, final int i) {


        Picasso.with(cart).load(listData.get(i).getImage())
                .resize(70,70).centerCrop().into(cartViewHolder.img_cart);

            int price= (Integer.parseInt(listData.get(i).getPrice()))*(Integer.parseInt(listData.get(i).getQuantily()));
            cartViewHolder.tv_cart_price.setText(price+"");
            cartViewHolder.tv_cart_name.setText(listData.get(i).getProductName());

            cartViewHolder.btn_quantily.setNumber(listData.get(i).getQuantily());
            cartViewHolder.btn_quantily.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
                @Override
                public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                    Order order= listData.get(i);
                    order.setQuantily(String.valueOf(newValue));
                    new Database(cart).updateCart(order);



                    int total=0;
                    List<Order> orders= new Database(cart).getCarts();
                    for (Order item:orders)
                        total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(item.getQuantily()));
                    cart.tvTotalPrice.setText(total+"");

                }
            });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}
