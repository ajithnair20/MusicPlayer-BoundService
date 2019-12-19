package com.example.audioclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.clipserver.ICalService;

public class AudioTrackActivity extends AppCompatActivity {

    //Declaring Views
    Button btnStartService, btnStopService, btnPlay, btnStop, btnPauseResume;
    RadioGroup radioTrack;
    protected ICalService calService = null;
    Intent i;
    int selectedTrack;

    //Program state variables
    boolean isServiceStarted = false;
    boolean isBound = false;
    int songStatus;
    final int MUSIC_PLAY=1;
    final int MUSIC_PAUSE=2;
    final int MUSIC_RESUME=3;
    final int MUSIC_STOP=4;

    //Constants
    final String TAG = "Audio Client";
    final String SERVICE_NAME = "musicservice";
    final String SERVICE_PACKAGE = "com.example.clipserver";

    private IntentFilter filter;
    private Receiver1 receiver;
    private static final String AUDIO_INTENT = "edu.uic.cs478.s19.kaboom.audio";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_track);

        //Initializing views
        btnPauseResume = findViewById(R.id.pauseResumeBtn);
        btnStartService = findViewById(R.id.startServiceBtn);
        btnStopService = findViewById(R.id.stopServiceBtn);
        btnPlay = findViewById(R.id.playBtn);
        btnStop = findViewById(R.id.stopBtn);
        radioTrack = findViewById(R.id.radioTrack);

        //Create explicit intent to connect to the service
        i = new Intent(SERVICE_NAME);
        i.setPackage(SERVICE_PACKAGE);

        //enable broadcast receiver to recieve message from service
        filter = new IntentFilter(AUDIO_INTENT) ;
        filter.setPriority(1) ;
        receiver = new Receiver1() ;
        registerReceiver(receiver, filter) ;

        enableDisableViews();

        //Start Service
        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isServiceStarted) {
                    startForegroundService(i);
                    isServiceStarted = true;
                    Log.i(TAG,"Pressed start button");
                }else{
                    Toast.makeText(AudioTrackActivity.this, "Service is already running",Toast.LENGTH_SHORT).show();;
                }
            }
        });

        //Stop Service
        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceStarted) {
                    try {
                        if (calService.stopMusic(selectedTrack)) {
                            songStatus = MUSIC_STOP;
                            enableDisableViews();
                            Log.i(TAG,"Pressed stop button");
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    unbindService(connection);
                    calService = null;
                    isBound = false;
                    stopService(i);
                    songStatus = 0;
                    enableDisableViews();
                    isServiceStarted = false;
                    Log.i(TAG,"Pressed stop service button");
                }else{
                    Toast.makeText(AudioTrackActivity.this, "Service isn't running",Toast.LENGTH_SHORT).show();;
                }
            }
        });

        //Play Music by Binding the service
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceStarted) {
                    if(null == calService)
                        bindService();
                    Log.i(TAG,"Pressed play service button");
                }else{
                    Toast.makeText(AudioTrackActivity.this, "Service isn't running",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Pause || Resume Music
        btnPauseResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceStarted) {
                    if(isBound) {
                        //To pause music
                        if (songStatus == MUSIC_PLAY) {
                            try {
                                if (calService.pauseMusic(selectedTrack)) {
                                    songStatus = MUSIC_PAUSE;
                                    enableDisableViews();
                                    Log.i(TAG,"Pressed pause button");
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            //To resume music
                        } else if (songStatus == MUSIC_PAUSE) {
                            try {
                                if (calService.resumeMusic(selectedTrack)) {
                                    songStatus = MUSIC_RESUME;
                                    enableDisableViews();
                                    Log.i(TAG,"Pressed resume button");
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }else
                        Toast.makeText(AudioTrackActivity.this, "Service is not Bound. Please restart service.",Toast.LENGTH_SHORT).show();;
                }else{
                    Toast.makeText(AudioTrackActivity.this, "Service isn't running",Toast.LENGTH_SHORT).show();;
                }
            }
        });

        //Stop Music by Un-Binding the service
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceStarted) {
                    if(isBound) {
                        try {
                            if (calService.stopMusic(selectedTrack)) {
                                songStatus = MUSIC_STOP;
                                enableDisableViews();
                                Log.i(TAG,"Pressed stop button");
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        unbindService(connection);
                        calService = null;
                        isBound = false;
                    }else
                        Toast.makeText(AudioTrackActivity.this, "Service is not bound. Please restart service.",Toast.LENGTH_SHORT).show();;
                }
                else{
                    Toast.makeText(AudioTrackActivity.this, "Service isn't running",Toast.LENGTH_SHORT).show();;
                }
            }
        });
    }

    public void playMusicTrack(){
        if(isBound) {
            selectedTrack = radioTrack.indexOfChild(radioTrack.findViewById(radioTrack.getCheckedRadioButtonId()));
            try {
                if (calService.playMusic(selectedTrack)) {
                    songStatus = MUSIC_PLAY;
                    enableDisableViews();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        else
            Toast.makeText(AudioTrackActivity.this, "Service is Bound. Restart Service",Toast.LENGTH_SHORT).show();;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    public void bindService(){
        bindService(i, connection, Context.BIND_AUTO_CREATE);
    }

    public void enableDisableViews(){
        switch (songStatus){
            case MUSIC_PLAY:{
                btnPlay.setEnabled(false);
                btnPauseResume.setEnabled(true);
                btnStop.setEnabled(true);
                //radioTrack.setEnabled(false);
                enableDisableRadioGroup(radioTrack,false);
                break;
            }
            case MUSIC_PAUSE:{
                btnPlay.setEnabled(false);
                btnPauseResume.setText("Resume");
                btnStop.setEnabled(true);
                //radioTrack.setEnabled(false);
                enableDisableRadioGroup(radioTrack,false);
                break;
            }
            case MUSIC_RESUME:{
                btnPlay.setEnabled(false);
                btnPauseResume.setText("Pause");
                btnStop.setEnabled(true);
                //radioTrack.setEnabled(false);
                enableDisableRadioGroup(radioTrack,false);
                break;
            }
            case MUSIC_STOP:{
                btnPlay.setEnabled(true);
                btnPauseResume.setEnabled(false);
                btnStop.setEnabled(false);
                //radioTrack.setEnabled(true);
                enableDisableRadioGroup(radioTrack,true);
                break;
            }
            default:{
                btnPlay.setEnabled(true);
                btnPauseResume.setEnabled(false);
                btnStop.setEnabled(false);
                //radioTrack.setEnabled(true);
                enableDisableRadioGroup(radioTrack,true);
                break;
            }
        }
    }

    private void enableDisableRadioGroup(RadioGroup rG,boolean action){
        for (int i = 0; i < rG.getChildCount(); i++) {
            ((RadioButton) rG.getChildAt(i)).setEnabled(action);
        }
    }

    //Creating service connection
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            calService = ICalService.Stub.asInterface(service);
            isBound = true;
            Log.i(TAG, "Service Connected");
            playMusicTrack();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            calService = null;
            isBound = false;
            Log.i(TAG, "Service Disconnected");
            Toast.makeText(AudioTrackActivity.this, "Service was disconnected.",Toast.LENGTH_SHORT).show();;
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Toast.makeText(AudioTrackActivity.this, "Binding to service died..",Toast.LENGTH_SHORT).show();;
        }
    };


    //Using broadcast receiver to manage the case when song is completed and the music is stopped by itself
    //The broadcast receiver send broadcast message which is received by audio client
    public class Receiver1 extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Audio Client received song completion message form Servier") ;

            if(intent.getBooleanExtra("SongCompletion",false)){
                try {
                    if (calService.stopMusic(selectedTrack)) {
                        songStatus = MUSIC_STOP;
                        enableDisableViews();
                        Log.i(TAG,"Song Completed");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                unbindService(connection);
                calService = null;
                isBound = false;
            }
        }
    }
}
