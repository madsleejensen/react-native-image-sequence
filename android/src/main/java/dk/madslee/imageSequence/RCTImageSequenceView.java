package dk.madslee.imageSequence;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.RejectedExecutionException;

public class RCTImageSequenceView extends AppCompatImageView {
    private Integer framesPerSecond = 24;
    private Boolean loop = true;
    private AsyncTask activeTask;
    private ArrayList<String> uris;
    private HashMap<Integer, Bitmap> bitmaps;
    private RCTResourceDrawableIdHelper resourceDrawableIdHelper;

    public RCTImageSequenceView(Context context) {
        super(context);
        resourceDrawableIdHelper = new RCTResourceDrawableIdHelper();
        this.setScaleType(ScaleType.CENTER_CROP);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, HashMap<Integer, Bitmap>> {
        private final ArrayList<String> uris;
        private final Context context;

        public DownloadImageTask(ArrayList<String> uris, Context context) {
            this.uris = uris;
            this.context = context;
        }

        @Override
        protected HashMap<Integer, Bitmap> doInBackground(String... params) {
            Log.i ("react-native-image-sequence", "doInBackground");

            HashMap<Integer, Bitmap> bitmaps = new HashMap<>();
            Bitmap bitmap = null;
            String uri = "";
            WritableMap eventParams = Arguments.createMap();
            for (int index = 0; index < uris.size(); index++) {
                Log.i ("react-native-image-sequence", "doInBackground:index=" + index);
                eventParams = Arguments.createMap();
                eventParams.putInt("index", index);
                sendEvent("onImageLoding", eventParams);

                uri = uris.get(index);
                if (uri.startsWith("http")) {
                    bitmap = this.loadBitmapByExternalURL(uri);
                }else{
                    bitmap = this.loadBitmapByLocalResource(uri);
                }
                bitmaps.put(index, bitmap);
            }

            return bitmaps;
        }

        private Bitmap loadBitmapByLocalResource(String uri) {
            return BitmapFactory.decodeResource(this.context.getResources(), resourceDrawableIdHelper.getResourceDrawableId(this.context, uri));
        }

        private Bitmap loadBitmapByExternalURL(String uri) {
            Bitmap bitmap = null;
            try {
                bitmap = Glide.with(getContext()).load(uri).asBitmap().into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
            } catch (Exception e) {
                Log.e("react-native-image-sequence", "loadBitmapByExternalURL failed" + e.getMessage());
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(HashMap<Integer, Bitmap> bitmaps) {
            Log.i ("react-native-image-sequence", "onPostExecute:bitmaps=" + bitmaps.size());
            if (!isCancelled()) {
                onTaskCompleted(this, bitmaps);
            }
        }
    }

    private void onTaskCompleted(DownloadImageTask downloadImageTask, HashMap<Integer, Bitmap> bitmaps) {
        Log.i ("react-native-image-sequence", "onTaskCompleted");

        terminateActiveTask();

        if (bitmaps == null || bitmaps.size() == 0)  return;

        // first image should be displayed as soon as possible.
        this.setImageBitmap(bitmaps.get(0));

        this.bitmaps = bitmaps;
        setupAnimationDrawable();
    }

    public void setImages(ArrayList<String> uris) {
        Log.i ("react-native-image-sequence", "setImages:uris=" + uris.size());

        if (isLoading()) {
            // cancel ongoing tasks (if still loading previous images)
            terminateActiveTask();
        }

        DownloadImageTask task = new DownloadImageTask(uris, getContext());
        this.activeTask = task;
        try {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (RejectedExecutionException e){
            Log.e("react-native-image-sequence", "DownloadImageTask failed:" + e.getMessage());
        }

    }

    public void setFramesPerSecond(Integer framesPerSecond) {
        this.framesPerSecond = framesPerSecond;

        // updating frames per second, results in building a new AnimationDrawable (because we cant alter frame duration)
        if (isLoaded()) {
            setupAnimationDrawable();
        }
    }

    public void setLoop(Boolean loop) {
        this.loop = loop;

        // updating looping, results in building a new AnimationDrawable
        if (isLoaded()) {
            setupAnimationDrawable();
        }
    }

    private boolean isLoaded() {
        return !isLoading() && bitmaps != null && !bitmaps.isEmpty();
    }

    private boolean isLoading() {
        return activeTask != null;
    }

    private void terminateActiveTask(){
        if(this.activeTask != null){
            this.activeTask.cancel(true);
            this.activeTask = null;
        }
    }

    private void setupAnimationDrawable() {
        AnimationDrawable animationDrawable = new AnimationDrawable();
        for (int index = 0; index < bitmaps.size(); index++) {
            BitmapDrawable drawable = new BitmapDrawable(this.getResources(), bitmaps.get(index));
            animationDrawable.addFrame(drawable, 1000 / framesPerSecond);
        }

        animationDrawable.setOneShot(!this.loop);

        this.setImageDrawable(animationDrawable);
        animationDrawable.start();

        sendEvent("onAnimationStart", null);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (isLoading()) {
            terminateActiveTask();
        }
    }

    void sendEvent(String eventName, @Nullable WritableMap params) {
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), eventName, params);
    }
}
