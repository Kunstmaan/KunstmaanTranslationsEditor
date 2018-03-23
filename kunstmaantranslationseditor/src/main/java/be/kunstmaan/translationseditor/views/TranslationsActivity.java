package be.kunstmaan.translationseditor.views;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.SearchView;

import be.kunstmaan.translationseditor.KunstmaanTranslationUtil;
import be.kunstmaan.translationseditor.R;
import be.kunstmaan.translationseditor.adapters.SmartFragmentStatePagerAdapter;
import be.kunstmaan.translationseditor.adapters.ViewPagerAdapter;
import be.kunstmaan.translationseditor.utils.ShareUtil;


public class TranslationsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ViewPager mViewPager;
    private Toolbar mToolbar;
    private BottomNavigationView mNavigationView;
    private SearchView mSearchView;
    private Button mApplyButton;
    private SmartFragmentStatePagerAdapter mViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translations);

        mToolbar =findViewById(R.id.my_toolbar);
        if(getSupportActionBar() == null){
            setSupportActionBar(mToolbar);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TranslationsActivity.super.finish();
                }
            });
        }else{
            mToolbar.setVisibility(Toolbar.GONE);
        }


        getSupportActionBar().setTitle(getString(R.string.kunstmaan_translations_cancel_current_locale)+ " " + getResources().getConfiguration().locale.toString());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mViewPager = findViewById(R.id.navigation_view_pager);
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                mNavigationView.getMenu().getItem(position).setChecked(true);
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        mNavigationView = findViewById(R.id.navigation_view);
        mNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                String itemTitle = String.valueOf(item.getTitle());
                if(itemTitle.equals(getResources().getString(R.string.kunstmaan_translations_current))){
                    mViewPager.setCurrentItem(0);
                    return true;
                }else if (itemTitle.equals(getResources().getString(R.string.kunstmaan_translations_in_memory))){
                    mViewPager.setCurrentItem(1);
                    return true;
                }else if (itemTitle.equals(getResources().getString(R.string.kunstmaan_translations_all))){
                    mViewPager.setCurrentItem(2);
                    return true;
                }
                return false;
            }
        });

        mApplyButton = findViewById(R.id.apply_button);
        mApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TranslationsActivity.this);
                builder.setTitle(R.string.kunstmaan_translations_confirmation_message);
                builder.setPositiveButton(R.string.kunstmaan_translations_ok_message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        KunstmaanTranslationUtil.applyChanges();
                    }
                })
                        .setNegativeButton(R.string.kunstmaan_translations_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        final View rootView = findViewById(R.id.main_layout);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
                float dpToPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, metrics);
                if (heightDiff > dpToPx) { // if more than 200 dp, it's probably a keyboard...
                    if(mApplyButton.getVisibility() == Button.VISIBLE){
                        mApplyButton.setVisibility(Button.GONE);
                    }
                }else{
                    if(mApplyButton.getVisibility() == Button.GONE){
                        mApplyButton.setVisibility(Button.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService( Context.SEARCH_SERVICE );
        mSearchView = (SearchView) menu.findItem(R.id.menu_item_search).getActionView();
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setSubmitButtonEnabled(true);

        MenuItem showUnchanged = menu.findItem(R.id.action_show_unchanged);
        showUnchanged.setChecked(true);
        MenuItem showChanged = menu.findItem(R.id.action_show_changed);
        showChanged.setChecked(true);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        TranslationsListFragment translationsListFragment = (TranslationsListFragment) mViewPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
        translationsListFragment.filterContent(s);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        TranslationsListFragment translationsListFragment = (TranslationsListFragment) mViewPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
        translationsListFragment.filterContent(s);
        return false;
    }

    @Override
    public void onBackPressed() {
        if (!mSearchView.isIconified()){
            mSearchView.setIconified(true);
            mSearchView.onActionViewCollapsed();
            mSearchView.clearFocus();
        }else{
            super.onBackPressed();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reset){
            KunstmaanTranslationUtil.clearData();
            this.finish();
        }else if (id == R.id.action_share){
            showShareMenu();
        }else if(id == android.R.id.home){
            TranslationsActivity.super.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showShareMenu() {
        final CharSequence[] items = {getString(R.string.kunstmaan_translations_include_unchanged_message)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.kunstmaan_translations_share_menu_title);
        builder.setMultiChoiceItems(items, null, null)
                .setNegativeButton(R.string.kunstmaan_translations_json, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        SparseBooleanArray sba = ((AlertDialog)dialog).getListView().getCheckedItemPositions();
                        dialog.dismiss();
                        if(sba.get(0)){
                            ShareUtil.shareJson2(TranslationsActivity.this, true);
                        }else{
                            ShareUtil.shareJson2(TranslationsActivity.this, false);
                        }
                    }
                });
        builder.setPositiveButton(R.string.kunstmaan_translations_xml,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        SparseBooleanArray sba = ((AlertDialog)dialog).getListView().getCheckedItemPositions();
                        if(sba.get(0)){
                            ShareUtil.shareXml2(TranslationsActivity.this, true);
                        }else{
                            ShareUtil.shareXml2(TranslationsActivity.this, false);
                        }
                        dialog.dismiss();

                    }
                });
        builder.setNeutralButton(R.string.kunstmaan_translations_cancel,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {                        dialog.dismiss();

                    }
                });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ShareUtil.SHARE_REQUEST){
            ShareUtil.cleanDir(TranslationsActivity.this);
        }
    }
}
