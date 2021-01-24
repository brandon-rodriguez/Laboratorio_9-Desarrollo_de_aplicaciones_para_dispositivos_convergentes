package com.example.laboratorio9;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private OdometerService odometer;
    private boolean bound = false;
    private boolean metros = false;
    private int segundos;
    private final int PERMISSION_REQUEST_CODE= 698;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            OdometerService.OdometerBinder odometerBinder = (OdometerService.OdometerBinder) service;
            odometer = odometerBinder.getOdometer();
            bound=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        metros=false;
        segundos=1;
        displayDistance();
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggle);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // metros
                    metros= true;
                } else {
                    // millas
                    metros=false;
                }
                odometer.setMedida(metros);
            }
        });

        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) findViewById( R.id.segundos);
                segundos = Integer.parseInt(tv.getText().toString());
                tv.setText("");
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ContextCompat.checkSelfPermission(this, OdometerService.PERMISSION_STRING) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String [] {OdometerService.PERMISSION_STRING}, PERMISSION_REQUEST_CODE);
        }else{
        Intent intent = new Intent(this, OdometerService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound){
            unbindService(connection);
            bound=false;
        }
    }

    private void displayDistance(){
        final TextView distanceView = (TextView) findViewById(R.id.distance);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                double distance =0.0;
                if(bound && odometer!= null){
                    distance = odometer.getDistance();
                }
                String distanceSTR;
                if (metros) {
                    distanceSTR=  String.format(Locale.getDefault(), "%1$, .2f metros", distance);
                }else{
                    distanceSTR=  String.format(Locale.getDefault(), "%1$, .2f millas", distance);
                }

                distanceView.setText(distanceSTR);
                handler.postDelayed(this, segundos*1000);
            }
        });
    }
}