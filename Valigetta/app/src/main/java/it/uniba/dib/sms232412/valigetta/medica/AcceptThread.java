package it.uniba.dib.sms232412.valigetta.medica;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

public class AcceptThread extends Thread {
    private final Context context;
    private final BluetoothServerSocket mServerSocket;

    public AcceptThread(Context context) {
        this.context = context;
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            mServerSocket = null;
            return;
        }
        BluetoothServerSocket tmp = null;
        try {
            // UUID univoco per la tua applicazione Bluetooth
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            tmp = btAdapter.listenUsingRfcommWithServiceRecord("Valigetta", uuid);
        } catch(IOException e){}
        mServerSocket = tmp;
    }

    public void run(){
        BluetoothSocket socket;
        while(true){
            try{
                socket = mServerSocket.accept();
            } catch(IOException e){
                break;
            }
            if(socket != null){
                manageConnectedSocket(socket);
                try {
                    mServerSocket.close();
                } catch (IOException e) {}
                break;
            }
        }
    }

    public void cancel(){
        try{
            mServerSocket.close();
        } catch(IOException e){}
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        // lavoro da fare
        try{
            InputStream inputStream = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            MainActivity myContext = (MainActivity) context;
            String sensore = bufferedReader.readLine();

            myContext.setSensoreRichiesto(sensore);

            double misura;
            while(true){
                if(myContext.misuraEffettuata != null){
                    misura = myContext.misuraEffettuata;
                    break;
                }
            }
            dataOutputStream.writeDouble(misura);
            dataOutputStream.flush();
        }catch (IOException e){}
    }
}
