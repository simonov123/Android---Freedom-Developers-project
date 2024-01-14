package it.uniba.dib.sms232412.sezioneInformativa;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import it.uniba.dib.sms232412.R;

public class InfoLocationFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info_location, container, false);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getActivity() == null) return;
        // Imposto il bottone per visionare i numeri di emergenza
        Button emergencyBtn = view.findViewById(R.id.info_emergency_number_btn);
        emergencyBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.info_emergency_btn))
                    .setMessage(getString(R.string.info_emergency_number_description))
                    .setPositiveButton(R.string.permission_ok, ((dialog, which) -> {}));
            builder.show();
        });

        // Recupero la regione letta dal fragment genitore
        if(!(getParentFragment() instanceof InfoFragment)) return;
        InfoFragment parentFrag = (InfoFragment) getParentFragment();
        int num_region = parentFrag.num_region;

        if(num_region == -1) return;
        // Da qui procedo solo se ho una regione italiana registrata

        TextView textDescription = view.findViewById(R.id.info_description_region);
        textDescription.setText(getActivity().getResources().getStringArray(R.array.info_region_description)[num_region]);

        int[] regionImages = {
                R.drawable.region_abruzzo,
                R.drawable.region_basilicata,
                R.drawable.region_calabria,
                R.drawable.region_campania,
                R.drawable.region_emilia_romagna,
                R.drawable.region_friuli_venezia_giulia,
                R.drawable.region_lazio,
                R.drawable.region_liguria,
                R.drawable.region_lombardia,
                R.drawable.region_marche,
                R.drawable.region_molise,
                R.drawable.region_piemonte,
                R.drawable.region_puglia,
                R.drawable.region_sardegna,
                R.drawable.region_sicilia,
                R.drawable.region_toscana,
                R.drawable.region_trentino_alto_adige,
                R.drawable.region_umbria,
                R.drawable.region_val_d_aosta,
                R.drawable.region_veneto,
        };
        ImageView img = view.findViewById(R.id.info_image_region);
        img.setImageDrawable(getActivity().getDrawable(regionImages[num_region]));
    }
}