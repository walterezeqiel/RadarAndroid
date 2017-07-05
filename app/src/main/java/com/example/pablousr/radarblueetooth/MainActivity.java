package com.example.pablousr.radarblueetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button conexion,salir;
    TextView titulo;
    public final static String clave = "Radar";
    private BluetoothAdapter btAdaptador = null;
    public BluetoothDevice device =null;
    //private BluetoothSocket btSocket = null;

    //Adress mac del HC-05
    private static String mac = "98:D3:32:30:D4:0A";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        conexion=(Button)findViewById(R.id.conexion);
        salir=(Button)findViewById(R.id.salir);
        titulo=(TextView)findViewById(R.id.titulo);
        titulo.setTextSize(40);
        titulo.setTextColor(Color.WHITE);
        btAdaptador = BluetoothAdapter.getDefaultAdapter();
        chekearBT();


    }

    public void Conectar(View view)
    {
        Intent i = new Intent(this,PrincipalActivity.class);
        i.putExtra(clave,device);
        startActivity(i);
    }

    public  void fnSalir(){
        finish();

    }

    private void chekearBT() {
        if (btAdaptador == null) {
            error("No hay bluetooth");
        } else {
            if (!btAdaptador.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void error(String cad) {
        Toast.makeText(getBaseContext(), cad, Toast.LENGTH_LONG).show();
        finish();
    }
    public void onResume() {
        super.onResume();
        // Set up a pointer to the remote node using it's address.
         device = btAdaptador.getRemoteDevice(mac);
        btAdaptador.cancelDiscovery();

    }
}
