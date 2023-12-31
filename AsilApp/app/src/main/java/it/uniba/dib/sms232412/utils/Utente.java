package it.uniba.dib.sms232412.utils;

import java.util.List;

public class Utente {
    private String nome;
    private String cognome;
    private String email;
    private String sesso;
    private List<Spesa> spese;

    //costruttore vuoto necessario per firebase
    public Utente(){}

    public String getNome(){
        return nome;
    }
    public String getCognome(){
        return cognome;
    }
    public String getEmail(){
        return email;
    }
    public String getSesso(){
        return sesso;
    }
    public List<Spesa> getSpese(){
        return spese;
    }
}
