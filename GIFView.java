package com.example.edwardfouxvictorious.giftest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GIFView extends View {

    //Set true to use decodeStream
    //Set false to use decodeByteArray
    private static final boolean DECODE_STREAM = true;

    private InputStream gifInputStream;
    private Movie gifMovie;
    private int movieWidth, movieHeight;
    private long movieDuration;
    private long mMovieStart;
    private Context mContext;
    private String gifAddr;

    public GIFView(Context context, String gifUrl) {
        super(context);
        mContext = context;
        gifAddr = gifUrl;
    }

    public GIFView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GIFView(Context context, AttributeSet attrs,
                   int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(){
        if (mContext == null || gifAddr == null){
            //TODO LOG
            return;
        }
        setFocusable(true);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        gifMovie = null;
        movieWidth = 0;
        movieHeight = 0;
        movieDuration = 0;

        Thread threadLoadGif = new Thread(new Runnable(){

            @Override
            public void run() {
                try {
                    URL gifURL = new URL(gifAddr);

                    HttpURLConnection connection = (HttpURLConnection)gifURL.openConnection();
                    gifInputStream = connection.getInputStream();

                    //Insert dummy sleep
                    //to simulate network delay
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    if(DECODE_STREAM){
                        gifMovie = Movie.decodeStream(gifInputStream);
                    }else{
                        byte[] array = streamToBytes(gifInputStream);
                        gifMovie = Movie.decodeByteArray(array, 0, array.length);
                    }
                    movieWidth = gifMovie.width();
                    movieHeight = gifMovie.height();
                    movieDuration = gifMovie.duration();

                    ((MainActivity)mContext).runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            //request re-draw layout
                            invalidate();
                            requestLayout();
                        }});

                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }});

        threadLoadGif.start();

    }

    private static byte[] streamToBytes(InputStream is) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = is.read(buffer)) >= 0) {
                os.write(buffer, 0, len);
            }
        } catch (java.io.IOException e) {
        }
        return os.toByteArray();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,
                             int heightMeasureSpec) {
        setMeasuredDimension(movieWidth, movieHeight);
    }

    public int getMovieWidth(){
        return movieWidth;
    }

    public int getMovieHeight(){
        return movieHeight;
    }

    public long getMovieDuration(){
        return movieDuration;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        long now = android.os.SystemClock.uptimeMillis();
        if (mMovieStart == 0) {   // first time
            mMovieStart = now;
        }

        if (gifMovie != null) {

            int dur = gifMovie.duration();
            if (dur == 0) {
                dur = 1000;
            }

            int relTime = (int)((now - mMovieStart) % dur);

            gifMovie.setTime(relTime);

            gifMovie.draw(canvas, 0, 0);
            invalidate();

        }

    }

}