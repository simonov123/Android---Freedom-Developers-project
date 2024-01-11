package it.uniba.dib.sms232412.gestioneSpese;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Spesa implements Comparable<Spesa>{
    final private DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MMMM/yyyy");
    private String tipologia;
    private Double costo;
    private String data;

    //costruttore vuoto necessario per firebase
    public Spesa(){}

    public Spesa(String tipo, Double costo, String data){
        this.tipologia = tipo;
        this.costo = costo;
        this.data = data;
    }

    public String getTipologia(){ return tipologia; }
    public Double getCosto(){
        return costo;
    }
    public String getData(){
        return data;
    }

    public boolean isType(String tipo){
        return (tipologia.equals(tipo));
    }

    public boolean isDateInRange(LocalDate startDate, LocalDate endDate){
        try{
            LocalDate myDate = LocalDate.parse(data, formato);
            return (!myDate.isBefore(startDate) &&
                    !myDate.isAfter(endDate));
        } catch(Exception e){
            return false;
        }
    }

    @Override
    public int compareTo(Spesa o) {
        try{
            LocalDate myDate = LocalDate.parse(data, formato);
            LocalDate paramDate = LocalDate.parse(o.getData(), formato);
            return myDate.compareTo(paramDate);
        } catch(Exception e){
            return 0;
        }
    }
}
