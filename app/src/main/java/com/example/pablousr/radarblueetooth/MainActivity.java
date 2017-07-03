package com.example.pablousr.radarblueetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;


//sensoreslibrerias
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "bluetooth1";

    Button auto, acc, gyro;
    TextView textoDistAng;
    TextView textoRecivir;

    //variables para blueetoth

    Handler h;
    private BluetoothAdapter btAdaptador = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    final int RECIVIR_MENSAJE = 1; //Status para el handler
    private StringBuilder cad = new StringBuilder();
    private ConnectedThread mConnectedThread;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //Adress mac del HC-05
    private static String mac = "98:D3:32:30:D4:0A";

    //variables para sensores
    SensorManager sensorManager;
    SensorManager proxManager;
    Sensor accel;
    Sensor rot;
    Sensor prox;

    int dir = QUIETO; //0 es quieto, 1 es izquierda, 2 es derecha
    public float mov = 0;
    public float proxDistancia = 100;
    int flag = 0;
    //Codigos
    private final static int QUIETO = 0;
    private final static int IZQ = 1;
    private final static int DER = 2;
    private final static int MODO_ANDROID = 3;
    private final static int AUTO = 4;
    private final static int ACC = 5;
    private final static int ROT = 6;
    private final static int SIN_CAMBIO = 7;
    private final static int COLOR = 8;


    //Variablñes para dibujar
    public Radar mRadar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auto = (Button) findViewById(R.id.auto);
        acc = (Button) findViewById(R.id.acc);
        gyro = (Button) findViewById(R.id.gyro);
        textoRecivir = (TextView) findViewById(R.id.textoRecivir);
        textoDistAng = (TextView) findViewById(R.id.textDistAng);
        btAdaptador = BluetoothAdapter.getDefaultAdapter();
        chekearBT();

      /*  auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //mConnectedThread.mandarMSJ("Conecto\n");
                flag = AUTO;
                mConnectedThread.mandarMSJ(Integer.toString(AUTO));
            }
        });

        acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //mConnectedThread.mandarMSJ("Conecto\n");
                flag = 2;
            }
        });

        gyro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //mConnectedThread.mandarMSJ("Conecto\n");
                flag = 3;
            }
        });*/


        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIVIR_MENSAJE:         //si recivio mensaje
                        // Log.d(TAG,"entro recivir");
                        byte[] leerBuffer = (byte[]) msg.obj;
                        String strRecivida = new String(leerBuffer, 0, msg.arg1);
                        cad.append(strRecivida);
                        int finDeLinea = cad.indexOf("\n");
                        if (finDeLinea > 0) {
                            String cadprint = cad.substring(0, finDeLinea);
                            cad.delete(0, cad.length());
                            textoRecivir.setText(cadprint);
                            mRadar.setDistAng(cadprint);

                        }
                        break;
                }
            }
        };
        //inicializacion sensores
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //proxManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        rot = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        prox = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        //proxManager.registerListener(this, prox,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(proxListener, prox, 5000000);


        //inicialiso el radar dibujo
        mRadar = (Radar) findViewById(R.id.radar);
        mRadar.startAnimation();


        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//Bloquea giro de la pantalla
    }


    private BluetoothSocket crearSocket(BluetoothDevice device) throws IOException {
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                error("Error en crear el socket." + e.getMessage() + ".");
            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    private void chekearBT() {
        if (btAdaptador == null) {
            error("No hay bluetoooth");
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

   /* private void mandarMSJ( String msj)
    {
        byte[] msgBuffer = msj.getBytes();
        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String warning = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (dir.equals("00:00:00:00:00:00"))
                warning = warning + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            warning = warning +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            error(msj);
        }
    }*/

    @Override //si no haces esto crashea
    public void onPause() {
        super.onPause();

        /*
        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                error("Error en onPause() : " + e.getMessage() + ".");
            }
        }*/

        try {
            btSocket.close();
        } catch (IOException e2) {
            error("Error en  onPause() fallo al cerrar el socket." + e2.getMessage() + ".");
        }
    }

    public void onResume() {
        super.onResume();
        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdaptador.getRemoteDevice(mac);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = crearSocket(device);
        } catch (IOException e1) {
            error("Error In onResume() and socket create failed: " + e1.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdaptador.cancelDiscovery();

        // Establish the connection.  This will block until it connects.

        try {
            btSocket.connect();

        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                error("Error In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.

        /*try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            error("Error In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }*/
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()


            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    //Recive datos de BT y los manda al hilo principal
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    h.obtainMessage(RECIVIR_MENSAJE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                    //Si tiene que mandar posiciones

                    if (flag == ACC) {
                        int aux = accelMsg(mov);
                        if (aux != SIN_CAMBIO) {
                            mConnectedThread.mandarMSJ(Integer.toString(aux));
                            Log.d(TAG, "entro acc");
                        }
                    } else if (flag == ROT) {
                        int aux = gyroMsg(mov);
                        if (aux != SIN_CAMBIO) {
                            mConnectedThread.mandarMSJ(Integer.toString(aux));
                            Log.d(TAG, "entro rot");
                        }
                    } else if (flag == AUTO) {
/*
                        int aux = proxMsg(mov);
                        if (aux!=SIN_CAMBIO){
                            mConnectedThread.mandarMSJ(Integer.toString(aux));
                            Log.d(TAG,"entro prox");
                        }*/
                        //aca va lo de prox msj
                        if (proxDistancia < 10) {
                            mConnectedThread.mandarMSJ(Integer.toString(COLOR));
                            Log.d(TAG, "entro prox");
                            Log.d(TAG, String.valueOf(proxDistancia));
                        }
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void mandarMSJ(String message) {

            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                error("Error data send: " + e.getMessage());
            }
        }
    }

    //Funciones de sensores

    public void auto(View view) {
        if (this.flag != AUTO) {
            sensorManager.registerListener(proxListener, prox, 500000);//SensorManager.SENSOR_DELAY_NORMAL);}
            try {
                // msg(accelMsg(mov));

            } catch (Exception e) {
                e.printStackTrace();
            }
            flag = AUTO;
            mConnectedThread.mandarMSJ(Integer.toString(AUTO));
        }
    }

    public void acc(View view) {

        if (this.flag != ACC) {
            sensorManager.registerListener(accelListener, accel, SensorManager.SENSOR_DELAY_NORMAL);
        }
        try {
            // msg(accelMsg(mov));

        } catch (Exception e) {
            e.printStackTrace();
        }
        this.flag = ACC;
        mConnectedThread.mandarMSJ(Integer.toString(MODO_ANDROID));
        // mConnectedThread.mandarMSJ(Integer.toString(DER));
    }

    public void rot(View view) {

        if (this.flag != ROT) {
            sensorManager.registerListener(rotListener, accel, SensorManager.SENSOR_DELAY_NORMAL);
        }
        try {
            // msg(gyroMsg(mov));
            //mConnectedThread.mandarMSJ("");//manda mensaje para que cambie a gyroscopio
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.flag = ROT;
        mConnectedThread.mandarMSJ(Integer.toString(MODO_ANDROID));
        // mConnectedThread.mandarMSJ(Integer.toString(IZQ));
    }

    public SensorEventListener accelListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }

        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            mov = (int) x;

        }
    };


    public SensorEventListener rotListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }

        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            mov = x;

        }
    };

    public SensorEventListener proxListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }

        public void onSensorChanged(SensorEvent event) {
            proxDistancia = event.values[0];
            //Log.d(TAG,String.valueOf(proxDistancia));

        }

    };


    public int gyroMsg(float x) {

        //String cad =new String();

        if (x < -1 && dir != IZQ) {
            dir = IZQ;
            return dir;
        } else {
            if (x > 1 && dir != DER) {
                dir = DER;
                return dir;
            } else if (-1 < x && x < 1 && dir != QUIETO) {
                dir = QUIETO;
                return dir;
            }
        }

        return SIN_CAMBIO;
    }

    public int accelMsg(float x) {

        //String cad =new String();

        if (x < -0.05 && dir != IZQ) {
            dir = IZQ;
            return dir;
        } else {
            if (x > 0.05 && dir != DER) {
                dir = DER;
                return dir;
            } else if (-0.05 < x && x < 0.05 && dir != QUIETO) {
                dir = QUIETO;
                return dir;
            }
        }

        return SIN_CAMBIO;
    }

}