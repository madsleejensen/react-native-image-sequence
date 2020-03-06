package dk.madslee.imageSequence;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.RejectedExecutionException;


public class RCTImageSequenceView extends ImageView {
    private Integer framesPerSecond = 24;
    private Boolean loop = true;
    private ArrayList<AsyncTask> activeTasks;
    private HashMap<Integer, Bitmap> bitmaps;
    private RCTResourceDrawableIdHelper resourceDrawableIdHelper;

    public RCTImageSequenceView(Context context) {
        super(context);

        resourceDrawableIdHelper = new RCTResourceDrawableIdHelper();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final Integer index;
        private final String uri;
        private final Context context;

        public DownloadImageTask(Integer index, String uri, Context context) {
            this.index = index;
            this.uri = uri;
            this.context = context;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                if (this.uri.startsWith("http")) {
                    return this.loadBitmapByExternalURL(this.uri);
                } else if (this.uri.startsWith("file://")) {
                    File file = new File(new URI(this.uri));
                    return BitmapFactory.decodeFile(file.getAbsolutePath());
                }

                return this.loadBitmapByLocalResource(this.uri);
            } catch (Exception e) {

            }
            return null;
        }


        private Bitmap loadBitmapByLocalResource(String uri) {
            return BitmapFactory.decodeResource(this.context.getResources(), resourceDrawableIdHelper.getResourceDrawableId(this.context, uri));
        }

        private Bitmap loadBitmapByExternalURL(String uri) {
            Bitmap bitmap = null;
            InputStream in = null;

            try {
                in = new URL(uri).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (Exception e) {
                }
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (!isCancelled() && bitmap != null) {
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
        Drawable drawable = getDrawable();
        if(drawable instanceof AnimationDrawable){
            ((AnimationDrawable)drawable).stop();
        }

        if (isLoading()) {
            // cancel ongoing tasks (if still loading previous images)
            for (int index = 0; index < activeTasks.size(); index++) {
                activeTasks.get(index).cancel(true);
            }
        }

        activeTasks = new ArrayList<>(uris.size());
        bitmaps = new HashMap<>(uris.size());

        for (int index = 0; index < uris.size(); index++) {
            DownloadImageTask task = new DownloadImageTask(index, uris.get(index), getContext());
            activeTasks.add(task);

            try {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (RejectedExecutionException e){
                Log.e("image-sequence", "DownloadImageTask failed" + e.getMessage());
                break;
            }
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
        return activeTasks != null && !activeTasks.isEmpty();
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
    }
}
