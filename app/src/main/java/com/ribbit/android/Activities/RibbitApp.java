package com.ribbit.android.Activities;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseObject;
import com.ribbit.android.ParseObjects.Post;

/**
 * Created by arnb on 7/13/15.
 */
public class RibbitApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(getApplicationContext(), "tClbCEFRzBTbTknAcivspQ8O1m2E9COuX1v9PHAy", "jPKzsfWYDurV8p2GPpYuwhvBPt6Nr0hejRQXoDKZ");
        ParseObject.registerSubclass(Post.class);
    }
}
