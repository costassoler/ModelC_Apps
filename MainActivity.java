package com.example.modelcversion1;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    /////////////////////////////////Accel Variables/////////////////////////////

    private Sensor mySensor;
    private SensorManager SM;
    public static String wifiModuleIp = "192.168.0.100";
    public static int MotorPort = 21567;
    public static float fx=0;
    public static float fy=0;
    public static String CMD = "0,0,0";
    public static float a = 0;
    public static float b = 0;
    public static float c = 0;
    public static float fz=0;
    public static float L;
    public static float R;
    public static float V;
    public static float OffX = 6.8f;//.2;
    public static float ScaleX = 1.5f;//1.5;
    public static float ScaleY =3.2f;//3.7;
    public static String Result;
    public static Switch start;
    public static String Arm;
    WebView webView;

    /////////////////////////////////SeekBar Variables/////////////////////////////
    SeekBar seekbar;
    TextView textView;
    public static int Vert=100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(com.example.modelcversion1.R.layout.activity_main);
        webView = findViewById(com.example.modelcversion1.R.id.WebView);
        //webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("http://192.168.0.100:8000/stream.mjpg"); //https://www.youtube.com/watch?v=pM2W7Kmnk_E
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setVerticalScrollBarEnabled(false);
        seekbar = findViewById(com.example.modelcversion1.R.id.seekBar);
        textView = findViewById(com.example.modelcversion1.R.id.textView);
        start=findViewById(com.example.modelcversion1.R.id.switch1);


                /////////////////////////////////Accel /////////////////////////////////////
                // create our sensor manager:
                SM = (SensorManager) getSystemService(SENSOR_SERVICE);

        //accelerometer sensor:
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //register sensor listener:
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);


        //////////////////////////////////SeekBar//////////////////////////////////

        seekbar.setMax(200);
        seekbar.setProgress(Vert);

        start = findViewById(com.example.modelcversion1.R.id.switch1);




        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Vert = i;


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                /*Vert = 100;
                textView.setText(makeCommands());
                seekbar.setProgress(Vert);*/

            }

        });

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //not in use

    }


    public static String makeCommands() {
        a = -((-fx-OffX)/ScaleX)*100;
        b = ((fy)/ScaleY)*100;
        c = Vert-100;

        L = a-b;
        R = a+b;
        V = c;

        if (L>100f){
            L = 100f;
        }
        if(L<-100f){
            L = -100f;
        }
        if(R>100f){
            R = 100f;
        }
        if(R<-100f){
            R = -100f;
        }

        if(fz<0){
            L = -100f;
            R = -100f;
        }
        L = Math.round(L/10)*10;
        R = Math.round(R/10)*10;
        V = Math.round(V/10)*10;
        if(start.isChecked()){
            Result = String.valueOf(L) + "," + String.valueOf(R) + "," + String.valueOf(V);
        }else{
            Result = "0,0,0";
        }
        return Result;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        fx = event.values[0];
        fy = event.values[1];
        fz = event.values[2];


        textView.setText(makeCommands());


        Socket_AsyncTask cmd_Change_Servo = new Socket_AsyncTask();
        cmd_Change_Servo.execute();
    }

    public static class Socket_AsyncTask extends AsyncTask<Void,Void,Void>
    {
        Socket socket;
        @Override
        protected Void doInBackground(Void... params){
            try{
                InetAddress inetAddress = InetAddress.getByName(MainActivity.wifiModuleIp);
                socket = new java.net.Socket(inetAddress,MainActivity.MotorPort);
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeBytes(makeCommands());
                //dataOutputStream.write(TestInt); //sends a number pls
                dataOutputStream.close();
                socket.close();
            }catch (UnknownHostException e){e.printStackTrace();}catch (IOException e){e.printStackTrace();}
            return null;
        }
    }


}
