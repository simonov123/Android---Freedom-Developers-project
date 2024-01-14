package it.uniba.dib.sms232412.utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import it.uniba.dib.sms232412.AnagraficaFragment;
import it.uniba.dib.sms232412.gestioneSpese.GestoreSpesaFragment;
import it.uniba.dib.sms232412.sezioneInformativa.InfoFragment;
import it.uniba.dib.sms232412.sezioneMalattie.MalattieFragment;

public class PagerAdapter extends FragmentStateAdapter {
    final static private int NUM_PAGINE = 4;
    public PagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment frag;
        switch (position){
            case 0:
                frag = new AnagraficaFragment();
                break;
            case 1:
                frag = new MalattieFragment();
                break;
            case 2:
                frag = new InfoFragment();
                break;
            case 3:
                frag = new GestoreSpesaFragment();
                break;
            default:
                frag = new Fragment();
        }
        return frag;
    }

    @Override
    public int getItemCount() {
        return NUM_PAGINE;
    }
}
