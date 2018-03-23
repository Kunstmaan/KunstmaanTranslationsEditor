package be.kunstmaan.translationseditor;

import android.app.Application;

import kunstmaan.be.kunstmaantranslationseditor.R;

public class TranslationUtils {
    public static void initTranlsationsEditor(Application application) {

        new KunstmaanTranslationUtil.Builder(application, R.string.class.getFields())
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
