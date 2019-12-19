package com.example.clipserver;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import androidx.annotation.Nullable;


public class AudioTrackService extends Service {

    //Declaring Media Player Instance
    MediaPlayer mediaPlayer;
    int pauseLength=0;

    //Array of Music files
    int[] musicArray = {R.raw.track1, R.raw.track2, R.raw.track3, R.raw.track4, R.raw.track5, R.raw.track6};

    final String TAG = "Clip Server";

    private static final String AUDIO_INTENT = "edu.uic.cs478.s19.kaboom.audio";
    private static final String KABOOM_PERMISSION = "edu.uic.cs478.s19.kaboom";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG,"Start Command was called");
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG,"Service has been binded");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG,"Service has been Un-binded");
        return super.onUnbind(intent);
    }

    private final ICalService.Stub binder = new ICalService.Stub() {
        @Override
        public int getResult(int val1, int val2) throws RemoteException {
            return val1 * val2;
        }

        @Override
        public boolean playMusic(int selectedTrack) throws RemoteException {
            try{
                mediaPlayer = MediaPlayer.create(AudioTrackService.this, musicArray[selectedTrack]);
                mediaPlayer.setOnCompletionListener(listener);
                mediaPlayer.start();
                return true;
            }catch(Exception ex){
                Log.e(TAG,"Error: " + ex.getMessage());
            }
            return false;
        }

        //send broadcast message to the client to unbind music after completion of song
        public MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Intent aIntent = new Intent(AUDIO_INTENT) ;
                aIntent.putExtra("SongCompletion", true);
                sendOrderedBroadcast(aIntent,KABOOM_PERMISSION) ;
            }
        };

        @Override
        public boolean pauseMusic(int selectedTrack) throws RemoteException {
            try{
                mediaPlayer.pause();
                pauseLength = mediaPlayer.getCurrentPosition();
                return true;
            }catch(Exception ex){
                Log.e(TAG,"Error: " + ex.getMessage());
            }
            return false;
        }

        @Override
        public boolean stopMusic(int selectedTrack) throws RemoteException {
            try{
                mediaPlayer.stop();
                mediaPlayer = null;
                return true;
            }catch(Exception ex){
                Log.e(TAG,"Error: " + ex.getMessage());
            }
            return false;
        }

        @Override
        public boolean resumeMusic(int selectedTrack) throws RemoteException {
            try{
                mediaPlayer.seekTo(pauseLength);
                mediaPlayer.start();
                return true;
            }catch(Exception ex){
                Log.e(TAG,"Error: " + ex.getMessage());
            }
            return false;
        }

        @Override
        public String getMessage(String name) throws RemoteException {
            return "Hello "+ name+", Result is:";
        }
    };
}
