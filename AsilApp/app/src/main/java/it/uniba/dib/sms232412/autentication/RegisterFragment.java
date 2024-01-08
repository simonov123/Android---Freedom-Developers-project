package it.uniba.dib.sms232412.autentication;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.uniba.dib.sms232412.MainActivity;
import it.uniba.dib.sms232412.R;

public class RegisterFragment extends Fragment {

    private TextInputEditText editEmail, editPassword, editConfirmPassword, editName, editSurname;
    private RadioButton m_btn, f_btn;
    private FirebaseAuth mAuth;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button register_button = view.findViewById(R.id.register_button);
        editEmail = view.findViewById(R.id.email);
        editPassword = view.findViewById(R.id.password);
        editConfirmPassword = view.findViewById(R.id.confirm_password);
        editName = view.findViewById(R.id.nome);
        editSurname = view.findViewById(R.id.cognome);
        m_btn = view.findViewById(R.id.m_btn);
        f_btn = view.findViewById(R.id.f_btn);
        mAuth = FirebaseAuth.getInstance();

        m_btn.setOnCheckedChangeListener((buttonView, isChecked) -> f_btn.setChecked(!isChecked));

        f_btn.setOnCheckedChangeListener((buttonView, isChecked) -> m_btn.setChecked(!isChecked));

        register_button.setOnClickListener(v -> {
            String nome = String.valueOf(editName.getText());
            String cognome = String.valueOf(editSurname.getText());
            String email = String.valueOf(editEmail.getText());
            String password = String.valueOf(editPassword.getText());
            if(nome.length()==0){
                Toast.makeText(getContext(), R.string.entry_empty_name, Toast.LENGTH_SHORT).show();
                return;
            }
            if(cognome.length()==0){
                Toast.makeText(getContext(), R.string.entry_empty_surname, Toast.LENGTH_SHORT).show();
                return;
            }
            if(!(m_btn.isChecked() || f_btn.isChecked())){
                Toast.makeText(getContext(), R.string.entry_choice_gender, Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(email)){
                Toast.makeText(getContext(), R.string.entry_empty_mail, Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(password)){
                Toast.makeText(getContext(), R.string.entry_empty_password, Toast.LENGTH_SHORT).show();
                return;
            }
            if(editConfirmPassword.getText()==null || !password.equals(editConfirmPassword.getText().toString())){
                Toast.makeText(getContext(), R.string.entry_different_password, Toast.LENGTH_SHORT).show();
                return;
            }
            if(password.length() < 6){
                Toast.makeText(getContext(), R.string.entry_short_password, Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user!=null) {
                                String myUid = user.getUid();
                                DatabaseReference rootDB = FirebaseDatabase.getInstance().getReference("Utenti").child(myUid);
                                rootDB.child("uid").setValue(myUid);
                                rootDB.child("email").setValue(email);
                                rootDB.child("nome").setValue(nome);
                                rootDB.child("cognome").setValue(cognome);
                                rootDB.child("ruolo").setValue("utente");
                                rootDB.child("update").setValue(false);
                                if (m_btn.isChecked()) {
                                    rootDB.child("sesso").setValue("M");
                                } else if (f_btn.isChecked()) {
                                    rootDB.child("sesso").setValue("F");
                                }

                                Toast.makeText(getContext(), R.string.entry_register_success, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getContext(), MainActivity.class);
                                startActivity(intent);
                                if(getActivity() != null){
                                    getActivity().finish();
                                }
                            }
                        } else {
                            Toast.makeText(getContext(), R.string.entry_register_failed, Toast.LENGTH_SHORT).show();
                        }
                    });

        });
    }
}