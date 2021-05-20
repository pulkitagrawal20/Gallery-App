  package com.example.android.galleryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.state.State;
import androidx.palette.graphics.Palette;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.ScriptGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.android.galleryapp.Models.Item;
import com.example.android.galleryapp.databinding.ActivityGalleryBinding;
import com.example.android.galleryapp.databinding.ChipColorBinding;
import com.example.android.galleryapp.databinding.ChipLabelBinding;
import com.example.android.galleryapp.databinding.DialogAddImageBinding;
import com.example.android.galleryapp.databinding.ItemCardBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

  public class Gallery_Activity extends AppCompatActivity {
      ActivityGalleryBinding b;

      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);


          b = ActivityGalleryBinding.inflate((getLayoutInflater()));
          setContentView(b.getRoot());


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
          return false;
      }

      private void showAddImageDialog() {
          new AddImageDialog()
                  .show(this, new AddImageDialog.OnCompleteListener() {
                      @Override
                      public void onImageAdded(Item item) {
                          inflateViewForItem(item);
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
          ItemCardBinding binding=ItemCardBinding.inflate(getLayoutInflater());

          //Bind Data:
          binding.ImageView.setImageBitmap(item.image);
          binding.title.setText(item.label);
          binding.title.setBackgroundColor(item.color);

          //Add it to the List:
          b.List.addView(binding.getRoot());
      }

  }

