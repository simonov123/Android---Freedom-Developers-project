package it.uniba.dib.sms232412.sezioneInformativa;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import it.uniba.dib.sms232412.MainActivity;
import it.uniba.dib.sms232412.R;

public class InfoFragment extends Fragment {

    private MainActivity activity;
    protected int num_region;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getActivity()==null || !(getActivity() instanceof MainActivity)) return;
        activity = (MainActivity) getActivity();

        // Verifico se ho letto una regione
        SharedPreferences shared = getActivity().getSharedPreferences("regione.txt", Context.MODE_PRIVATE);
        String region = shared.getString("REGIONE", "Nessuna");
        num_region = shared.getInt("NUM_REGIONE", -1);
        if(region.equals("Nessuna")){
            // Nel caso in cui non ho conferito il permesso, abilito il bottone per dare il permesso
            Button btn_region = view.findViewById(R.id.btn_search_region);
            btn_region.setVisibility(View.VISIBLE);
            btn_region.setOnClickListener(v -> activity.askExplicitPermissionForLocationInInfo());
        }

        getChildFragmentManager().beginTransaction()
                .replace(R.id.votation_frame, new InfoVotationFragment())
                .replace(R.id.info_region_frame, new InfoLocationFragment())
                .commit();

    }
}