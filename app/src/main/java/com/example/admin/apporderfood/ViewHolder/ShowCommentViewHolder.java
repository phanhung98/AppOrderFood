package com.example.admin.apporderfood.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.admin.apporderfood.R;

public class ShowCommentViewHolder extends RecyclerView.ViewHolder {

    public TextView tvUserPhone, tvComment;
    public RatingBar ratingBar;

    public ShowCommentViewHolder(@NonNull View itemView) {
        super(itemView);
        tvComment=(TextView)itemView.findViewById(R.id.tvComment);
        tvUserPhone=(TextView)itemView.findViewById(R.id.tvUserPhone);

        ratingBar=(RatingBar)itemView.findViewById(R.id.ratingbarcomment);
    }
}
