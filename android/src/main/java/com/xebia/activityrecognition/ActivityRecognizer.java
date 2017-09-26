package com.xebia.activityrecognition;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactApplicationContext;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognizer implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status> {
    protected static final String TAG = ActivityRecognizer.class.getSimpleName();
    protected ActivityDetectionBroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private ReactContext mReactContext;
    private GoogleApiClient mGoogleApiClient;
    private GoogleApiAvailability mGoogleApiAvailability;
    private boolean connected;
    private boolean started;
    private long interval;
    private Timer mockTimer;

    public ActivityRecognizer(ReactApplicationContext reactContext) {
        mGoogleApiAvailability = GoogleApiAvailability.getInstance();
        mContext = reactContext.getApplicationContext();
        mReactContext = reactContext;
        connected = false;
        started = false;

        if (checkPlayServices()) {
            mBroadcastReceiver = new ActivityDetectionBroadcastReceiver();
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        }
    }

    // Subscribe to activity updates. If not connected to Google Play Services, connect first and try again from the onConnected callback.
    public void start(long detectionIntervalMillis) {
        if (mGoogleApiClient == null) {
            throw new Error("No Google API client. Your device likely doesn't have Google Play Services.");
        }

        interval = detectionIntervalMillis;
        if (!connected) {
            mGoogleApiClient.connect();
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, new IntentFilter(DetectionService.BROADCAST_ACTION));
        } else if (!started) {
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                detectionIntervalMillis,
                getActivityDetectionPendingIntent()
            ).setResultCallback(this);
            started = true;
        }
    }

    // Subscribe to mock activity updates.
    public void startMocked(long detectionIntervalMillis, final int mockActivityType) {
        mockTimer = new Timer();
        mockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final ArrayList<DetectedActivity> detectedActivities = new ArrayList<>();
                DetectedActivity detectedActivity = new DetectedActivity(mockActivityType, 100);
                detectedActivities.add(detectedActivity);
                onUpdate(detectedActivities);
            }
        }, 0, detectionIntervalMillis);

        started = true;
    }

    // Unsubscribe from mock activity updates.
    public void stopMocked() {
        if (started) {
            mockTimer.cancel();
            started = false;
        }
    }

    // Unsubscribe from activity updates and disconnect from Google Play Services. Also called when connection failed.
    public void stop() {
        if (mGoogleApiClient == null) {
            throw new Error("No Google API client. Your device likely doesn't have Google Play Services.");
        }

        if (started) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityDetectionPendingIntent()
            ).setResultCallback(this);
            started = false;
        }
        if (connected) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
            mGoogleApiClient.disconnect();
            connected = false;
        }
    }

    // Verify Google Play Services availability
    public boolean checkPlayServices() {
        int resultCode = mGoogleApiAvailability.isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            String errorString = mGoogleApiAvailability.getErrorString(resultCode);
            if (mGoogleApiAvailability.isUserResolvableError(resultCode)) {
                Log.w(TAG, errorString);
            } else {
                Log.e(TAG, "This device is not supported. " + errorString);
            }
            return false;
        }
        return true;
    }

    // Implement GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "GoogleApiClient connected");
        connected = true;
        start(interval);
    }

    // Implement GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended, reconnecting...");
        mGoogleApiClient.connect();
    }

    // Implement GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "GoogleApiClient connection failed: " + result.getErrorCode());
        connected = false;
        stop();
    }

    // Implement ResultCallback<Status>
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.d(TAG, "Succesfully added or removed activity detection updates");
        } else {
            Log.e(TAG, "Error adding or removing activity detection updates: " + status.getStatusMessage());
        }
    }

    // Create a PendingIntent to be sent for each activity detection
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(mReactContext, DetectionService.class);
        return PendingIntent.getService(mReactContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // Create key-value map with activity recognition result
    private void onUpdate(ArrayList<DetectedActivity> detectedActivities) {
        WritableMap params = Arguments.createMap();
        for (DetectedActivity activity : detectedActivities) {
            params.putInt(DetectionService.getActivityString(activity.getType()), activity.getConfidence());
        }
        sendEvent("DetectedActivity", params);
    }

    // Send result back to JavaScript land
    private void sendEvent(String eventName, @Nullable WritableMap params) {
        try {
            mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
        } catch (RuntimeException e) {
            Log.e(TAG, "java.lang.RuntimeException: Trying to invoke JS before CatalystInstance has been set!", e);
        }
    }

    // Listen to events broadcasted by the DetectionService
    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
        protected static final String TAG = "ActivityDetectionBroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received activity update");
            ArrayList<DetectedActivity> updatedActivities = intent.getParcelableArrayListExtra(DetectionService.ACTIVITY_EXTRA);
            onUpdate(updatedActivities);
        }
    }
}
