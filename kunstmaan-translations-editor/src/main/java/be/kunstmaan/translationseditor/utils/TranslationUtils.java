package be.kunstmaan.translationseditor.utils;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.SparseArray;
import android.util.TypedValue;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import be.kunstmaan.translationseditor.R;


public class TranslationUtils {

    private final Resources resources;

    public TranslationUtils(Resources resources) {
        this.resources = resources;
    }

    private TranslationLookup getTranslation(int resId){
        try {
            Method m = AssetManager.class.getDeclaredMethods()[70]; //loadResourceValue
            m.setAccessible(true);
            TypedValue outValue = new TypedValue();
            short t = (short)0;

            Object block = m.invoke( resources.getAssets(), resId, t, outValue, true);
            TranslationLookup tl = new TranslationLookup(outValue, (Boolean)block ? 1 :0);
            return tl;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
    private TranslationLookup getTranslationFromMemory(int resId) {
        try {
            Method m = AssetManager.class.getDeclaredMethod("loadResourceValue", int.class, short.class, TypedValue.class, boolean.class); //loadResourceValue //TODO: change to byName
            m.setAccessible(true);
            TypedValue outValue = new TypedValue();
            short t = (short) 0;
            Object block = m.invoke(resources.getAssets(), resId, t, outValue, true);
            Field f = FieldUtils.getField(AssetManager.class, "mStringBlocks", true);
            f.setAccessible(true);
            Object[] mStringBlocks = (Object[]) f.get(resources.getAssets());
            Field f2 = FieldUtils.getField(mStringBlocks[0].getClass(), "mSparseStrings", true);
            f2.setAccessible(true);
            SparseArray sa = (SparseArray) f2.get(mStringBlocks[(int) block]);
            outValue.string = (CharSequence) sa.get(outValue.data);
            TranslationLookup tl = new TranslationLookup(outValue, (Integer) block);
            return tl;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Return all translations that are currently loaded in memory
     *
     * @return
     */
    public List<TranslationPair> getTranslationPairsFromMemory(Field[] passedFields) {
        List<TranslationPair> result = new ArrayList<>();
        Field[] fields = passedFields;
        for (final Field field : fields) {
            String name = field.getName(); //name of string
            try {
                int id = field.getInt(R.string.class); //id of string
                TranslationLookup tl = getTranslationFromMemory(id);
                if (tl.typedValue.string != null) {
                    TranslationPair tp = new TranslationPair();
                    tp.resourceId = id;
                    tp.value = tl.typedValue.string.toString();
                    tp.key = name;
                    result.add(tp);
                }
            } catch (Exception ex) {}
        }
        return result;
    }

    public List<TranslationPair> getTranslationPairs(Field[] passedFields) {
        List<TranslationPair> result = new ArrayList<>();
        Field[] fields = passedFields;
        for (final Field field : fields) {
            String name = field.getName(); //name of string
            try {
                int id = field.getInt(R.string.class); //id of string
                TranslationLookup tl = getTranslation(id);
                if (tl.typedValue.string != null) {
                    TranslationPair tp = new TranslationPair();
                    tp.resourceId = id;
                    tp.value = tl.typedValue.string.toString();
                    tp.key = name;
                    result.add(tp);
                }
            } catch (Exception ex) {}
        }
        return result;
    }

    public void setTranslation(int resId, String newValue) {
        try {
            TranslationLookup tl = getTranslationFromMemory(resId);
            Field f = FieldUtils.getField(AssetManager.class, "mStringBlocks", true);
            f.setAccessible(true);
            Object[] mStringBlocks = (Object[]) f.get(resources.getAssets());
            Field f2 = FieldUtils.getField(mStringBlocks[0].getClass(), "mSparseStrings", true);
            f2.setAccessible(true);
            SparseArray sa = (SparseArray) f2.get(mStringBlocks[tl.block]);
            sa.put(tl.typedValue.data, newValue);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }



    public static final class TranslationLookup {
        public TypedValue typedValue;
        public int block;

        public TranslationLookup(TypedValue outValue, int block) {
            this.typedValue = outValue;
            this.block = block;
        }
    }

    public static final class TranslationPair {
        public int resourceId;
        public String key;
        public String value;
    }
}
