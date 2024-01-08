package it.uniba.dib.sms232412.adminSection;

public class RichiestaPerDiventarePS {
    private String uid;
    private String codiceFiscale;
    private String nomeCompleto;
    private String email;
    private String professione;
    private String istituzione;
    private String regione;
    private boolean visibility;

    //costruttore vuoto necessario per firebase
    public RichiestaPerDiventarePS(){

    }

    public String getUid(){
        return uid;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
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
}
