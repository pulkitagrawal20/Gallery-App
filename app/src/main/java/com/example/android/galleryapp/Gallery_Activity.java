  package com.example.android.galleryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import com.example.android.galleryapp.databinding.DialogAddfromDeviceBinding;
import com.example.android.galleryapp.databinding.ItemCardBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

  public class Gallery_Activity extends AppCompatActivity {
      private static final int RESULT_LOAD_IMAGE =0;
      ActivityGalleryBinding b;
      List<Item> items=new ArrayList<>();
      SharedPreferences preferences;
      private List<String>urls=new ArrayList<>();
      private Menu globalMenuItem;

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
                          b.itemsList.setVisibility(View.GONE);
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
          ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());

          //Bind Data:
          Glide.with(this)
                  .load(item.url)
                  .into(binding.ImageView);

          binding.title.setText(item.label);
          binding.title.setBackgroundColor(item.color);
          urls.add(item.url);


          //Add it to the List:
          b.List.addView(binding.getRoot());
      }


      private void getDataFromSharedPreferences() {
          int itemCount=preferences.getInt(Constants.NO_OF_IMAGES,0);
          if(itemCount!=0){
              b.itemsList.setVisibility(View.GONE);
          }
          for (int i=0;i<itemCount;i++){
              Item item=new Item(preferences.getString(Constants.IMAGE+i,"")
                      ,preferences.getInt(Constants.COLOR+i,0)
                      ,preferences.getString(Constants.LABEL+i,""));

              items.add(item);
              inflateViewForItem(item);
          }
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

      @Override
      protected void onPause() {
          super.onPause();
          int numOfImage=items.size();
          preferences.edit().putInt(Constants.NO_OF_IMAGES,numOfImage).apply();

          int imageCount=0;
          for(Item item:items){
              preferences.edit()
                      .putInt(Constants.COLOR+imageCount,item.color)
                      .putString(Constants.LABEL+imageCount,item.label)
                      .putString(Constants.IMAGE+imageCount,urls.get(imageCount))
                      .apply();

              imageCount++;

          }
          preferences.edit().commit();
      }
  }

