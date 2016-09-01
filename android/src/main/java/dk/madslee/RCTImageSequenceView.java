package dk.madslee;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.net.Uri;
import android.widget.ImageView;
import android.support.annotation.Nullable;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class RCTImageSequenceView extends ImageView {
    private Integer framesPerSecond = 24;
    private boolean start = true;
    private boolean oneShot = false;
    private ArrayList<AsyncTask> activeTasks;
    private HashMap<Integer, Bitmap> bitmaps;
    private AnimationDrawable animationDrawable;
    private Context context = null;

    private ThemedReactContext mThemedReactContext;

    public RCTImageSequenceView(ThemedReactContext themedReactContext) {
        super(themedReactContext);
        mThemedReactContext = themedReactContext;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final Integer index;
        private final String uri;

        public DownloadImageTask(Integer index, String uri) {
            this.index = index;
            this.uri = uri;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;

            try {
                InputStream in;
                if (this.uri.startsWith("http") == true) {
                  in = new URL(this.uri).openStream();
                  bitmap = BitmapFactory.decodeStream(in);
                } else {
                  bitmap = BitmapFactory.decodeResource(mThemedReactContext.getResources(), mThemedReactContext.getResources().getIdentifier(this.uri , "drawable", mThemedReactContext.getPackageName()));
                }
            } catch (IOException e) {
                WritableMap eventParams = Arguments.createMap();
                eventParams.putString("index", this.index.toString());
                eventParams.putString("uri", this.uri);
                eventParams.putString("message", e.getMessage());
                sendEvent("onError", eventParams);
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (!isCancelled()) {
                onTaskCompleted(this, index, bitmap);
            }
        }
    }

    private void onTaskCompleted(DownloadImageTask downloadImageTask, Integer index, Bitmap bitmap) {
        if (index == 0) {
            // first image should be displayed as soon as possible.
            this.setImageBitmap(bitmap);
        }

        bitmaps.put(index, bitmap);
        activeTasks.remove(downloadImageTask);

        if (activeTasks.isEmpty()) {
            sendEvent("onLoadComplete", null);
            setupAnimationDrawable();
        }
    }

    public void setImages(ArrayList<String> uris) {
        if (isLoading()) {
            // cancel ongoing tasks (if still loading previous images)
            for (int index = 0; index < activeTasks.size(); index++) {
                activeTasks.get(index).cancel(true);
            }
        }

        activeTasks = new ArrayList<>(uris.size());
        bitmaps = new HashMap<>(uris.size());

        for (int index = 0; index < uris.size(); index++) {
            String uri = uris.get(index);
            DownloadImageTask task = new DownloadImageTask(index, uri);
            activeTasks.add(task);

            WritableMap eventParams = Arguments.createMap();
            eventParams.putString("index", Integer.toString(index));
            eventParams.putString("uri", uri);
            sendEvent("onLoadStart", eventParams);

            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void setFramesPerSecond(Integer framesPerSecond) {
        this.framesPerSecond = framesPerSecond;

        // updating frames per second, results in building a new AnimationDrawable (because we cant alter frame duration)
        if (isLoaded()) {
            setupAnimationDrawable();
        }
    }

    public void setStart(boolean start) {
        this.start = start;

        //Start again if already loaded.
        if (start == true && isLoaded()) {
            setupAnimationDrawable();
        }
    }

    public void setOneShot(boolean oneShot) {
        this.oneShot = oneShot;

        //Start again if already loaded.
        if (isLoaded() && null != this.animationDrawable) {
            this.animationDrawable.setOneShot(oneShot);
        }
    }

    public void start() {
        if (null != this.animationDrawable) {
            this.animationDrawable.setOneShot(this.oneShot);
            this.animationDrawable.start();
        }
    }

    private boolean isLoaded() {
        return !isLoading() && bitmaps != null && !bitmaps.isEmpty();
    }

    private boolean isLoading() {
        return activeTasks != null && !activeTasks.isEmpty();
    }

    private void setupAnimationDrawable() {
        this.animationDrawable = new AnimationDrawable();
        for (int index = 0; index < bitmaps.size(); index++) {
            BitmapDrawable drawable = new BitmapDrawable(this.getResources(), bitmaps.get(index));
            this.animationDrawable.addFrame(drawable, 1000 / framesPerSecond);
        }

        if (start == true) {
            start();
        }

        this.setImageDrawable(this.animationDrawable);
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        mThemedReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }
}
