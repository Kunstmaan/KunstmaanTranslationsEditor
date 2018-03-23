package be.kunstmaan.translationseditor;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import be.kunstmaan.translationseditor.utils.TranslationPair;
import be.kunstmaan.translationseditor.utils.TranslationUtils;
import be.kunstmaan.translationseditor.views.TranslationsActivity;


public class KunstmaanTranslationUtil {

    public static List<Locale> sLocalesToConsider;
    private static final String STORED_VALUES = KunstmaanTranslationUtil.class.getName() + ".stored.values";
    private static final String SHARED_PREFS = KunstmaanTranslationUtil.class.getName() + ".shared.prefs";
    private static ArrayList<TranslationPair> sValuesFromCurrentScreen;
    private static WeakReference<Activity> sActivity;
    private static Field[] sStringFields;

    public static String sCustomJsonFormat;
    public static String sCustomXmlFormat;
    public static String sCustomXmlRootTag;


    public static void showTranslationsWindow() {
        saveStringsFromCurrentScreen();
        sActivity.get().startActivity(new Intent(sActivity.get(), TranslationsActivity.class));
    }


    public static void applyChanges(){
        checkForPresentChangedValuesToApply(sActivity.get());
        endActivity();
    }

    public static ArrayList<TranslationPair> getValuesFromMemory(){
        ArrayList<TranslationPair> textsFoundInMemory = new ArrayList<>();
        TranslationUtils translationUtils = new TranslationUtils(sActivity.get().getResources());

        for (TranslationUtils.TranslationPair tp : translationUtils.getTranslationPairsFromMemory(sStringFields)) {
            textsFoundInMemory.add(new TranslationPair(tp.key, tp.value, tp.resourceId, sActivity.get().getResources().getConfiguration().locale.toString()));
        }
        return mergeValuesWithSharedPreferences(textsFoundInMemory);
    }

     public static ArrayList<TranslationPair> getValuesFromCurrentScreen(){
        ArrayList<TranslationPair> copy = deepCopyValues(sValuesFromCurrentScreen);
        return mergeValuesWithSharedPreferences(copy);

    }

     public static ArrayList<TranslationPair> getAllValues(){
        ArrayList<TranslationPair> allTextsFound = new ArrayList<>();
        TranslationUtils translationUtils = new TranslationUtils(sActivity.get().getApplication().getResources());
        String currentResourcesFileName = sActivity.get().getResources().getConfiguration().locale.toString();
        for (TranslationUtils.TranslationPair tp : translationUtils.getTranslationPairs(sStringFields)) {
            allTextsFound.add(new TranslationPair(tp.key, tp.value, tp.resourceId, currentResourcesFileName));
        }
        return mergeValuesWithSharedPreferences(allTextsFound);
    }

    public static ArrayList<TranslationPair> getAllLocalized(Locale locale){
        ArrayList<TranslationPair> allTextsFound = new ArrayList<>();
        TranslationUtils translationUtils = new TranslationUtils(getLocalizedResources(sActivity.get().getApplication(), locale));
        String currentResourcesFileName = sActivity.get().getResources().getConfiguration().locale.toString();
        for (TranslationUtils.TranslationPair tp : translationUtils.getTranslationPairs(sStringFields)) {
            allTextsFound.add(new TranslationPair(tp.key, tp.value, tp.resourceId, currentResourcesFileName));
        }
        return mergeValuesWithSharedPreferences(allTextsFound);
    }

