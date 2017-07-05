package com.example.pablousr.radarblueetooth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Pablousr on 02/07/2017.
 */

public class Radar extends View{


    Paint paint = new Paint();//como dibujar. Canvas es que dibujar
    Paint paintEncontradas = new Paint();
    Paint paintFondo = new Paint();
    private Canvas canvas = null;
    private int fps = 20;
    public int dist=0,ang=30,direc=0;//0 es izquierda, 1 es derecha
    ArrayList<LineasEncontradas> lineas= new ArrayList<LineasEncontradas>();
    LineasEncontradas anterior=new LineasEncontradas(0,0,0,0,0),actual=new LineasEncontradas(0,0,0,0,0);
    private int primerPaso=0;
    private int distAjustada;

    private static int pisoY = 750;
    private static int iniX =10;
    private static int finX=1430;
    private static int puntoMedio=720;
    private static int radio = 710;

    public class LineasEncontradas {

        private int startX;
        private int startY;
        private int finishX;
        private int finishY;
        private int angle;

        public LineasEncontradas(int sx, int sy, int fx, int fy,int a) {
            this.startX = sx;
            this.startY = sy;
            this.finishX = fx;
            this.finishY = fy;
            this.angle=a;
        }

        public int getStartX() {
            return this.startX;
        }

        public int getStartY() {
            return this.startY;
        }

        public int getFinishX() {
            return this.finishX;
        }

        public int getFinishY() {
            return this.finishY;
        }

        public int getAngle() {
            return this.angle;
        }

        public void setear(int sx, int sy, int fx, int fy,int a) {
            this.startX = sx;
            this.startY = sy;
            this.finishX = fx;
            this.finishY = fy;
            this.angle=a;
        }


    }
    /*
    private final static Point puntoCentro = new Point(140,200);
    private final static Point pisoIzq =new Point(20,200);
    private final static Point pisoDer =new Point(260,200);
    private final static Point techo =new Point(140,0);
    private final static int radioMayor =140;*/

    public Radar(Context context) {
        this(context, null);
    }

