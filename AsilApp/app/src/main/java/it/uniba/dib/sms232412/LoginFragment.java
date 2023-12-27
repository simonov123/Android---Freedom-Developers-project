package it.uniba.dib.sms232412;

import android.content.Context;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private ChangePanelListener mCallback = null;
    private TextInputEditText editEmail, editPassword;
    private Button login_button, register_button;
    private FirebaseAuth mAuth;

    public interface ChangePanelListener {
        void ChangePanel();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //devo essere sicuro che l'activity implementi ChangePanelListener
        if(context instanceof ChangePanelListener){
            mCallback = (ChangePanelListener) context;
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        login_button = (Button) view.findViewById(R.id.login_button);
        register_button = (Button) view.findViewById(R.id.register_button);
        editEmail = (TextInputEditText) view.findViewById(R.id.email);
        editPassword = (TextInputEditText) view.findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();

        register_button.setOnClickListener(v -> {
            mCallback.ChangePanel();
        });

        login_button.setOnClickListener(v -> {
            String email = String.valueOf(editEmail.getText());
            String password = String.valueOf(editPassword.getText());
            if(TextUtils.isEmpty(email)){
                Toast.makeText(getContext(), R.string.entry_empty_mail, Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(password)){
                Toast.makeText(getContext(), R.string.entry_empty_password, Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Toast.makeText(getContext(), R.string.entry_login_success, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getContext(), MainActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        } else {
                            Toast.makeText(getContext(), R.string.entry_login_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}