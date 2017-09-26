package com.xebia.activityrecognition;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

public class RNActivityRecognitionNativeModule extends ReactContextBaseJavaModule {
    private static final String REACT_CLASS = "ActivityRecognition";
    private ReactApplicationContext mReactContext;
    private ActivityRecognizer mActivityRecognizer = null;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    public RNActivityRecognitionNativeModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mReactContext = reactContext;
    }

    @ReactMethod
    public void startWithCallback(int detectionIntervalMillis, final Callback onSuccess, final Callback onError) {
        try {
            if (mActivityRecognizer == null) {
                mActivityRecognizer = new ActivityRecognizer(mReactContext);
            }

            mActivityRecognizer.start((long) detectionIntervalMillis);
        } catch (Error e) {
            onError.invoke(e.getMessage());
            return;
        }

        onSuccess.invoke();
    }

    @ReactMethod
    public void stopWithCallback(final Callback onSuccess, final Callback onError) {
        try {
            if (mActivityRecognizer != null) {
                mActivityRecognizer.stop();
            }
        } catch (Error e) {
            onError.invoke(e.getMessage());
            return;
        }

        onSuccess.invoke();
    }
}
