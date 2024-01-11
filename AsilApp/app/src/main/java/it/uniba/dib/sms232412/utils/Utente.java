package it.uniba.dib.sms232412.utils;

import java.util.List;

import it.uniba.dib.sms232412.gestioneSpese.Spesa;

public class Utente implements Comparable<Utente>{
    private String uid;
    private String nome;
    private String cognome;
    private String email;
    private String sesso;
    private String ruolo;
    private List<Spesa> spese;

    //costruttore vuoto necessario per firebase
    public Utente(){}

    public String getUid(){
        return uid;
    }
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
    public String getRuolo() {
        return ruolo;
    }
    public List<Spesa> getSpese(){
        return spese;
    }

    @Override
    public int compareTo(Utente o) {
        int firstCondition = nome.toLowerCase().compareTo(o.getNome().toLowerCase());
        if(firstCondition == 0){
            return cognome.toLowerCase().compareTo(o.getCognome().toLowerCase());
        }
        return firstCondition;
    }
}
