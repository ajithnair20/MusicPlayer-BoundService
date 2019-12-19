package com.example.clipserver;


interface ICalService {
    String getMessage(String name);
    int getResult(int val1, int val2);
    boolean playMusic(int selectedTrack);
    boolean pauseMusic(int selectedTrack);
    boolean stopMusic(int selectedTrack);
    boolean resumeMusic(int selectedTrack);
}
