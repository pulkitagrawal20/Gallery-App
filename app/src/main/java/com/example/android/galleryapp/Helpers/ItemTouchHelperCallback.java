package com.example.android.galleryapp.Helpers;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.galleryapp.Adapters.ItemTouchHelperAdapter;

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
      ItemTouchHelperAdapter itemTouchHelperAdapter;

      //Constructor for itemAdapterHelper:
      public ItemTouchHelperCallback(ItemTouchHelperAdapter itemTouchHelperAdapter){
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
          itemTouchHelperAdapter.onItemMove(viewHolder.getAdapterPosition(),target.getAdapterPosition());
        return false;
    }
    //CallBack(onSwipe):
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
          itemTouchHelperAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }
}
