
package com.b8ne.RNPusherPushNotifications;

import android.content.Intent;
import android.os.AsyncTask;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Callback;
import android.os.Bundle;
import org.json.JSONException;
import org.json.JSONObject;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import android.os.Build;
import android.app.Activity;

import java.util.Set;

// SEE: https://docs.pusher.com/beams/reference/android

public class RNPusherPushNotificationsModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
    private PusherWrapper pusher;

    public RNPusherPushNotificationsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    private final LifecycleEventListener lifecycleEventListener = new LifecycleEventListener() {

        @Override
        public void onHostResume() {
            pusher.onResume(getCurrentActivity());
        }

        @Override
        public void onHostDestroy() {
            pusher.onDestroy(getCurrentActivity());
        }

        @Override
        public void onHostPause() {
            pusher.onPause(getCurrentActivity());
        }
    };

    @Override
    public String getName() {
        return "RNPusherPushNotifications";
    }

    @ReactMethod
    public void setAppKey(String appKey) {
        this.pusher = new PusherWrapper(appKey, this.reactContext);
        reactContext.addLifecycleEventListener(lifecycleEventListener);
    }

    @ReactMethod
    public void clearAllState() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                pusher.clearAllState();
            }
        });
    }

    @ReactMethod
    public void subscribe(final String interest, final Callback errorCallback, final Callback successCallback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                pusher.subscribe(interest, errorCallback, successCallback);
            }
        });
    }

    @ReactMethod
    public void unsubscribe(final String interest, final Callback errorCallback, final Callback successCallback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                pusher.unsubscribe(interest, errorCallback, successCallback);
            }
        });
    }

    @ReactMethod
    public void unsubscribeAll(final Callback errorCallback, final Callback successCallback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                pusher.unsubscribeAll(errorCallback, successCallback);
            }
        });
    }

    @ReactMethod
    public void getSubscriptions( final Callback subscriptionCallback, final Callback errorCallback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                pusher.getSubscriptions(subscriptionCallback, errorCallback);
            }
        });
    }

    @ReactMethod
    public void setUserId(final String userId, final String token, final Callback errorCallback, final Callback successCallback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                pusher.setUserId(userId, token, errorCallback, successCallback);
            }
        });
    }

    @ReactMethod
    public void setOnSubscriptionsChangedListener(final Callback subscriptionChangedListener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                pusher.setOnSubscriptionsChangedListener(subscriptionChangedListener);
            }
        });
    }

    private Bundle getBundleFromIntent(Intent intent) {
        Bundle bundle = null;
        if (intent.hasExtra("notification")) {
            bundle = intent.getBundleExtra("notification");
        } else if (intent.hasExtra("google.message_id")) {
            bundle = intent.getExtras();
        }
        return bundle;
    }

    private String convertJSON(Bundle bundle) {
        try {
            JSONObject json = this.convertJSONObject(bundle);
            return json.toString();
        } catch (JSONException e) {
            return null;
        }
    }

    // a Bundle is not a map, so we have to convert it explicitly
    private JSONObject convertJSONObject(Bundle bundle) throws JSONException {
        JSONObject json = new JSONObject();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            Object value = bundle.get(key);
            if (value instanceof Bundle) {
                json.put(key, convertJSONObject((Bundle)value));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                json.put(key, JSONObject.wrap(value));
            } else {
                json.put(key, value);
            }
        }
        return json;
    }

    @ReactMethod
    public void getInitialNotification(Promise promise) {
        WritableMap params = Arguments.createMap();
        Activity activity = getCurrentActivity();
        if (activity != null) {
            Bundle bundle = this.getBundleFromIntent(activity.getIntent());
            if (bundle != null) {
                bundle.putBoolean("foreground", false);
                String bundleString = this.convertJSON(bundle);
                params.putString("dataJSON", bundleString);
            }
        }
        promise.resolve(params);
    }
}
