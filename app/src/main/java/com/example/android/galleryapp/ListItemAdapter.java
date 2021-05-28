package com.example.android.galleryapp;

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
import com.example.android.galleryapp.databinding.ItemCardBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ItemHolder> implements ItemAdapterListener {
    private Context context;
    private List<Item> items;
    private List<Item> itemsToShow;
    public ItemTouchHelper mainItemTouchHelper;
    public String url;
    public int index;
    public ItemCardBinding itemCardBinding;

    //Constructor for ListItemAdapter:
    public ListItemAdapter(Context context, List<Item>items){

        this.context = context;
        this.items = items;
        //Initializing sorted items as all items:
        itemsToShow=items;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflate layout:
        ItemCardBinding binding=ItemCardBinding.inflate(LayoutInflater.from(context),parent,false);

        //create and return ViewHolder:
        return new ItemHolder(binding);
    }

    @Override
    //Binding data to VH:
    public void onBindViewHolder(@NonNull  ListItemAdapter.ItemHolder holder, int position) {
        //Inflate and bind Card:
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
    public void onItemDrag(int from,int to){
        Item fromItem=items.get(from);
        items.remove(fromItem);
        items.add(to,fromItem);
        itemsToShow=items;
        notifyItemMoved(from,to);
    }
    @Override
    public void onItemSwipe(int position){
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

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener, View.OnTouchListener,GestureDetector.OnGestureListener{
        public ItemCardBinding binding;
        GestureDetector gestureDetector;

        public ItemHolder(@NonNull ItemCardBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
            gestureDetector=new GestureDetector(binding.getRoot().getContext(), this);
            //Setting OnTouch Listener for drag item:
            binding.ImageView.setOnTouchListener(this);
            //Create context Menu:
            binding.title.setOnCreateContextMenuListener (this);
        }


        @Override
        //Context Menu For Edit and Share Options:
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select Action");
            menu.add(this.getAdapterPosition(),R.id.editMenuItem,0,"Edit");
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
            mainItemTouchHelper.startDrag(this);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

}
