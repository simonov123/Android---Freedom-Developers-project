package it.uniba.dib.sms232412.gestioneSpese;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import it.uniba.dib.sms232412.R;


public class GestoreSpesaFragment extends Fragment implements GestoreSpesaAddFragment.ChiusuraPannelloAggiunta {

    static final private String ADD_TAG = "ADD_TAG";
    static final private String CALCULATE_TAG = "CALCULATE_TAG";
    static final private String LIST_TAG = "LIST_TAG";
    private FloatingActionButton fabAdd, fabCalcola;
    private boolean fabAddIsPressed = false;
    private boolean fabCalcolaIsPressed = false;
    private FragmentManager manager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gestore_spesa, container, false);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(manager == null && getActivity() != null){
            manager = getActivity().getSupportFragmentManager();
        }

        reloadList();

        fabAdd = view.findViewById(R.id.fab_aggiungi_spesa);
        fabAdd.setOnClickListener(v -> {
            fabAdd.setEnabled(false);
            fabCalcola.setEnabled(false);
            boolean mustCommit = true;
            if(manager != null) {
                FragmentTransaction ft = manager.beginTransaction();
                ft.setCustomAnimations(R.anim.spese_anim_enter_from_right, R.anim.spese_anim_exit_to_right);
                if(fabAddIsPressed){
                    ViewCompat.setElevation(fabAdd, getResources().getDimension(R.dimen.fab_elevation_resting));
                    fabAdd.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_logo));
                    fabAddIsPressed = false;
                    Fragment toRemove = manager.findFragmentByTag(ADD_TAG);
                    if(toRemove != null){
                        FrameLayout frame = view.findViewById(R.id.frame_aggiungi_spese);
                        mustCommit = false;
                        animationExit(frame, R.anim.spese_anim_exit_to_right, ft, toRemove);
                    }
                } else {
                    ViewCompat.setElevation(fabAdd, getResources().getDimension(R.dimen.fab_elevation_pressed));
                    fabAdd.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_logo));
                    fabAddIsPressed = true;
                    ft.add(R.id.frame_aggiungi_spese, new GestoreSpesaAddFragment(this), ADD_TAG);
                }
                if(fabCalcolaIsPressed){
                    ViewCompat.setElevation(fabCalcola, getResources().getDimension(R.dimen.fab_elevation_resting));
                    fabCalcola.setImageDrawable(getResources().getDrawable(R.drawable.ic_calculate_logo));
                    fabCalcolaIsPressed = false;
                    Fragment toRemove = manager.findFragmentByTag(CALCULATE_TAG);
                    if(toRemove != null){
                        FrameLayout frame = view.findViewById(R.id.frame_calcola_spese);
                        mustCommit = false;
                        animationExit(frame, R.anim.spese_anim_exit_to_right, ft, toRemove);
                    }
                }
                if(mustCommit){
                    ft.commit();
                }
            }
            fabAdd.setEnabled(true);
            fabCalcola.setEnabled(true);
        });

        fabCalcola = view.findViewById(R.id.fab_calcola_spesa);
        fabCalcola.setOnClickListener(v -> {
            fabAdd.setEnabled(false);
            fabCalcola.setEnabled(false);
            boolean mustCommit = true;
            if(manager != null){
                FragmentTransaction ft = manager.beginTransaction();
                ft.setCustomAnimations(R.anim.spese_anim_enter_from_right, R.anim.spese_anim_exit_to_right);
                if(fabCalcolaIsPressed){
                    ViewCompat.setElevation(fabCalcola, getResources().getDimension(R.dimen.fab_elevation_resting));
                    fabCalcola.setImageDrawable(getResources().getDrawable(R.drawable.ic_calculate_logo));
                    fabCalcolaIsPressed = false;
                    Fragment toRemove = manager.findFragmentByTag(CALCULATE_TAG);
                    if(toRemove != null){
                        FrameLayout frame = view.findViewById(R.id.frame_calcola_spese);
                        mustCommit = false;
                        animationExit(frame, R.anim.spese_anim_exit_to_right, ft, toRemove);
                    }
                } else {
                    ViewCompat.setElevation(fabCalcola, getResources().getDimension(R.dimen.fab_elevation_pressed));
                    fabCalcola.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_logo));
                    fabCalcolaIsPressed = true;
                    ft.add(R.id.frame_calcola_spese, new GestoreSpesaCalcolaFragment(), CALCULATE_TAG);
                }
                if(fabAddIsPressed){
                    ViewCompat.setElevation(fabAdd, getResources().getDimension(R.dimen.fab_elevation_resting));
                    fabAdd.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_logo));
                    fabAddIsPressed = false;
                    Fragment toRemove = manager.findFragmentByTag(ADD_TAG);
                    if(toRemove != null){
                        FrameLayout frame = view.findViewById(R.id.frame_aggiungi_spese);
                        mustCommit = false;
                        animationExit(frame, R.anim.spese_anim_exit_to_right, ft, toRemove);
                    }
                }
                if(mustCommit){
                    ft.commit();
                }
            }
            fabAdd.setEnabled(true);
            fabCalcola.setEnabled(true);
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void chiudiPannelloAggiunta() {
        if(manager != null){
            FragmentTransaction ft = manager.beginTransaction();
            ViewCompat.setElevation(fabAdd, getResources().getDimension(R.dimen.fab_elevation_resting));
            fabAdd.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_logo));
            fabAddIsPressed = false;
            Fragment toRemove = manager.findFragmentByTag(ADD_TAG);
            if(toRemove != null){
                ft.remove(toRemove);
            }
            reloadList();
            ft.commit();
        }
    }

    public void reloadList(){
        getChildFragmentManager().beginTransaction()
                .replace(R.id.frame_lista_spese, new GestoreSpesaListaFragment(), LIST_TAG)
                .commit();
    }

    private void animationExit(FrameLayout frame, int anim, FragmentTransaction ft, Fragment fragToRemove){
        Animation exitAnimation = AnimationUtils.loadAnimation(getActivity(), anim);
        exitAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ft.remove(fragToRemove);
                ft.commit();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        frame.startAnimation(exitAnimation);
    }
}