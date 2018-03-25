package be.kunstmaan.translationseditor.adapters;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;


import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import be.kunstmaan.translationseditor.R;
import be.kunstmaan.translationseditor.utils.TranslationPair;
import be.kunstmaan.translationseditor.views.EditDialog;


public class TranslationsFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable, EditDialog.ResultWatcher {

    private final ArrayList<TranslationPair> mValues;
    private ArrayList<TranslationPair> mValuesFiltered;
    private final Activity mActivity;

    public TranslationsFragmentAdapter(ArrayList<TranslationPair> values, Activity currentActivity) {
        this.mValues = values;
        this.mValuesFiltered = values;
        this.mActivity = currentActivity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_translation, parent, false);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        return new SimpleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TranslationPair currentPair = mValuesFiltered.get(position);
        SimpleViewHolder simpleViewHolder = (SimpleViewHolder) holder;
        simpleViewHolder.bind(currentPair);
    }

    @Override
    public int getItemCount() {
        return mValuesFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    mValuesFiltered = mValues;
                } else {
                    ArrayList<TranslationPair> listValuesFiltered = new ArrayList<>();
                    for(TranslationPair value : mValues){
                        if(StringUtils.contains(value.key.toLowerCase(), charString.toLowerCase())
                                || StringUtils.contains(value.oldValue.toLowerCase(), charString.toLowerCase())){
                            listValuesFiltered.add(value);
                        }
                    }
                    mValuesFiltered = listValuesFiltered;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mValuesFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    mValuesFiltered = (ArrayList<TranslationPair>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void hideEdited(){
        ArrayList<TranslationPair> listValuesFiltered = new ArrayList<>();
        for(TranslationPair tp : mValuesFiltered){
            if(!tp.hasBeenEdited()){
                listValuesFiltered.add(tp);
            }
        }
        mValuesFiltered = listValuesFiltered;
        notifyDataSetChanged();
    }

    public void showEdited(){
        for(TranslationPair tp : mValues){
            if(tp.hasBeenEdited()){
                mValuesFiltered.add(tp);
            }
        }
        notifyDataSetChanged();
    }

    public void hideUnedited(){
        ArrayList<TranslationPair> listValuesFiltered = new ArrayList<>();
        for(TranslationPair tp : mValuesFiltered){
            if(tp.hasBeenEdited()){
                listValuesFiltered.add(tp);
            }
        }
        mValuesFiltered = listValuesFiltered;
        notifyDataSetChanged();
    }

    public void showUnedited(){
        for(TranslationPair tp : mValues){
            if(! tp.hasBeenEdited()){
                mValuesFiltered.add(tp);
            }
        }
        notifyDataSetChanged();
    }

    private void manageClickOnItem(int itemPosition) {
        EditDialog editDialog = new EditDialog(mActivity,this ,mValuesFiltered.get(itemPosition));
        editDialog.show();
    }

    @Override
    public void onResult(TranslationPair translationPair) {
        int i = mValuesFiltered.indexOf(translationPair);
        if(i >= 0){
            mValuesFiltered.set(i, translationPair);
            notifyItemRangeChanged(i, 1);
        }
    }

    class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ConstraintLayout mConstraintLayout;
        private final TextView mKey;
        private final TextView mValue;
        private final ImageView mEditImage;

        SimpleViewHolder(View view) {
            super(view);
            mKey = view.findViewById(R.id.key);
            mKey.setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            mKey.setOnClickListener(this);
            mValue = view.findViewById(R.id.oldValue);
            mValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            mValue.setOnClickListener(this);
            mEditImage = view.findViewById(R.id.edit_image_view);
            mEditImage.setOnClickListener(this);
            mConstraintLayout = (ConstraintLayout) view;
            mConstraintLayout.setOnClickListener(this);
        }

        void bind(TranslationPair translationPair) {
            this.mKey.setText(translationPair.key);
            this.mValue.setText(translationPair.oldValue);
            /*
            if(translationPair.hasBeenEdited()){
                TypedValue typedValue = new  TypedValue();
                mActivity.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
                final  int color = typedValue.data;
                mConstraintLayout.setBackgroundColor(color);
            }else {
                mConstraintLayout.setBackgroundColor(mActivity.getResources().getColor(R.color.colorNonEdited));
            }
            */
            if(translationPair.hasBeenEdited()){
                /*
                mEditedValue.setVisibility(EditText.VISIBLE);
                mOldPrefix.setVisibility(EditText.VISIBLE);
                mNewPrefix.setVisibility(EditText.VISIBLE);
                mEditedValue.setText(translationPair.newValue);
                mValue.setText(mValue.getText());
                */
                mValue.setText(translationPair.newValue);
                mValue.setTypeface(null, Typeface.BOLD);
            }else{
                /*
                mEditedValue.setVisibility(EditText.GONE);
                mOldPrefix.setVisibility(EditText.GONE);
                mNewPrefix.setVisibility(EditText.GONE);
                */
                mValue.setText(translationPair.oldValue);
                mValue.setTypeface(null, Typeface.NORMAL);
            }
        }

        @Override
        public void onClick(View view) {
            manageClickOnItem(getAdapterPosition());
        }
    }

}
