package be.kunstmaan.translationseditor.views;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import be.kunstmaan.translationseditor.KunstmaanTranslationUtil;
import be.kunstmaan.translationseditor.R;
import be.kunstmaan.translationseditor.utils.TranslationPair;


public class EditDialog {
    private final ArrayList<TranslationPair> mAllVersionsOfTranslationPair;
    private final AlertDialog mAlertDialog;
    private TranslationPair mCurrentPair;


    public EditDialog(Activity activity, final ResultWatcher watcher , TranslationPair pairToEdit) {
        mAllVersionsOfTranslationPair = KunstmaanTranslationUtil.getTranslationsFor(pairToEdit);
        mCurrentPair = pairToEdit;


        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setTitle(pairToEdit.key);

        View constraintLayout = inflater.inflate(R.layout.edit_popup_layout, null);
        Spinner spinner = constraintLayout.findViewById(R.id.popup_spinner);
        final EditText editText = constraintLayout.findViewById(R.id.popup_edit_text);

        editText.setText(pairToEdit.newValue != null?
                pairToEdit.newValue :
                pairToEdit.oldValue);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mCurrentPair.newValue = String.valueOf(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        List<String> locales = new ArrayList<>();
        for (TranslationPair tp : mAllVersionsOfTranslationPair){
            locales.add(tp.locale.toString());
        }

        CustomSpinnerAdapter customSpinnerAdapter = new CustomSpinnerAdapter(activity, R.layout.spinner_row, locales);
        spinner.setAdapter(customSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mCurrentPair = mAllVersionsOfTranslationPair.get(i);
                editText.setText(mCurrentPair.newValue != null ? mCurrentPair.newValue : mCurrentPair.oldValue);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        TextView originalTextView = constraintLayout.findViewById(R.id.original_text);
        originalTextView.setText(mCurrentPair.oldValue);

        builder.setView(constraintLayout)
               .setPositiveButton(R.string.kunstmaan_translations_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        for (TranslationPair tp : mAllVersionsOfTranslationPair){
                            if(tp.hasBeenEdited() || tp.oldValue.equals(tp.newValue)){
                                KunstmaanTranslationUtil.storeInSharedPreferences(tp);
                            }
                        }
                        watcher.onResult(mAllVersionsOfTranslationPair.get(0));

                    }
                })
               .setNegativeButton(R.string.kunstmaan_translations_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                   }
                });
        mAlertDialog = builder.create();
    }

    public void show(){
        mAlertDialog.show();
    }


    public interface ResultWatcher{
        void onResult(TranslationPair translationPairs);
    }


    private static class CustomSpinnerAdapter extends ArrayAdapter<String> {
        LayoutInflater inflater;
        List<String> spinnerItems;

        public CustomSpinnerAdapter(Context applicationContext, int resource, List<String> spinnerItems) {
            super(applicationContext, resource, spinnerItems);
            this.spinnerItems = spinnerItems;
            inflater = (LayoutInflater.from(applicationContext));
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = inflater.inflate(R.layout.custom_spinner_item, null);
            TextView type = (TextView) view.findViewById(R.id.spinner_item_text);
            type.setText(spinnerItems.get(i));
            return view;
        }
    }
}
