package com.example.android.galleryapp.Adapters;

import android.content.Context;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android.galleryapp.Models.Item;
import com.example.android.galleryapp.R;
import com.example.android.galleryapp.databinding.ItemCardBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListItemTouchHelperAdapter extends RecyclerView.Adapter<ListItemTouchHelperAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {
    private Context context;
    private List<Item> items;
    public List<Item> itemsToShow;
    public ItemTouchHelper mainItemTouchHelper;
    public String url;
    public int index;
    public ItemCardBinding itemCardBinding;
    public int mode;
    public List<ItemViewHolder> holderList=new ArrayList<>();

    //Constructor for ListItemAdapter:
    public ListItemTouchHelperAdapter(Context context, List<Item>items){

        this.context = context;
        this.items = items;
        //Initializing sorted items as all items:
        itemsToShow=items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflate layout:
        ItemCardBinding binding=ItemCardBinding.inflate(LayoutInflater.from(context),parent,false);

        //create and return ViewHolder:
        return new ItemViewHolder(binding);
    }

    @Override
    //Binding data to VH:
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        //Inflate and bind Card:
        holderList.add(holder);
        holder.binding.title.setText(itemsToShow.get(position).label);
        holder.binding.title.setBackgroundColor(itemsToShow.get(position).color);
        Glide.with(context)
                .asBitmap()
                .load(itemsToShow.get(position).url)
                .into(holder.binding.ImageView);

    }

    @Override
    //Size:
    public int getItemCount() {
        return itemsToShow.size();
    }

    //Search Option:
    public void filter(String query){
        if(query.trim().isEmpty()){
            itemsToShow=items;
            notifyDataSetChanged();
            return;
        }
        query=query.toLowerCase();

        //Items according to the query searched:
        List<Item>queryItems=new ArrayList<>();
        for(Item item:items){
            if(item.label.toLowerCase().contains(query)){
                queryItems.add(item);
            }
        }
        itemsToShow=queryItems;
        notifyDataSetChanged();
    }
    public void setListItemAdapterHelper(ItemTouchHelper itemTouchHelper){
        mainItemTouchHelper=itemTouchHelper;
    }
    @Override
    //Change card position on drag:
    public void onItemMove(int fromPosition, int toPosition){
        Item fromItem=items.get(fromPosition);
        items.remove(fromItem);
        items.add(toPosition,fromItem);
        itemsToShow=items;
        notifyItemMoved(fromPosition, toPosition);
    }
    @Override
    public void onItemDismiss(int position){
        return;
    }

    //Alphabetically sorting items:
    public void sortAlpha() {
        //Sort List of Items according to alphabetical order of labels given:
        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.label.compareTo(o2.label);
            }
        });
        itemsToShow = items;
        notifyDataSetChanged();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener, View.OnTouchListener,GestureDetector.OnGestureListener{
        public ItemCardBinding binding;
        GestureDetector gestureDetector;

        public ItemViewHolder(@NonNull ItemCardBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
            gestureDetector=new GestureDetector(binding.getRoot().getContext(), this);
            eventListenerHandler();
        }

        public void eventListenerHandler() {
            if(mode==0){
                binding.ImageView.setOnTouchListener(null);
                binding.title.setOnTouchListener(null);
                binding.title.setOnCreateContextMenuListener(this);
                binding.ImageView.setOnCreateContextMenuListener(this);
            }
            else if(mode==1){
                binding.title.setOnCreateContextMenuListener(null);
                binding.ImageView.setOnCreateContextMenuListener(null);
                binding.title.setOnTouchListener(this);
                binding.ImageView.setOnTouchListener(this);
            }
        }


        @Override
        //Context Menu For Edit and Share Options:
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select Action");
            menu.add(this.getAdapterPosition(), R.id.editMenuItem,0,"Edit");
            menu.add(this.getAdapterPosition(),R.id.shareImage,0,"Share");
            url=items.get(this.getAdapterPosition()).url;
            index=this.getAdapterPosition();
            itemCardBinding=binding;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if(mode==1)
            mainItemTouchHelper.startDrag(this);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

}