    public Radar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Radar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint.setColor(Color.GREEN);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.0F);
        paintEncontradas.setColor(Color.RED);
        paintEncontradas.setAntiAlias(true);
        paintEncontradas.setStyle(Paint.Style.STROKE);
        paintEncontradas.setStrokeWidth(1.0F);
        paintFondo.setColor(Color.BLACK);
        paintFondo.setAntiAlias(true);
        paintFondo.setStyle(Paint.Style.FILL);

    }

    //Thread que dibuje
    Handler mHandler = new Handler();
    Runnable dibujante = new Runnable() {
        private String cad;
        @Override
        public void run() {
            invalidate();
            mHandler.postDelayed(this, 1000 / fps);
        }
    };

    public void startAnimation() {
        mHandler.removeCallbacks(dibujante);
        mHandler.post(dibujante);
    }

    public void stopAnimation() {
        mHandler.removeCallbacks(dibujante);
    }

    @Override
    public void onDraw(Canvas canvas) {
        this.canvas = canvas;


        drawRadarFondo();

        drawRadarMov();

        drawEncontrados();
        //Log.d("val","------------------------------------"+Integer.toString(dist)+" d-a "+Integer.toString(ang));
    }

    public void drawRadarFondo(){

        /* cel pablo
        canvas.drawLine(0,280,280,280,paint);//dibuja el piso
        canvas.drawLine(140,280,140,0,paint);//dibujo linea al techo

        canvas.drawCircle(140,280,160,paint);
        canvas.drawCircle(140,280,140,paint);
        canvas.drawCircle(140,280,120,paint);//circulo
        canvas.drawCircle(140,280,100,paint);
        canvas.drawCircle(140,280,80,paint);
        canvas.drawCircle(140,280,60,paint);
        canvas.drawCircle(140,280,40,paint);
        canvas.drawCircle(140,280,20,paint);*/

        canvas.drawLine(iniX,pisoY,finX,pisoY,paint);//dibuja el piso
        canvas.drawLine(puntoMedio,pisoY,radio,0,paint);//dibujo linea al techo

        //canvas.drawCircle(720,750,710,paint);//punto medio de la pantalla, radio de la linea
        canvas.drawCircle(puntoMedio,pisoY,700,paint);
        canvas.drawCircle(puntoMedio,pisoY,600,paint);
        canvas.drawCircle(puntoMedio,pisoY,500,paint);
        canvas.drawCircle(puntoMedio,pisoY,400,paint);
        canvas.drawCircle(puntoMedio,pisoY,300,paint);
        canvas.drawCircle(puntoMedio,pisoY,200,paint);
        canvas.drawCircle(puntoMedio,pisoY,100,paint);

       // canvas.drawRect(0,751,1440,751,paintFondo);


    }

    public void drawRadarMov(){
        //int radio=140;

        /*double angle = Math.toRadians(145);
        int offsetX =  (int) (i + (float)(i * Math.cos(angle)));
        int offsetY = (int) (i - (float)(i * Math.sin(angle)));*/
        double angle= Math.toRadians(this.ang);
        // double angle = 20;
        //Log.d("val","angle: "+Double.toString(angle));
/*
        int offsetX =140+ (int)((float)radioMayor* Math.cos(angle));
        int offsetY =280+(int)((float)radioMayor*- Math.sin(angle));*/
        int offsetX =  (int) (puntoMedio + (float)(radio * Math.cos(angle)));
        int offsetY = (int) (pisoY - (float)(radio * Math.sin(angle)));
        canvas.drawLine(puntoMedio,pisoY,offsetX,offsetY,paint);
        if (this.dist < 70 && this.dist>3){ // dist en cm
            distAjustada=this.dist*10;
            //canvas.drawLine(140,280-dist,offsetX,offsetY,paintEncontradas);
            //LineasEncontradas temp = new LineasEncontradas(140+dist,280-dist,offsetX,offsetY,ang);
            LineasEncontradas temp = new LineasEncontradas((int) (puntoMedio + (float)(distAjustada * Math.cos(angle))),(int) (pisoY - (float)(distAjustada * Math.sin(angle))),offsetX,offsetY,ang);
            lineas.add(temp);
            Log.d("val","-----------------------Distancia:"+Integer.toString(distAjustada));
        }
        if (primerPaso==0){
            anterior.setear(puntoMedio,pisoY,offsetX,offsetY,this.ang);
            actual.setear(puntoMedio,pisoY,offsetX,offsetY,this.ang);
            primerPaso=1;
        }else if (primerPaso==1){
            actual.setear(puntoMedio,pisoY,offsetX,offsetY,this.ang);
            primerPaso=2;
        }else if (primerPaso==2) {
            anterior.setear(actual.getStartX(), actual.getStartY(), actual.getFinishX(), actual.getFinishY(), actual.getAngle());
            actual.setear(puntoMedio,pisoY, offsetX, offsetY, this.ang);

            if (ang>10) {
                if (direc == 0) {
                    if (actual.getAngle() > anterior.getAngle()) {


                        direc = 1;
                        lineas.clear();
                    }
                } else if (direc == 1) {
                    if (actual.getAngle() < anterior.getAngle()) {


                        direc = 0;
                        lineas.clear();
                    }
                }
            }
        }
        //Log.d("val",Integer.toString(offsetX)+" x-y "+Integer.toString(offsetY));

    }

    public void drawEncontrados(){
        if (!lineas.isEmpty()){
            for (int c=0;c<lineas.size();c++){
                canvas.drawLine(lineas.get(c).getStartX(),lineas.get(c).getStartY(),
                        lineas.get(c).getFinishX(),lineas.get(c).getFinishY(),paintEncontradas);
            }
        }
    }

    public void setDistAng(String cad){
       /* this.dist=d;
        this.ang=a;*/

        String[] aux = cad.split(":");

        try {
            this.dist = Integer.parseInt(aux[0]);
        } catch (Exception e) {
            this.dist=1000;
        }
        try {
            this.ang = Integer.parseInt(aux[1]);
        } catch (Exception e) {
            this.ang=0;
        }
        //this.dist=Integer.valueOf(aux[0]);
        //this.ang=Integer.valueOf(aux[1]);
        //this.ang=30;
        //Log.d("val","-----------------------------------------------angle: "+Integer.toString(ang));
        /*int d=Integer.valueOf(aux[0]);
        int a=Integer.valueOf(aux[1]);*/
        // mRadar.setDistAng(d,a);
    }
}
