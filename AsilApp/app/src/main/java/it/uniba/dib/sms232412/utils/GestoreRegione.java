package it.uniba.dib.sms232412.utils;

public class GestoreRegione {
    static final private String[] regioni = {
            "Abruzzo", "Basilicata", "Calabria", "Campania", "Emilia Romagna",
            "Friuli Venezia Giulia", "Lazio", "Liguria", "Lombardia", "Marche",
            "Molise", "Piemonte", "Puglia", "Sardegna", "Sicilia", "Toscana",
            "Trentino Alto Adige", "Umbria", "Val D'Aosta", "Veneto"
    };

    static final private String[] regioni2 = {
            "Abruzzo", "Lucania", "Calabria", "Campania", "Emilia Romagna",
            "Friuli Venice Julia", "Latium", "Liguria", "Lombardy", "Marches",
            "Molise", "Piedmont", "Apulia", "Sardinia", "Sicily", "Tuscany",
            "Trentino Alto Adige", "Umbria", "Aosta Valley", "Veneto"
    };

    static public int getNumberRegion(String region){
        int num = -1;
        for (int i=0; i<regioni.length; i++) {
            if(regioni[i].equals(region)){
                num = i;
                break;
            }
        }
        if(num != -1) return num;
        for (int i=0; i<regioni2.length; i++) {
            if(regioni2[i].equals(region)){
                num = i;
                break;
            }
        }
        return num;
    }
}
