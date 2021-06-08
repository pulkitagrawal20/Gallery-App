package com.example.android.galleryapp.Models;

public class Item {
   public String url;
   public String label;
   public int color;

    public Item(String url, int color, String label){

        this.url = url;
        this.color = color;
        this.label = label;
    }

}
