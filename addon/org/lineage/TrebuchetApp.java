package org.lineage;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.launcher3.BuildConfig;
import com.android.quickstep.RecentsActivity;
import com.android.systemui.shared.system.QuickStepContract;

import java.util.HashSet;

public class TrebuchetApp extends Application {
    private final boolean compatible = Build.VERSION.SDK_INT >= BuildConfig.QUICKSTEP_MIN_SDK
            && Build.VERSION.SDK_INT <= BuildConfig.QUICKSTEP_MAX_SDK;
    private final boolean isRecentComponent = checkRecentsComponent();
    private final boolean resentsEnabled = compatible && isRecentComponent;


    private static final String TAG = "TrebuchetApp";

    private static TrebuchetApp INSTANCE = null;

    public static TrebuchetApp getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TrebuchetApp ();
        }
        return INSTANCE;
    }
    public static boolean isRecentsEnabled() {
        TrebuchetApp instance = getInstance ();
        return instance != null && instance.resentsEnabled;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        QuickStepContract.sRecentsDisabled = !resentsEnabled;
        onLauncherAppStateCreated();
    }

    public void onLauncherAppStateCreated() {
        registerActivityLifecycleCallbacks(activityHandler);
    }


    private final ActivityLifecycleCallbacks activityHandler = new ActivityLifecycleCallbacks() {
        private final HashSet<Activity> activities = new HashSet<>();
        private Activity foregroundActivity = null;

        @Override
        public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
            activities.add(activity);
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            foregroundActivity = activity;
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            if (activity == foregroundActivity) foregroundActivity = null;
            activities.remove(activity);
        }
    };

    public boolean checkRecentsComponent(){
        int resId = Resources.getSystem().getIdentifier ("config_recentsComponentName", "string", "android");
        if (resId == 0) {
            Log.d(TAG, "config_recentsComponentName not found, disabling recents");
            return false;
        }
        ComponentName recentsComponent = ComponentName.unflattenFromString(Resources.getSystem().getString(resId));
        if (recentsComponent == null) {
            Log.d(TAG, "config_recentsComponentName is empty, disabling recents");
            return false;
        }

        return recentsComponent.getPackageName().equals (BuildConfig.APPLICATION_ID)
                && recentsComponent.getClassName().equals (RecentsActivity.class.getName ());
    }
}