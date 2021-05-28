package com.example.android.galleryapp;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class ItemAdapterHelper extends ItemTouchHelper.Callback {
      ItemAdapterListener itemTouchHelperAdapter;

      //Constructor for itemAdapterHelper:
      public ItemAdapterHelper(ItemAdapterListener itemTouchHelperAdapter){
          this.itemTouchHelperAdapter=itemTouchHelperAdapter;
      }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    //Movement flags for drag and swipe actions;
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags=ItemTouchHelper.UP|ItemTouchHelper.DOWN;
        int swipeFlags=ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags,swipeFlags);
    }
    //CallBack(onMove):
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
          itemTouchHelperAdapter.onItemDrag(viewHolder.getAdapterPosition(),target.getAdapterPosition());
        return false;
    }
    //CallBack(onSwipe):
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
          itemTouchHelperAdapter.onItemSwipe(viewHolder.getAdapterPosition());
    }
}
