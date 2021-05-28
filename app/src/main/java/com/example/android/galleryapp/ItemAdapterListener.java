package com.example.android.galleryapp;

public interface ItemAdapterListener {
    void onItemDrag(int from, int to);
    void onItemSwipe(int position);
}
