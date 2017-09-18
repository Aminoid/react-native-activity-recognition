package com.xebia.activityrecognition;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.google.android.gms.location.DetectedActivity;

import java.util.HashMap;
import java.util.Map;

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

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();

        // Export a few common activity types to allow easier mocking.
        constants.put("ANDROID_STILL", DetectedActivity.STILL);
        constants.put("ANDROID_WALKING", DetectedActivity.WALKING);
        constants.put("ANDROID_IN_VEHICLE", DetectedActivity.IN_VEHICLE);

        return constants;
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
    public void startMockedWithCallback(int detectionIntervalMillis, int mockActivityType, final Callback onSuccess, final Callback onError) {
        if (mActivityRecognizer == null) {
            mActivityRecognizer = new ActivityRecognizer(mReactContext);
        }

        mActivityRecognizer.startMocked((long) detectionIntervalMillis, mockActivityType);

        onSuccess.invoke();
    }

    @ReactMethod
    public void stopMockedWithCallback(final Callback onSuccess, final Callback onError) {
        if (mActivityRecognizer != null) {
            mActivityRecognizer.stopMocked();
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
