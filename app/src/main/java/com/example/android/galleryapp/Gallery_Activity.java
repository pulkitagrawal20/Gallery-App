  package com.example.android.galleryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.android.galleryapp.Adapters.ListItemTouchHelperAdapter;
import com.example.android.galleryapp.Helpers.ItemTouchHelperCallback;
import com.example.android.galleryapp.Models.Item;
import com.example.android.galleryapp.databinding.ActivityGalleryBinding;
import com.example.android.galleryapp.databinding.ItemCardBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

  public class Gallery_Activity extends AppCompatActivity {
      private static final int RESULT_LOAD_IMAGE =0;
      private static final int RESULT =1001 ;
      ActivityGalleryBinding b;
      List<Item> items=new ArrayList<>();
      SharedPreferences preferences;
      private ItemCardBinding binding;
      ListItemTouchHelperAdapter adapter;
      int mode=0;
      ItemTouchHelper.Callback callback2;
      ItemTouchHelper itemTouchHelper1;
      private String imageUrl;
      private Context context=this;
      private boolean isEdited;

      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);


          b = ActivityGalleryBinding.inflate((getLayoutInflater()));
          setContentView(b.getRoot());

          preferences= getPreferences(MODE_PRIVATE);
          getDataFromSharedPreferences();

          if(!items.isEmpty()){
              showListItems(items);}

              else
                  {b.itemsList.setVisibility(View.VISIBLE);}
          enableDisableDrag();
      }

      private void enableDisableDrag() {
          b.OnOffDrag.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  if(mode==0){
                      mode=1;
                      adapter.mode=1;
                      Toast.makeText(context,"Drag Enabled",Toast.LENGTH_SHORT).show();
                      List<ListItemTouchHelperAdapter.ItemViewHolder> holders=adapter.holderList;
                      b.OnOffDrag.setBackgroundTintList(getResources().getColorStateList(R.color.purple_500));
                      b.OnOffDrag.setRippleColor(getResources().getColorStateList(R.color.purple_700));

                      b.OnOffDrag.setImageResource(R.drawable.drag);
                      for(int i=0;i<holders.size();i++){
                          holders.get(i).eventListenerHandler();
                      }
                        itemTouchHelper1.attachToRecyclerView(b.List);
                  }
                  else{
                      mode=0;
                      adapter.mode=0;
                      Toast.makeText(context,"Drag Disabled",Toast.LENGTH_SHORT).show();
                      List<ListItemTouchHelperAdapter.ItemViewHolder> holders=adapter.holderList;
                      for(int i=0;i<holders.size();i++){
                          holders.get(i).eventListenerHandler();
                      }
                      b.OnOffDrag.setBackgroundTintList(getResources().getColorStateList(R.color.purple_700));
                      b.OnOffDrag.setRippleColor(getResources().getColorStateList(R.color.purple_500));
                      b.OnOffDrag.setImageResource(R.drawable.notdrag);
                      itemTouchHelper1.attachToRecyclerView(null);
                  }

              }
          });
      }

      void dragDropButtonRestore() {
          if (mode == 1) {
              mode = 1;
              adapter.mode = 1;
              List<ListItemTouchHelperAdapter.ItemViewHolder> holders = adapter.holderList;
              b.OnOffDrag.setBackgroundTintList(getResources().getColorStateList(R.color.purple_500));
              b.OnOffDrag.setRippleColor(getResources().getColorStateList(R.color.purple_700));

              b.OnOffDrag.setImageResource(R.drawable.drag);
              for (int i = 0; i < holders.size(); i++) {
                  holders.get(i).eventListenerHandler();
              }
              itemTouchHelper1.attachToRecyclerView(b.List);
          } else {
              mode = 0;
              adapter.mode = 0;
              List<ListItemTouchHelperAdapter.ItemViewHolder> holders = adapter.holderList;
              for (int i = 0; i < holders.size(); i++) {
                  holders.get(i).eventListenerHandler();
              }
              b.OnOffDrag.setBackgroundTintList(getResources().getColorStateList(R.color.purple_700));
              b.OnOffDrag.setRippleColor(getResources().getColorStateList(R.color.purple_500));
              b.OnOffDrag.setImageResource(R.drawable.notdrag);
              itemTouchHelper1.attachToRecyclerView(null);
          }

      }


      @Override
      //Handling events of Context Menu Item Selection:
      public boolean onContextItemSelected (MenuItem item) {
          //For edit image option::
          if(item.getItemId()==R.id.editMenuItem){
              editImage();
              return true;
          }
          //For share image option:
          if(item.getItemId()==R.id.shareImage){
              sharePermission();
          return true;}
          return super.onContextItemSelected(item);
      }

      private void editImage() {
          imageUrl = adapter.url;
          int index= adapter.index;
          binding= adapter.itemCardBinding;
          new ImageDialog().editFetchImage(this, items.get(index), new ImageDialog.OnCompleteListener() {
              @Override
              public void onImageAdded(Item item) {
                  items.set(index,item);
                  adapter.notifyDataSetChanged();

              }

              @Override
              public void onError(String error) {

              }
          });
      }

      private void sharePermission() {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
              if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                  String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

                  requestPermissions(permission, RESULT);
              }

          else
          {
              shareItem(binding);
          }
      }
          else{
              shareItem(binding);
          }
      }


      @Override
      public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
          super.onRequestPermissionsResult(requestCode, permissions, grantResults);
          if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
              shareItem(binding);
          }
          else{
              Toast.makeText(this, "Storage permission Denied", Toast.LENGTH_SHORT).show();
          }
      }

      @Override
      public boolean onCreateOptionsMenu(Menu menu) {
          getMenuInflater().inflate(R.menu.gallery,menu);

          SearchView searchView=(SearchView)menu.findItem(R.id.search).getActionView();

          //Listener to add Search Function of adapter:
          searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
              @Override
              public boolean onQueryTextSubmit(String query) {
                  adapter.filter(query);
                  return true;
              }

              @Override
              public boolean onQueryTextChange(String newText) {
                  adapter.filter(newText);
                  return false;
              }
          });
          return true;
      }

      //Shows Icons In menu:
      @Override
      public boolean onOptionsItemSelected(@NonNull MenuItem item) {
          if(item.getItemId()==R.id.add_image){
              showAddImageDialog();
              return true;
          }
          if(item.getItemId()==R.id.AddFromGallery){
              addFromGallery();
          }
          if(item.getItemId()==R.id.sorting){
              adapter.sortAlpha();
          }
          return false;
      }

      //Callback for swipe options:
      ItemTouchHelper.SimpleCallback callback=new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
          @Override
          public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
              return false;
          }

          @Override
          public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                items.remove(viewHolder.getAdapterPosition());
              Toast.makeText(context, "Image Removed", Toast.LENGTH_SHORT).show();
              if (items.isEmpty())
                  b.itemsList.setVisibility(View.VISIBLE);
                  b.SearchItemsList.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
          }
      };

      private void addFromGallery() {
          Intent intent=new Intent(
                  Intent.ACTION_PICK,
                  MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

          startActivityForResult(intent,RESULT_LOAD_IMAGE);
      }

      private void showAddImageDialog() {
          new ImageDialog()
                  .showDialog(this, new ImageDialog.OnCompleteListener() {
                      @Override
                      public void onImageAdded(Item item) {
                          items.add(item);
                          showListItems(items);

                          b.itemsList.setVisibility(View.GONE);
                          b.SearchItemsList.setVisibility(View.GONE);
                      }

                      @Override
                      public void onError(String error) {
                          new  MaterialAlertDialogBuilder(Gallery_Activity.this)
                                  .setTitle("Error")
                                  .setMessage(error)
                                  .show();
                      }
                  });
      }

      private void showListItems(List<Item> item) {
          adapter = new ListItemTouchHelperAdapter(this, items);
          b.List.setLayoutManager(new LinearLayoutManager(this));

          ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
          adapter.setListItemAdapterHelper(itemTouchHelper);
          itemTouchHelper.attachToRecyclerView(b.List);
          callback2 = new ItemTouchHelperCallback(adapter);
          itemTouchHelper1 = new ItemTouchHelper(callback2);
          adapter.setListItemAdapterHelper(itemTouchHelper1);
          b.List.setAdapter(adapter);

          dragDropButtonRestore();

      }


      private void getDataFromSharedPreferences() {
          int itemCount=preferences.getInt(Constants.NO_OF_IMAGES,0);

          for (int i=1;i<=itemCount;i++){
              //Make a new item and get objects from json:
              Item item= itemFromJson(preferences.getString(Constants.ITEMS+i,""));
              items.add(item);
          }
          mode=preferences.getInt(Constants.MODE,0);
          showListItems(items);

          if(items==null){
              b.itemsList.setVisibility(View.VISIBLE);
              b.SearchItemsList.setVisibility(View.GONE);
          }
          else {
              b.itemsList.setVisibility(View.GONE);
              b.SearchItemsList.setVisibility(View.GONE);
          }
      }

      //To get Json for the Item...
      private String jsonFromItem(Item item){
          Gson json=new Gson();
          return json.toJson(item);
      }

      //To get Item from Json...
      private Item itemFromJson(String string){
          Gson json2= new Gson();
          return json2.fromJson(string,Item.class);
      }

      @Override
      protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
          super.onActivityResult(requestCode, resultCode, data);
          if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
              Uri selectedImage = data.getData();
              String uri = selectedImage.toString();

              new ImageDialog().fetchDataForGallery(uri,this, new ImageDialog.OnCompleteListener() {
                  @Override
                  public void onImageAdded(Item item) {
                    items.add(item);
                    showListItems(items);

                    b.itemsList.setVisibility(View.GONE);
                    b.SearchItemsList.setVisibility(View.GONE);
                  }

                  @Override
                  public void onError(String error) {

                  }
              });
          }

      }

          public Bitmap loadBitmapFromView(View view) {
              //Define a bitmap with the same size as the view
              Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
              //Bind a canvas to it
              Canvas canvas = new Canvas(returnedBitmap);
              //Get the view's background
              Drawable bgDrawable = view.getBackground();
              if (bgDrawable != null)
                  //has background drawable, then draw it on the canvas
                  bgDrawable.draw(canvas);
              else
                  //does not have background drawable, then draw white background on the canvas
                  canvas.drawColor(Color.WHITE);
              // draw the view on the canvas
              view.draw(canvas);
              //return the bitmap
              return returnedBitmap;
          }

      private void shareItem(ItemCardBinding binding){

                  Bitmap icon = loadBitmapFromView(binding.getRoot());


          // Calling the intent to share the bitmap
          Intent share = new Intent(Intent.ACTION_SEND);
          share.setType("image/jpeg");

          ContentValues values = new ContentValues();
          values.put(MediaStore.Images.Media.TITLE, "title");
          values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
          Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                  values);


          OutputStream outputStream;
          try {
              outputStream = getContentResolver().openOutputStream(uri);
              icon.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
              outputStream.close();
          } catch (Exception e) {
              System.err.println(e.toString());
          }

          share.putExtra(Intent.EXTRA_STREAM, uri);
          startActivity(Intent.createChooser(share, "Share Image"));
              }


      @Override
      protected void onPause() {
          super.onPause();

          // Putting all the objects in the shared preferences
          int itemCount = 0;
          for (Item item : items) {
              // Check for the item
              if (item != null) {
                  // incrementing the index
                  itemCount++;

                  // Saving the item in the shared preferences
                  preferences.edit()
                          .putString(Constants.ITEMS + itemCount, jsonFromItem(item))
                          .apply();
              }
          }
          preferences.edit()
                  .putInt(Constants.NO_OF_IMAGES, itemCount)
                  .apply();
          preferences.edit()
                  .putInt(Constants.MODE,mode);
      }

  }
