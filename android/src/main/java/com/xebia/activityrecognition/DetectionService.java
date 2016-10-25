package com.xebia.activityrecognition;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

public class DetectionService extends IntentService {
    protected static final String TAG = DetectionService.class.getSimpleName();
    protected static final String PACKAGE_NAME = DetectionService.class.getPackage().getName();
    protected static final String ACTIVITY_EXTRA = PACKAGE_NAME + ".ACTIVITY_EXTRA";
    protected static final String BROADCAST_ACTION = PACKAGE_NAME + ".BROADCAST_ACTION";

    public DetectionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        Log.d(TAG, "Detected activities:");
        for (DetectedActivity da: detectedActivities) {
            Log.d(TAG, getActivityString(da.getType()) + " (" + da.getConfidence() + "%)");
        }

        Intent localIntent = new Intent(BROADCAST_ACTION);
        localIntent.putExtra(ACTIVITY_EXTRA, detectedActivities);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    public static String getActivityString(int detectedActivityType) {
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return "IN_VEHICLE";
            case DetectedActivity.ON_BICYCLE:
                return "ON_BICYCLE";
            case DetectedActivity.ON_FOOT:
                return "ON_FOOT";
            case DetectedActivity.RUNNING:
                return "RUNNING";
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.TILTING:
                return "TILTING";
            case DetectedActivity.UNKNOWN:
                return "UNKNOWN";
            case DetectedActivity.WALKING:
                return "WALKING";
            default:
                return "UNIDENTIFIABLE";
        }
    }
}
