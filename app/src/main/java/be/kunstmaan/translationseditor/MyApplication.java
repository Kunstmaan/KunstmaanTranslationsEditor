package be.kunstmaan.translationseditor;

import android.app.Application;

/**
 * Created by michalb on 22/03/2018.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TranslationUtils.initTranlsationsEditor(this);
    }
}
