package com.example.android.galleryapp;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class RedirectURLHelper extends AsyncTask<String ,Void,String> {
    private String redirectURL;
    private OnFetchedUrlListener listener;

    public RedirectURLHelper fetchRedUrl(OnFetchedUrlListener listener){

        this.listener = listener;
        return this;
    }


    @Override
    protected String doInBackground(String... strings) {
        String url=strings[0];
        HttpURLConnection connection=null;
        InputStream inputStream=null;
        try{
            connection= (HttpURLConnection) new URL(url).openConnection();
        }catch (IOException e){
            e.printStackTrace();
        }
        try{
            connection.connect();
        }catch (IOException e){
            e.printStackTrace();
        }try {
            inputStream=connection.getInputStream();
        }catch (IOException e){
            e.printStackTrace();
        }
        redirectURL = connection.getURL().toString();
        try{
            inputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        return redirectURL;

    }

    @Override
    protected void onPostExecute(String s) {
        listener.OnFetchedURL(redirectURL);
    }

    interface OnFetchedUrlListener{
        void OnFetchedURL(String url);
    }
}
