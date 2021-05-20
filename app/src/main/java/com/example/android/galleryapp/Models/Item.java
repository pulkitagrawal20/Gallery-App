package com.example.android.galleryapp.Models;

import android.graphics.Bitmap;

import java.lang.reflect.Constructor;

public class Item {
   public Bitmap image;
   public String label;
   public int color;

    public Item(Bitmap image,int color,String label){

        this.image = image;
        this.color = color;
        this.label = label;
    }
}
