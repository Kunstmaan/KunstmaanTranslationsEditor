package be.kunstmaan.translationseditor.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import be.kunstmaan.translationseditor.KunstmaanTranslationUtil;
import be.kunstmaan.translationseditor.views.TranslationsListFragment;


public class ViewPagerAdapter extends SmartFragmentStatePagerAdapter {

    private static final int NUM_ITEM = 3;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return TranslationsListFragment.newInstance(KunstmaanTranslationUtil.getValuesFromCurrentScreen());
            case 1:
                return TranslationsListFragment.newInstance(KunstmaanTranslationUtil.getValuesFromMemory());
            case 2:
                return TranslationsListFragment.newInstance(KunstmaanTranslationUtil.getAllValues());
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_ITEM;
    }
}
