package com.example.android.galleryapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.android.galleryapp.Models.Item;
import com.example.android.galleryapp.databinding.ChipColorBinding;
import com.example.android.galleryapp.databinding.ChipLabelBinding;
import com.example.android.galleryapp.databinding.DialogAddImageBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class AddImageDialog implements itemHelper.OnCompleteListener {
    private Context context;
    private OnCompleteListener listener;
    private DialogAddImageBinding b;
    private LayoutInflater inflater;
    private boolean isCustomLabel;
    private Bitmap image;
    private AlertDialog dialog;
    private String imageUrl;

    void show(Context context,OnCompleteListener listener){
        this.context=context;
        this.listener = listener;

        //Inflate dialogs layout:
        if(context instanceof Gallery_Activity){
            inflater=((Gallery_Activity)context).getLayoutInflater();
                    b=DialogAddImageBinding.inflate(inflater);
        }
        else {
            dialog.dismiss();
            listener.onError("Cast Exception:");
            return;
        }

        //Create and Show Dialog:
         dialog= new MaterialAlertDialogBuilder(context,R.style.CustomDialogTheme)
                .setView(b.getRoot())
                 .setCancelable(false)
                .show();

        //Handle events:
        handleDimensionsInput();

        hideErrorsForEditText();
    }

    //Step 1: Input Dimensions:

    private void handleDimensionsInput() {
        b.fetchImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get Strings From ET:
                String widthStr = b.width.getText().toString().trim(), heightStr = b.height.getText().toString().trim();

                //Guard Code:
                if (widthStr.isEmpty() && heightStr.isEmpty()) {
                    b.width.setError("Please enter at least one dimension");
                    return;
                }

                //Update UI:
                b.inputDimensionsRoot.setVisibility(View.GONE);
                b.progressIndicatorRoot.setVisibility(View.VISIBLE);
                //Keyboard hide:
                 HideKeyboard();

                //Square image:
                if (widthStr.isEmpty()) {
                    int height = Integer.parseInt(heightStr);
                    try {
                        fetchRandomImage(height);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if (heightStr.isEmpty()) {
                    int width = Integer.parseInt(widthStr);
                    try {
                        fetchRandomImage(width);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //Rectangle image:
                else{
                    int height = Integer.parseInt(heightStr);
                    int width = Integer.parseInt(widthStr);
                    try {
                        fetchRandomImage(width,height);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    private void HideKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(b.widthInput.getWindowToken(), 0);
    }


    //Step 2: Fetch Random Image:
    //Rectangular image:

    private void fetchRandomImage(int width, int height) throws IOException {
        new itemHelper()
                .fetchData(width, height, context,this );
    }

    //Square image:
    private void fetchRandomImage(int x) throws IOException {
        new itemHelper()
                .fetchData(x, context, this);
    }

    //Show Data:

    private void showData(String url, Set<Integer> colors, List<String> labels) {
        //Set url of the image:
        this.imageUrl = url;
        //Making process indicator gone and image visible:
        b.progressIndicatorRoot.setVisibility(View.GONE);
        b.mainRoot.setVisibility(View.VISIBLE);
        b.customLabelInput.setVisibility(View.GONE);

        //Setting image view in binding:
        Glide.with(context)
                .load(url)
                .into(b.imageView);

        //Inflating label and color chips in binding:
        inflateColorChips(colors);
        inflateLabelChips(labels);

        //Handling Events:
        handleCustomLabelInput();
        handleAddImageEvent();

    }
    private void handleAddImageEvent() {
        b.AddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int colorChipId=b.colorChips.getCheckedChipId()
                        ,LabelChipId= b.labelChips.getCheckedChipId();

                if(colorChipId== -1 || LabelChipId== -1){
                    Toast.makeText(context,"Please choose color and label",Toast.LENGTH_SHORT).show();
                    return;
                }

                //Get Color and Label:
                String label;
                if(isCustomLabel){
                    label=b.CustomLabelET.getText().toString().trim();
                    if(label.isEmpty()){
                        Toast.makeText(context,"Please enter custom label",Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
                else {
                    label=((Chip)b.labelChips.findViewById(LabelChipId)).getText().toString();
                }

                int color=((Chip)b.colorChips.findViewById(colorChipId)).getChipBackgroundColor().getDefaultColor();

                listener.onImageAdded(new Item(imageUrl,color,label));

                dialog.dismiss();
            }

        });
    }

    private void handleCustomLabelInput() {
        ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
        binding.getRoot().setText("Custom");
        b.labelChips.addView(binding.getRoot());

        binding.getRoot().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    b.customLabelInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    isCustomLabel= isChecked;
                }
            }
        });
    }

    //Label chips:

    private void inflateLabelChips(List<String> labels)  {
        for (String label : labels) {
            ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
            binding.getRoot().setText(label);
            b.labelChips.addView(binding.getRoot());
        }
    }
    //Color chips:

    private void inflateColorChips(Set<Integer> colors) {
        for (int color : colors) {
            ChipColorBinding binding = ChipColorBinding.inflate(inflater);
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(color));
            b.colorChips.addView(binding.getRoot());
        }
    }

    //itemHelper CallBacks:
    //Utils:

    private void hideErrorsForEditText() {
        b.width.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                b.width.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onFetched(String url, Set<Integer> colors, List<String> labels) {
        showData(url,colors,labels);
    }

    @Override
    public void onError(String error) {
        dialog.dismiss();
     listener.onError(error);
    }

    interface OnCompleteListener{

        void onImageAdded(Item item);
        void onError(String error);
    }

}
