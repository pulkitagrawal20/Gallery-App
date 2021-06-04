package com.example.android.galleryapp.Helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.android.galleryapp.Helpers.RedirectURLHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class itemHelper {

    private Context context;
    private OnCompleteListener listener;
    private String rectangularImageURL = "https://picsum.photos/%d/%d", squareImageURL = "https://picsum.photos/%d";

    private Bitmap bitmap;
    private Set<Integer> colors;
    private String redURL;

    //Triggers...............................

    //For Rectangular image:
    public void fetchData(int x, int y, Context context, OnCompleteListener listener) throws IOException {
        this.context = context;

        this.listener = listener;

        fetchUrl(
                String.format(rectangularImageURL, x, y)
        );
    }

    //For Square image:
    public void fetchData(int x, Context context, OnCompleteListener listener) throws IOException {
        this.context = context;
        this.listener = listener;

        fetchUrl(
                String.format(squareImageURL, x)
        );
    }

    public void fetchData(String url,Context context,OnCompleteListener listener ){
        this.context = context;
        this.listener = listener;
        redURL = url;
        fetchImage(url);
    }


    //Fetching URL:
    void fetchUrl(String url) throws IOException{
        new RedirectURLHelper().fetchRedUrl(new RedirectURLHelper.OnFetchedUrlListener() {
            @Override
            public void OnFetchedURL(String url) {
                redURL = url;
                fetchImage(redURL);
            }

            @Override
            public void onError(String error) {
                listener.onError(error);
            }
        }).execute(url);
    }


    //ImageFetcher............................
    void fetchImage(String url) {
        Glide.with(context)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bitmap = resource;
                        extractPaletteFromBitmap();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        listener.onError("Image Load Failed");
                    }
                });
    }

    //PaletteHelper..............................
    private void extractPaletteFromBitmap(){
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {
                colors= getColorsFromPalette(p);
                
                labelImage();
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

    //LabelFetcher...................................
    private void labelImage() {
        InputImage image=InputImage.fromBitmap(bitmap,0);
        ImageLabeler labeler= ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        labeler.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        List<String> strings=new ArrayList<>();
                        for(ImageLabel label:labels){
                            strings.add(label.getText());
                        }
                        listener.onFetched(redURL,colors,strings);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onError(e.toString());
                    }
                });
    }

    //Listener...................
    public interface OnCompleteListener{

        void onFetched(String url, Set<Integer> colors, List<String> labels);
        void onError(String error);
    }
}
