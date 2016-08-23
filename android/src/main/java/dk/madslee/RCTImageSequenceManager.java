package dk.madslee;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.ArrayList;


public class RCTImageSequenceManager extends SimpleViewManager<RCTImageSequenceView> {
    @Override
    public String getName() {
        return "RCTImageSequence";
    }

    @Override
    protected RCTImageSequenceView createViewInstance(ThemedReactContext reactContext) {
        return new RCTImageSequenceView(reactContext);
    }

    /**
     * sets the speed of the animation.
     *
     * @param view
     * @param framesPerSecond
     */
    @ReactProp(name = "framesPerSecond")
    public void setFramesPerSecond(final RCTImageSequenceView view, Integer framesPerSecond) {
        view.setFramesPerSecond(framesPerSecond);
    }

    /**
     * @param view
     * @param images an array of ReadableMap's {uri: "http://...."} return value of the resolveAssetSource(....)
     */
    @ReactProp(name = "images")
    public void setImages(final RCTImageSequenceView view, ReadableArray images) {
        ArrayList<String> uris = new ArrayList<>();
        for (int index = 0; index < images.size(); index++) {
            ReadableMap map = images.getMap(index);
            uris.add(map.getString("uri"));
        }

        view.setImages(uris);
    }

    /**
     * sets whether or not the animation starts automatically.
     *
     * @param view
     * @param start
     */
    @ReactProp(name = "start")
    public void setStart(final RCTImageSequenceView view, boolean start) {
        view.setStart(start);
    }

    /**
     * sets whether or not the animation plays endlessly or once
     *
     * @param view
     * @param oneShot
     */
    @ReactProp(name = "oneShot")
    public void setOneShot(final RCTImageSequenceView view, boolean oneShot) {
        view.setOneShot(oneShot);
    }
}
