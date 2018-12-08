package com.example.admin.apporderfood.Databases;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.example.admin.apporderfood.Model.Order;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteAssetHelper {

    private static final String DB_NAME= "EatDB.db";
    private static final int DB_VER= 1;

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    public List<Order> getCarts()
    {
        SQLiteDatabase db= getReadableDatabase();
        SQLiteQueryBuilder qb= new SQLiteQueryBuilder();

        String[] sqlSelect={"ID","ProductId","ProductName","Quantily","Price","Discount","Image"};
        String sqltable= "OrderDetail";

        qb.setTables(sqltable);
        Cursor c= qb.query(db, sqlSelect, null,null,null,null,null);

        final List<Order> result= new ArrayList<>();
        if (c.moveToFirst()){
            do {
                result.add(new Order(
                        c.getInt(c.getColumnIndex("ID")),
                        c.getString(c.getColumnIndex("ProductId")),
                        c.getString(c.getColumnIndex("ProductName")),
                        c.getString(c.getColumnIndex("Quantily")),
                        c.getString(c.getColumnIndex("Price")),
                        c.getString(c.getColumnIndex("Discount")),
                        c.getString(c.getColumnIndex("Image"))

                        ));
            }while (c.moveToNext());
        }
        return result;
    }

    public void addToCart(Order order){
        SQLiteDatabase db= getReadableDatabase();
        String query= String.format("INSERT INTO OrderDetail(ProductId,ProductName,Quantily,Price,Discount,Image) VALUES('%s','%s','%s','%s','%s','%s');",
        order.getProductId(),
        order.getProductName(),
        order.getQuantily(),
        order.getPrice(),
        order.getDiscount(),
                order.getImage());

        db.execSQL(query);
    }

    public void CleanCart(){
        SQLiteDatabase db= getReadableDatabase();
        String query= String.format("DELETE FROM OrderDetail");

        db.execSQL(query);
    }

    public void addToFavorites(String foodid,String userPhone)
    {
        SQLiteDatabase db= getReadableDatabase();
        String query= String.format("INSERT INTO Favorites(FoodId,UserPhone) VALUES('%s','%s');",foodid,userPhone);
        db.execSQL(query);
    }

    public void deleteFavorite(String foodid,String userPhone)
    {
        SQLiteDatabase db= getReadableDatabase();
        String query= String.format("DELETE FROM Favorites WHERE FoodId='%s' and UserPhone='%s';",foodid,userPhone);
        db.execSQL(query);
    }

    public boolean isFavorites(String foodid,String userPhone)
    {
        SQLiteDatabase db= getReadableDatabase();
        String query= String.format("SELECT * FROM Favorites WHERE FoodId='%s' and UserPhone='%s';",foodid,userPhone);
     Cursor cursor= db.rawQuery(query,null);
     if (cursor.getCount() <= 0)
     {
         cursor.close();
         return false;
     }
     cursor.close();
     return true;
    }

    public int getCountCart() {
        int count=0;

        SQLiteDatabase db= getReadableDatabase();
        String query= String.format("SELECT COUNT(*) FROM OrderDetail");
        Cursor cursor= db.rawQuery(query,null);
        if (cursor.moveToFirst())
        {
            do {
                count= cursor.getInt(0);

            }while (cursor.moveToNext());
        }

        return count;

    }

    public void updateCart(Order order) {
        SQLiteDatabase db= getReadableDatabase();
        String query= String.format("UPDATE OrderDetail SET Quantily= %s WHERE ID= %d", order.getQuantily(),order.getID());
        db.execSQL(query);
    }
}