    public static ArrayList<TranslationPair> getAllModifiedLocalized(Locale locale){
        SharedPreferences sharedPref = sActivity.get().getApplicationContext().getSharedPreferences(KunstmaanTranslationUtil.SHARED_PREFS, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String storedValues = sharedPref.getString(KunstmaanTranslationUtil.STORED_VALUES + locale.toString(), null);
        return gson.fromJson(storedValues, new TypeToken<ArrayList<TranslationPair>>(){}.getType());
    }

     public static ArrayList<TranslationPair> getTranslationsFor(TranslationPair translationPair) {
        ArrayList<TranslationPair> translationPairs = new ArrayList<>();
        translationPairs.add(translationPair);
        for(Locale l : sLocalesToConsider){
            Resources resources = getLocalizedResources(sActivity.get().getApplication(), l);
            TranslationUtils translationUtils = new TranslationUtils(resources);
            for (TranslationUtils.TranslationPair tp : translationUtils.getTranslationPairs(sStringFields)) {
                if(! l.toString().equals(translationPair.locale) && tp.key.equals(translationPair.key)){
                    translationPairs.add(new TranslationPair(tp.key, tp.value, tp.resourceId, l.toString()));
                }
            }
        }
        return mergeValuesWithSharedPreferences(translationPairs);
    }

     public static void clearData() {
        SharedPreferences sharedPref = sActivity.get().getApplicationContext().getSharedPreferences(KunstmaanTranslationUtil.SHARED_PREFS, Context.MODE_PRIVATE);
         TranslationUtils translationUtils = new TranslationUtils(sActivity.get().getResources());
         for(Locale l : sLocalesToConsider) {
            String storedValues = sharedPref.getString(STORED_VALUES + l.toString(), null);
            if (storedValues != null) {
                Gson gson = new Gson();
                ArrayList<TranslationPair> editedValues = gson.fromJson(storedValues, new TypeToken<ArrayList<TranslationPair>>() {
                }.getType());

                for(TranslationPair tp :editedValues){
                    translationUtils.setTranslation(tp.id, tp.oldValue);
                }
                editedValues.clear();
                String json = gson.toJson(editedValues);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(KunstmaanTranslationUtil.STORED_VALUES + l.toString(), json);
                editor.apply();
            }
        }
        endActivity();
    }


    public static void storeInSharedPreferences(TranslationPair translationPair){
        SharedPreferences sharedPref = sActivity.get().getSharedPreferences(KunstmaanTranslationUtil.SHARED_PREFS, Context.MODE_PRIVATE);
        String storedValues = sharedPref.getString(KunstmaanTranslationUtil.STORED_VALUES + translationPair.locale, null);
        if(storedValues != null){
            Gson gson = new Gson();
            ArrayList<TranslationPair> editedValues = gson.fromJson(storedValues ,new TypeToken<ArrayList<TranslationPair>>(){}.getType());
            if(translationPair.hasBeenEdited()){
                if(editedValues.contains(translationPair)){
                    editedValues.set(editedValues.indexOf(translationPair), translationPair);
                }else{
                    editedValues.add(translationPair);
                }
            }else if(editedValues.contains(translationPair)) {
                editedValues.remove(editedValues.indexOf(translationPair));

            }
            String json = gson.toJson(editedValues);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(KunstmaanTranslationUtil.STORED_VALUES + translationPair.locale, json);
            editor.apply();
        }
    }

    private static void endActivity(){
        Intent i = sActivity.get().getBaseContext().getPackageManager().
                getLaunchIntentForPackage(sActivity.get().getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sActivity.get().startActivity(i);
    }

    private static ArrayList<TranslationPair> mergeValuesWithSharedPreferences(ArrayList<TranslationPair> translationPairs) {
        ArrayList<TranslationPair> mergedValues = new ArrayList<>();
        SharedPreferences sharedPref = sActivity.get().getApplicationContext().getSharedPreferences(KunstmaanTranslationUtil.SHARED_PREFS, Context.MODE_PRIVATE);
        for(TranslationPair tp : translationPairs){
            String translationPairsFromSharedPrefs = sharedPref.getString(STORED_VALUES + tp.locale, null);
            if(translationPairsFromSharedPrefs != null){
                Gson gson = new Gson();
                ArrayList<TranslationPair> editedValues = gson.fromJson(translationPairsFromSharedPrefs ,new TypeToken<ArrayList<TranslationPair>>(){}.getType());
                if(editedValues.contains(tp)){
                    mergedValues.add(editedValues.get(editedValues.indexOf(tp)));
                }else{
                    mergedValues.add(tp);
                }
            }else{
            }
        }
        return mergedValues;

    }

    private static void saveStringsFromCurrentScreen(){
        sValuesFromCurrentScreen = new ArrayList<>();
        TranslationUtils translationUtils = new TranslationUtils(sActivity.get().getResources());
        final ViewGroup rootLayout = (ViewGroup) ((ViewGroup) sActivity.get().findViewById(android.R.id.content)).getChildAt(0);
        String currentResourcesFileName = sActivity.get().getResources().getConfiguration().locale.toString();
        for (TranslationUtils.TranslationPair tp : translationUtils.getTranslationPairsFromMemory(sStringFields)) {
            ArrayList<View> foundViews = new ArrayList<>();
            rootLayout.findViewsWithText(foundViews, tp.value, View.FIND_VIEWS_WITH_TEXT);
            if(foundViews.size()>0){
                sValuesFromCurrentScreen.add(new TranslationPair(tp.key, tp.value, tp.resourceId, currentResourcesFileName));
            }

        }
    }

    private static ArrayList<TranslationPair> deepCopyValues(ArrayList<TranslationPair> values){
        ArrayList<TranslationPair> copy = new ArrayList<>(values.size());
        for(TranslationPair tp  : values){
            copy.add(new TranslationPair(tp));
        }
        return copy;
    }

    private static void checkForPresentChangedValuesToApply(Activity activity) {
        SharedPreferences sharedPref = activity.getApplicationContext().getSharedPreferences(KunstmaanTranslationUtil.SHARED_PREFS, Context.MODE_PRIVATE);
        String storedValues = sharedPref.getString(STORED_VALUES + sActivity.get().getResources().getConfiguration().locale.toString(), null);
        if(storedValues != null){
            Gson gson = new Gson();
            ArrayList<TranslationPair> editedValues = gson.fromJson(storedValues ,new TypeToken<ArrayList<TranslationPair>>(){}.getType());
            TranslationUtils translationUtils = new TranslationUtils(activity.getResources());
            for(TranslationPair tp : editedValues){
                translationUtils.setTranslation(tp.id, tp.newValue);
            }

            String json = gson.toJson(editedValues);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(KunstmaanTranslationUtil.STORED_VALUES + sActivity.get().getResources().getConfiguration().locale.toString(), json);
            editor.apply();

        }
    }

    private static Resources getLocalizedResources(Context context, Locale desiredLocale) {
        Configuration conf = context.getResources().getConfiguration();
        conf = new Configuration(conf);
        conf.setLocale(desiredLocale);
        Context localizedContext = context.createConfigurationContext(conf);
        return localizedContext.getResources();
    }

    private static void setupActivityGrabber(Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                sActivity = new WeakReference<>(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                sActivity.clear();
            }

            @Override
            public void onActivityStopped(Activity activity) {}
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}
        });
    }

    public static class Builder{
        private final Application mApplication;
        private Field[] mStringFields;
        private List<Locale> mLocalesToConsider;
        private String mCustomJsonFormat = null;
        private String mCustomXmlFormat = null;
        private String mCustomXmlRootTag = null;



        public Builder(Application application, Field[] stringFields){
            mLocalesToConsider = new ArrayList<>();
            mLocalesToConsider.add(Locale.getDefault());
            this.mApplication = application;
            this.mStringFields = stringFields;
            addIgnorePattern(Pattern.compile("^kunstmaan_translations_.*$"));
        }

        public Builder addLocales(List<Locale> localesToConsider){
            this.mLocalesToConsider.addAll(localesToConsider);
            return this;
        }

        public Builder addIgnorePattern(Pattern patternToBeIngored){
            Field[] result = new Field[0];
            for(Field f : mStringFields){
                if(! patternToBeIngored.matcher(f.getName()).matches()){
                    result = ArrayUtils.add(result, f);
                }
            }
            this.mStringFields = result;
            return this;
        }

        public Builder addCustomJsonFormat(String customJsonFormat){
            this.mCustomJsonFormat = customJsonFormat;
            return this;
        }

        public Builder addCustomXmlFormat(String rootTag, String customXmlFormat){
            this.mCustomXmlFormat = customXmlFormat;
            this.mCustomXmlRootTag = rootTag;
            return this;
        }

        public void build(){
            KunstmaanTranslationUtil.sLocalesToConsider = mLocalesToConsider;
            KunstmaanTranslationUtil.sStringFields = removeFieldsByName(this.mStringFields, android.support.v7.appcompat.R.string.class.getFields());
            setupActivityGrabber(mApplication);

            SharedPreferences sharedPref = mApplication.getSharedPreferences(KunstmaanTranslationUtil.SHARED_PREFS, Context.MODE_PRIVATE);
            for(Locale l : mLocalesToConsider){
                String storedValues = sharedPref.getString(KunstmaanTranslationUtil.STORED_VALUES + l.toString(), null);
                if(storedValues == null){
                    Gson gson = new Gson();
                    ArrayList<TranslationPair> editedValues = new ArrayList<>();
                    editedValues.clear();
                    String json = gson.toJson(editedValues);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(KunstmaanTranslationUtil.STORED_VALUES+ l.toString(), json);
                    editor.apply();
                }
            }
            KunstmaanTranslationUtil.sCustomJsonFormat = this.mCustomJsonFormat;
            KunstmaanTranslationUtil.sCustomXmlFormat = this.mCustomXmlFormat;
            KunstmaanTranslationUtil.sCustomXmlRootTag = this.mCustomXmlRootTag;
        }

        private Field[] removeFieldsByName(Field[] main, Field[] toRemove){
            List<String> fieldNamesToRemove = new LinkedList<>();
            for(Field f : toRemove){
                fieldNamesToRemove.add(f.getName());
            }
            Field[] result = new Field[0];
            for(Field f : main){
                if(!fieldNamesToRemove.contains(f.getName())){
                    result = ArrayUtils.add(result, f);
                }
            }
            return result;
        }
    }

}
