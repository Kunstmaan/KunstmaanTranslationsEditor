package be.kunstmaan.translationseditor.views;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Field;
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

        View constraintLayout = inflater.inflate(R.layout.edit_popup_layout, null);

        TextView title = constraintLayout.findViewById(R.id.title_text_view);
        title.setText(pairToEdit.key);


        Button okButton = constraintLayout.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (TranslationPair tp : mAllVersionsOfTranslationPair){
                    if(tp.hasBeenEdited() || tp.oldValue.equals(tp.newValue)){
                        KunstmaanTranslationUtil.storeInSharedPreferences(tp);
                    }
                }
                watcher.onResult(mAllVersionsOfTranslationPair.get(0));
                mAlertDialog.dismiss();
            }
        });

        Button cancelButton = constraintLayout.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlertDialog.cancel();
            }
        });
        Spinner spinner = constraintLayout.findViewById(R.id.popup_spinner);
        final EditText editText = constraintLayout.findViewById(R.id.popup_edit_text);

        editText.setText(pairToEdit.hasBeenEdited()?
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
            locales.add(tp.locale);
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

        builder.setView(constraintLayout);
        mAlertDialog = builder.create();
        mAlertDialog.getWindow().setBackgroundDrawableResource(android.R.color.background_light);
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

