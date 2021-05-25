  package com.example.android.galleryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
      ActivityGalleryBinding b;
      List<Item> items=new ArrayList<>();
      SharedPreferences preferences;
      private ItemCardBinding binding;

      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);


          b = ActivityGalleryBinding.inflate((getLayoutInflater()));
          setContentView(b.getRoot());

          preferences= getPreferences(MODE_PRIVATE);
          getDataFromSharedPreferences();

          if(!items.isEmpty()){
              b.itemsList.setVisibility(View.GONE);
          }
      }



      @Override
      public boolean onCreateOptionsMenu(Menu menu) {
          getMenuInflater().inflate(R.menu.gallery,menu);
          return true;
      }

      @Override
      public boolean onOptionsItemSelected(@NonNull MenuItem item) {
          if(item.getItemId()==R.id.add_image){
              showAddImageDialog();
              return true;
          }
          if(item.getItemId()==R.id.AddFromGallery){
              addFromGallery();
          }
          return false;
      }

      private void addFromGallery() {
          Intent intent=new Intent(
                  Intent.ACTION_PICK,
                  MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

          startActivityForResult(intent,RESULT_LOAD_IMAGE);
      }

      private void showAddImageDialog() {
          new AddImageDialog()
                  .show(this, new AddImageDialog.OnCompleteListener() {
                      @Override
                      public void onImageAdded(Item item) {
                          items.add(item);
                          inflateViewForItem(item);
                          shareItem();
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

      private void inflateViewForItem(Item item) {

          //Inflate Layout:
          binding = ItemCardBinding.inflate(getLayoutInflater());

          //Bind Data:
          Glide.with(this)
                  .load(item.url)
                  .into(binding.ImageView);

          binding.title.setText(item.label);
          binding.title.setBackgroundColor(item.color);


          //Add it to the List:
          b.List.addView(binding.getRoot());
          shareItem();

          if (items.isEmpty()) {
              b.itemsList.setVisibility(View.VISIBLE);
          } else {
              b.itemsList.setVisibility(View.GONE);
          }
      }


      private void getDataFromSharedPreferences() {
          int itemCount=preferences.getInt(Constants.NO_OF_IMAGES,0);

          for (int i=1;i<=itemCount;i++){
              //Make a new item and get objects from json:
              Item item= itemFromJson(preferences.getString(Constants.ITEMS+i,""));
              items.add(item);
              inflateViewForItem(item);
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
              String[] filePathColumn = {MediaStore.Images.Media.DATA};
              Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
              cursor.moveToFirst();
              int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
              String picturePath = cursor.getString(columnIndex);
              cursor.close();

              String uri = selectedImage.toString();

              new addFromDevice().show(this, uri, new addFromDevice.OnCompleteListener() {
                  @Override
                  public void onAddCompleted(Item item) {
                      items.add(item);
                      inflateViewForItem(item);
                      b.itemsList.setVisibility(View.GONE);
                  }

                  @Override
                  public void onError(String error) {
                      new MaterialAlertDialogBuilder(Gallery_Activity.this)
                              .setTitle("ERROR")
                              .setMessage(error)
                              .show();
                  }
              });
          }
      }

      public static Bitmap loadBitmapFromView(View v) {
          Bitmap bitmap;
          v.setDrawingCacheEnabled(true);
          bitmap = Bitmap.createBitmap(v.getDrawingCache());
          v.setDrawingCacheEnabled(false);
          return bitmap;
      }

      private void shareItem(){
          binding.shareButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {

                  Bitmap icon = loadBitmapFromView(b.List);

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
      });

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
      }
  }
