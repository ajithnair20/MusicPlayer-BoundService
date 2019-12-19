package com.example.audioclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clipserver.ICalService;

public class MainActivity extends AppCompatActivity {

    EditText editName, editVal1, editVal2;
    TextView resultView;
    protected ICalService calService = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    @Override
    protected void onStart() {
        super.onStart();
        editName = (EditText)findViewById(R.id.name);
        editVal1 = (EditText) findViewById(R.id.num1);
        editVal2 = (EditText) findViewById(R.id.num2);
        resultView = (TextView) findViewById(R.id.result);
        if (calService == null) {
            Intent it = new Intent("musicservice");
            it.setPackage("com.example.clipserver");
            bindService(it, connection, Context.BIND_AUTO_CREATE);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
    public void multiply(View v) {
        switch (v.getId()) {
            case R.id.multiply_btn: {
                int num1 = Integer.parseInt(editVal1.getText().toString());
                int num2 = Integer.parseInt(editVal2.getText().toString());
                try {
                    int result = calService.getResult(num1, num2);
                    String msg = calService.getMessage(editName.getText().toString());
                    resultView.setText(msg + result);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            calService = ICalService.Stub.asInterface(service);
            Toast.makeText(getApplicationContext(),	"Service Connected", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            calService = null;
            Toast.makeText(getApplicationContext(), "Service Disconnected", Toast.LENGTH_SHORT).show();
        }
    };
}
