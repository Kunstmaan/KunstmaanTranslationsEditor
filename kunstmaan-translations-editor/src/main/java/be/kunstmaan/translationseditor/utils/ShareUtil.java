package be.kunstmaan.translationseditor.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import be.kunstmaan.translationseditor.BuildConfig;
import be.kunstmaan.translationseditor.KunstmaanTranslationUtil;

public class ShareUtil {

    public static final int SHARE_REQUEST = 1;
    private static final String DIR_NAME = "kunstmaanTranslationsEditor";

    private static final String defaultJsonFormat = "  {\n" +
            "    \"key\": \"${key}\",\n" +
            "    \"locale\": \"${locale}\",\n" +
            "    \"newValue\": \"${newValue}\",\n" +
            "    \"oldValue\": \"${oldValue}\"\n" +
            "  }\n";

    private static final String defaultXmlFormat =
            "  <translation>\n" +
            "    <locale>${locale}</locale>\n" +
            "    <key>${key}</key>\n" +
            "    <oldValue>${oldValue}</oldValue>\n" +
            "    <newValue>${newValue}</newValue>\n" +
            "  </translation>\n";


    private static final String defaultXmlRootTag = "Translations";



    private static final String keyPlaceHolder = "${key}";
    private static final String localePlaceHolder = "${locale}";
    private static final String newValuePlaceHolder = "${newValue}";
    private static final String oldValuePlaceHolder = "${oldValue}";



    public static void cleanDir(Context context) {
        File dir = new File(context.getCacheDir(), DIR_NAME);
        dir.mkdir();
        File[] files = dir.listFiles();

        for (File file : files) {
            file.delete();
        }

    }

    private static String createFormattedOutput(String pattern, TranslationPair tp) {
        //values
        if(pattern.contains(keyPlaceHolder)){
            pattern = pattern.replace(keyPlaceHolder, tp.key);
        }
        if(pattern.contains(localePlaceHolder)){
            pattern = pattern.replace(localePlaceHolder, tp.locale);
        }
        if(pattern.contains(newValuePlaceHolder)){
            pattern = pattern.replace(newValuePlaceHolder, tp.newValue);
        }
        if(pattern.contains(oldValuePlaceHolder)){
            pattern = pattern.replace(oldValuePlaceHolder, tp.oldValue);
        }

        return pattern;
    }

    public static void shareJson(Activity activity, Boolean includeUnchangedValues){
        ArrayList<Uri> uris = new ArrayList<>();
        File myDir = new File(activity.getCacheDir(), DIR_NAME);
        myDir.mkdir();
        for(Locale l : KunstmaanTranslationUtil.sLocalesToConsider){
            try {
                String fileName = l.equals(KunstmaanTranslationUtil.sDefaultLocale) ? BuildConfig.APPLICATION_ID : l + "_" + BuildConfig.APPLICATION_ID;
                File tempFile = File.createTempFile(fileName , ".json", myDir);
                FileWriter fw = new FileWriter(tempFile);

                ArrayList<TranslationPair> values = null;
                if(includeUnchangedValues){
                    values = KunstmaanTranslationUtil.getAllLocalized(l);
                }else {
                    values = KunstmaanTranslationUtil.getAllModifiedLocalized(l);
                }
                fw.write("[\n");
                String jsonFormat = KunstmaanTranslationUtil.sCustomJsonFormat == null ? defaultJsonFormat : KunstmaanTranslationUtil.sCustomJsonFormat;
                for(TranslationPair tp : values){
                    String readyToPrint = createFormattedOutput(jsonFormat, tp);
                    fw.write(readyToPrint + ",");
                }
                fw.write("]");

                fw.flush();
                fw.close();
                uris.add(TranslationsFileProvider.getUriForFile(activity, TranslationsFileProvider.AUTHORITY, tempFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("text/plain");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        activity.startActivityForResult(Intent.createChooser(intent, "Send Email"), SHARE_REQUEST);
    }

    public static void shareXml(Activity activity, Boolean includeUnchangedValues){
        ArrayList<Uri> uris = new ArrayList<>();
        File myDir = new File(activity.getCacheDir(), DIR_NAME);
        myDir.mkdir();
        try {
            for(Locale l : KunstmaanTranslationUtil.sLocalesToConsider) {
                ArrayList<TranslationPair> values  = null;
                if(includeUnchangedValues){
                    values = KunstmaanTranslationUtil.getAllLocalized(l);
                }else {
                    values = KunstmaanTranslationUtil.getAllModifiedLocalized(l);
                }

                File tempFile = File.createTempFile(l + "_" + BuildConfig.APPLICATION_ID, ".xml", myDir);
                FileWriter fw = new FileWriter(tempFile);
                fw.write("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>\n");
                String xmlRootTag = KunstmaanTranslationUtil.sCustomXmlRootTag == null ? defaultXmlRootTag : KunstmaanTranslationUtil.sCustomXmlRootTag;
                fw.write("<" + xmlRootTag + ">\n");
                String xmlFormat = KunstmaanTranslationUtil.sCustomXmlFormat == null ? defaultXmlFormat : KunstmaanTranslationUtil.sCustomXmlFormat;
                for(TranslationPair tp : values){
                    fw.write(createFormattedOutput(xmlFormat, tp));
                }
                fw.write("\n</" + xmlRootTag + ">");
                fw.flush();
                fw.close();
                uris.add(TranslationsFileProvider.getUriForFile(activity, TranslationsFileProvider.AUTHORITY, tempFile));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("text/plain");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        activity.startActivityForResult(Intent.createChooser(intent, "Send Email"), SHARE_REQUEST);
    }


}
