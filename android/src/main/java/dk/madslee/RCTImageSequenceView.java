package dk.madslee;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

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

    public RCTImageSequenceView(Context context) {
        super(context);

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
                InputStream in = new URL(this.uri).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                e.printStackTrace();
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
            DownloadImageTask task = new DownloadImageTask(index, uris.get(index));
            activeTasks.add(task);

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
}
