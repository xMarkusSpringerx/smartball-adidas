package com.adidas.sb.hackathon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.URL;

public class RecyclerViewHolders extends RecyclerView.ViewHolder{

    public TextView playerName;
    public TextView playerScore;
    public ImageView playerAvatar;

    public RecyclerViewHolders(View itemView) {
        super(itemView);

        playerName = (TextView)itemView.findViewById(R.id.player_name);
        playerScore = (TextView)itemView.findViewById(R.id.player_score);
        playerAvatar = (ImageView)itemView.findViewById(R.id.circleView);
    }
}