package com.example.android.galleryapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.android.galleryapp.Models.Item;
import com.example.android.galleryapp.databinding.ChipColorBinding;
import com.example.android.galleryapp.databinding.ChipLabelBinding;
import com.example.android.galleryapp.databinding.DialogAddfromDeviceBinding;
import com.example.android.galleryapp.databinding.DialogEditImageBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class editImageDialog {
    private Context context;
    private DialogEditImageBinding editBinding;
    private OnCompleteListener listener;
    private LayoutInflater inflater;
    private boolean isCustomLabel;
    private Bitmap image;
    private AlertDialog dialog;
    private String imageUrl;
    private Set<Integer> colors;

    public void show(Context context,String imageUrl ,OnCompleteListener listener) {
        this.context = context;
        this.imageUrl = imageUrl;
        this.listener = listener;
        if(context instanceof Gallery_Activity){
            inflater=((Gallery_Activity)context).getLayoutInflater();
            editBinding= DialogEditImageBinding.inflate(inflater);
        }
        else {
            dialog.dismiss();
            listener.onError("Cast Exception:");
            return;
        }

        //Create and Show Dialog:
        dialog= new MaterialAlertDialogBuilder(context,R.style.CustomDialogTheme)
                .setView(editBinding.getRoot())
                .setCancelable(false)
                .show();

        fetchImage(imageUrl);

        handleUpdateButton();
    }

    private void fetchImage(String url) {

        Glide.with(context)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        image = resource;
                        extractPaletteFromBitmap();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private void handleUpdateButton() {
        editBinding.editedImageView.setImageBitmap(image);
        editBinding.updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int colorChipId=editBinding.eColorChips.getCheckedChipId()
                        ,LabelChipId= editBinding.eLabelChips.getCheckedChipId();

                if(colorChipId== -1 || LabelChipId== -1){
                    Toast.makeText(context,"Please choose color and label",Toast.LENGTH_SHORT).show();
                    return;
                }

                //Get Color and Label:
                String label;
                if(isCustomLabel){
                    label=editBinding.eCustomLabelET.getText().toString().trim();
                    if(label.isEmpty()){
                        Toast.makeText(context,"Please enter custom label",Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
                else {
                    label=((Chip)editBinding.eLabelChips.findViewById(LabelChipId)).getText().toString();
                }

                int color=((Chip)editBinding.eColorChips.findViewById(colorChipId)).getChipBackgroundColor().getDefaultColor();

                listener.onEditCompleted(new Item(imageUrl,color,label));

                dialog.dismiss();
            }
        });
    }
    //PaletteHelper..............................

    private void extractPaletteFromBitmap(){
        Palette.from(image).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {
                colors= getColorsFromPalette(p);

                labelImage();
            }
        });
    }

    private void labelImage() {
        InputImage inputImage=InputImage.fromBitmap(image,0);
        ImageLabeler labeler= ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        labeler.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        List<String> strings=new ArrayList<>();
                        for(ImageLabel label:labels){
                            strings.add(label.getText());
                        }
                        inflateColorChips(colors);
                        inflateLabelChips(strings);
                        editBinding.editedImageView.setImageBitmap(image);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onError(e.toString());
                    }
                });
    }

    private Set<Integer> getColorsFromPalette(Palette p) {
        Set<Integer> colors = new HashSet<>();

        colors.add(p.getVibrantColor(0));
        colors.add(p.getLightVibrantColor(0));
        colors.add(p.getDarkVibrantColor(0));

        colors.add(p.getMutedColor(0));
        colors.add(p.getLightMutedColor(0));
        colors.add(p.getDarkMutedColor(0));

        colors.add(p.getVibrantColor(0));
        colors.remove(0);


        return colors;
    }



    private void handleCustomLabel() {
        ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
        binding.getRoot().setText("Custom");
        editBinding.eLabelChips.addView(binding.getRoot());

        binding.getRoot().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editBinding.eCustomLabelInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                isCustomLabel= isChecked;

            }
        });
    }
    //Color chips:
    private void inflateColorChips(Set<Integer> colors) {
        for (int color : colors) {
            ChipColorBinding binding = ChipColorBinding.inflate(inflater);
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(color));
            editBinding.eColorChips.addView(binding.getRoot());
        }
    }
    //Label chips:

    private void inflateLabelChips(List<String> labels)  {
        for (String label : labels) {
            ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
            binding.getRoot().setText(label);
            editBinding.eLabelChips.addView(binding.getRoot());
        }
        handleCustomLabel();
    }

    interface OnCompleteListener{
        void onEditCompleted(Item item);
        void onError(String error);
    }
}


