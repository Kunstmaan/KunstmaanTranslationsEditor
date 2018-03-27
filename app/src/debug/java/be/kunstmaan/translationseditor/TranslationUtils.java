package be.kunstmaan.translationseditor;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kunstmaan.be.kunstmaantranslationseditor.R;

public class TranslationUtils {
    public static void initTranlsationsEditor(Application application) {

        List<Locale> localeList = new ArrayList<>();
                localeList.add(new Locale("nl"));
                localeList.add(new Locale("en", "US"));

        new KunstmaanTranslationUtil.Builder(application, R.string.class.getFields(), localeList, new Locale("en", "US"))
                .addCustomJsonFormat("{\n" +
                        "  \"myCustomNameForTheKey\": \"${key}\",\n" +
                        "  \"myCustomNameForTheNewValue\": \"${newValue}\",\n" +
                        "}")
                .addCustomXmlFormat("myNewRootTag",
                                "<myName>\n" +
                                "    <myLocaleTag>${locale}</myLocaleTag>\n" +
                                "    <myKeyTag>${key}</myKeyTag>\n" +
                                "    <myOldValueTag>${oldValue}</myOldValueTag>\n" +
                                "    <myNewValueTag>${newValue}</myNewValueTag>\n" +
                                "</myName>")
                .build();

    }

}
