package com.example.pablousr.radarblueetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class PrincipalActivity extends AppCompatActivity {

    public final static String clave = "Radar";
    BluetoothDevice device =null;
    private BluetoothSocket btSocket = null;
    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Button auto, acc, gyro;
    TextView textoRecivir,textoDistAng,textoModo,textoEscala;

    Handler h;
    final int RECIVIR_MENSAJE = 1; //Status para el handler
    private StringBuilder cad = new StringBuilder();
    private ConnectedThread mConnectedThread;


    //Variablñes para dibujar
    public Radar mRadar;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//Bloquea giro de la pantalla

        Intent i = getIntent();
        device= getIntent().getExtras().getParcelable(clave);

      //  auto = (Button) findViewById(R.id.auto);
        textoRecivir = (TextView) findViewById(R.id.textoRecivir);
        textoRecivir.setTextColor(Color.WHITE);
        textoDistAng = (TextView) findViewById(R.id.textDistAng);
        textoDistAng.setTextColor(Color.WHITE);
        textoModo = (TextView) findViewById(R.id.textoModo);
        textoModo.setTextSize(40);
        textoModo.setTextColor(Color.WHITE);
        textoEscala = (TextView) findViewById(R.id.textoEscala);
        textoEscala.setTextColor(Color.WHITE);


        auto = (Button) findViewById(R.id.auto);
        acc = (Button) findViewById(R.id.acc);
        gyro = (Button) findViewById(R.id.gyro);

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

    }

    private void error(String cad) {
        Toast.makeText(getBaseContext(), cad, Toast.LENGTH_LONG).show();
        finish();
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

    @Override //si no haces esto crashea
    public void onPause() {
        super.onPause();

        try {
            btSocket.close();
        } catch (IOException e2) {
            error("Error en  onPause() fallo al cerrar el socket." + e2.getMessage() + ".");
        }
    }
    public void onResume() {
        super.onResume();

        try {
            btSocket = crearSocket(device);
        } catch (IOException e1) {
            error("Error In onResume() and socket create failed: " + e1.getMessage() + ".");
        }

        try {
            btSocket.connect();

        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                error("Error In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

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
                           // Log.d(TAG, "entro acc");
                        }
                    } else if (flag == ROT) {
                        int aux = gyroMsg(mov);
                        if (aux != SIN_CAMBIO) {
                            mConnectedThread.mandarMSJ(Integer.toString(aux));
                           // Log.d(TAG, "entro rot");
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
                            //Log.d(TAG, "entro prox");
                            //Log.d(TAG, String.valueOf(proxDistancia));
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
            textoModo.setText("Modo: Automatico");
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
        textoModo.setText("Modo: Acelerometro");
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
        textoModo.setText("Modo: Gyroscopio");
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
