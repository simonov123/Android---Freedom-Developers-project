package it.uniba.dib.sms232412.personaleSanitario;

public class PersonaleSanitario implements Comparable<PersonaleSanitario>{
    private String nomeCompleto;
    private String email;
    private String professione;
    private String istituzione;
    private String regione;
    private boolean visibility;

    //costruttore vuoto necessario per firebase
    public PersonaleSanitario(){

    }

    public String getNomeCompleto(){
        return nomeCompleto;
    }

    public String getEmail(){
        return email;
    }

    public String getProfessione() {
        return professione;
    }

    public String getIstituzione() {
        return istituzione;
    }

    public String getRegione(){
        return regione;
    }

    public boolean getVisibility(){
        return visibility;
    }

    @Override
    public int compareTo(PersonaleSanitario o) {
        return nomeCompleto.toLowerCase().compareTo(o.getNomeCompleto().toLowerCase());
    }
}
