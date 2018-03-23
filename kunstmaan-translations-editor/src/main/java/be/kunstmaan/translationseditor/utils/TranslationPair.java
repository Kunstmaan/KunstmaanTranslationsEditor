package be.kunstmaan.translationseditor.utils;

import java.io.Serializable;

public class TranslationPair implements Serializable {
    public final int id;
    public final String key;
    public final String oldValue;
    public String newValue;
    public final String locale;

    public TranslationPair(String key, String oldValue, int id, String locale) {
        this.locale = locale;
        this.key = key;
        this.oldValue = oldValue;
        this.id = id;

    }

    public TranslationPair( TranslationPair translationPair){
        this.locale = translationPair.locale;
        this.key = translationPair.key;
        this.oldValue = translationPair.oldValue;
        this.newValue = translationPair.newValue;
        this.id = translationPair.id;
    }

    public boolean hasBeenEdited(){
        return newValue != null && ! oldValue.equals(newValue);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o){
            return true;
        }
        if(o == null){
            return false;
        }
        if(getClass() != o.getClass()){
            return false;
        }
        TranslationPair translationPair = (TranslationPair) o;
        return this.key.equals(translationPair.key) && this.locale.equals(translationPair.locale);
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (locale != null ? locale.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TranslationPair{" +
                "key=" + key +
                ", oldValue=" + oldValue +
                ", newValue=" + newValue +
                ", hasBeenEdited=" + hasBeenEdited() +
                ", id=" + id +
                ", locale=" + locale +
                '}';
    }
}