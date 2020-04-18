package dk.madslee.imageSequence;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import com.facebook.common.util.UriUtil;


public class RCTResourceDrawableIdHelper {

    private Map<String, Integer> mResourceDrawableIdMap;

    public RCTResourceDrawableIdHelper() {
        mResourceDrawableIdMap = new HashMap<String, Integer>();
    }

    public int getResourceDrawableId(Context context, @Nullable String name) {
        if (name == null || name.isEmpty()) {
            return 0;
        }
        name = name.toLowerCase().replace("-", "_");
        if (mResourceDrawableIdMap.containsKey(name)) {
            return mResourceDrawableIdMap.get(name);
        }
        int id = context.getResources().getIdentifier(
                name,
                "drawable",
                context.getPackageName());
        mResourceDrawableIdMap.put(name, id);
        return id;
    }

    public @Nullable Drawable getResourceDrawable(Context context, @Nullable String name) {
        int resId = getResourceDrawableId(context, name);
        return resId > 0 ? ContextCompat.getDrawable(context, resId) : null;
    }

    public Uri getResourceDrawableUri(Context context, @Nullable String name) {
        int resId = getResourceDrawableId(context, name);
        return resId > 0 ? new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                .path(String.valueOf(resId))
                .build() : Uri.EMPTY;
    }
}
