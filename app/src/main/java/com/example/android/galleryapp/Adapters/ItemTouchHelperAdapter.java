package com.example.android.galleryapp.Adapters;

public interface ItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
}
