package it.uniba.dib.sms232412.valigetta.medica;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

public class MisuraBattitiFragment extends Fragment {

    private MainActivity parentActivity;
    private TextView editMisuraRilevata;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_misura_battiti, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getActivity() == null || !(getActivity() instanceof MainActivity)) return;
        parentActivity = (MainActivity) getActivity();

        editMisuraRilevata = view.findViewById(R.id.battiti_misura_rilevata);

        // Creo il bottone per simulare il sensore dei battiti cardiaci
        Button btnSimulaBattiti = view.findViewById(R.id.btn_simula_battiti);
        btnSimulaBattiti.setOnClickListener(v -> {
            Random random =new Random();
            double misura = (double) random.nextInt(150)+45;
            parentActivity.misuraTemporanea = misura;
            editMisuraRilevata.setText(getString(R.string.battiti_misura_rilevata, misura));
        });
    }
}