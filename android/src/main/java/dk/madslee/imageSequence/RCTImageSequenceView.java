package dk.madslee.imageSequence;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;


public class RCTImageSequenceView extends ImageView {
    private Integer framesPerSecond = 24;
    private Boolean loop = true;
    private Boolean isAnim = false;

    private ArrayList<AsyncTask> activeTasks;
    private HashMap<Integer, Bitmap> bitmaps;
    private RCTResourceDrawableIdHelper resourceDrawableIdHelper;

    AnimationDrawable animationDrawable;

    private int mWith;
    private int mHeight;

    private Context mContext;

    public RCTImageSequenceView(Context context) {
        this(context, null);
    }

    public RCTImageSequenceView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RCTImageSequenceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        resourceDrawableIdHelper = new RCTResourceDrawableIdHelper();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
            if (this.uri.startsWith("http")) {
                try {
                    return Glide.with(this.context).load(this.uri).asBitmap().into(mWith, mHeight).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                return null;
            }
            return this.loadBitmapByLocalResource(this.uri);
        }


        private Bitmap loadBitmapByLocalResource(String uri) {
            try {
                return Glide.with(this.context).load(resourceDrawableIdHelper.getResourceDrawableId(this.context, uri)).asBitmap().into(mWith, mHeight).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            return null;
        }


        private Bitmap loadBitmapByExternalURL(String uri) {
            Bitmap bitmap = null;
            InputStream in = null;
            try {
                in = new URL(uri).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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

    public void setImages(final ArrayList<String> uris) {
        if (isLoading()) {
            // cancel ongoing tasks (if still loading previous images)
            for (int index = 0; index < activeTasks.size(); index++) {
                activeTasks.get(index).cancel(true);
            }
        }

        activeTasks = new ArrayList<>(uris.size());
        bitmaps = new HashMap<>(uris.size());

        this.post(new Runnable() {
            @Override
            public void run() {

                mWith = getWidth();
                mHeight = getHeight();
                
                if (mWith==0){
                    mWith=700;
                }
                if (mHeight==0){
                    mHeight=700;
                }

                for (int index = 0; index < uris.size(); index++) {
                    DownloadImageTask task = new DownloadImageTask(index, uris.get(index), getContext());
                    activeTasks.add(task);
                    try {
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } catch (RejectedExecutionException e) {
                        Log.e("image-sequence", "DownloadImageTask failed" + e.getMessage());
                        break;
                    }
                }
            }
        });

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

    public void setIsAnim(Boolean isAnim) {
        this.isAnim = isAnim;
        if (isLoaded()) {
            setupAnimationDrawable();
        }
    }

    public Boolean getAnim() {
        return isAnim;
    }

    private boolean isLoaded() {
        return !isLoading() && bitmaps != null && !bitmaps.isEmpty();
    }

    private boolean isLoading() {
        return activeTasks != null && !activeTasks.isEmpty();
    }

    private void setupAnimationDrawable() {
        animationDrawable = new AnimationDrawable();
        for (int index = 0; index < bitmaps.size(); index++) {
            BitmapDrawable drawable = new BitmapDrawable(this.getResources(), bitmaps.get(index));
            animationDrawable.addFrame(drawable, 1000 / framesPerSecond);
        }

        animationDrawable.setOneShot(!this.loop);

        this.setImageDrawable(animationDrawable);

        if (isAnim) {
            animationDrawable.start();
        } else {
            animationDrawable.stop();
        }
    }
}
