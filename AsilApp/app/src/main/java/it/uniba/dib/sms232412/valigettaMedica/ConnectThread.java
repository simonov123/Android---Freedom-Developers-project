package it.uniba.dib.sms232412.valigettaMedica;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

public class ConnectThread extends Thread {
    private final ValigettaActivity context;
    private final BluetoothSocket socket;
    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    public ConnectThread(ValigettaActivity context, BluetoothDevice device) {
        this.context = context;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            socket = null;
            return;
        }
        BluetoothSocket tmp = null;
        try {
            // UUID univoco per la tua applicazione Bluetooth
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
        }
        socket = tmp;
    }

    public void run() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        btAdapter.cancelDiscovery();
        try{
            socket.connect();
        } catch(IOException connectException){
            try{
                socket.close();
            } catch(IOException closeException){}
            return;
        }
        manageConnectedSocket();
    }

    public void cancel(){
        try{
            socket.close();
        } catch(IOException e){}
    }
    private void manageConnectedSocket() {
        // lavoro da fare
        try{
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            OutputStream outputStream = socket.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

            bufferedWriter.write(context.getSensore());
            bufferedWriter.newLine();  // Aggiungo una nuova riga per indicare la fine della stringa
            bufferedWriter.flush();    // Mi assicuro di inviare effettivamente il sensore richiesto

            context.misuraRilevata = dataInputStream.readDouble();
        }catch (IOException e){}
    }
}
