# Music Player - Bound Services in Android 

The project aims at demonstarting the usage of the concepts of Bound Services in Android. The application compries of a Music player client and Service to play music. The client UI comprises of the list of tracks and controls to play, pause, resume and stop a track.

## Bound Services in Android
A bound service is the server in a client-server interface. It allows components (such as activities) to bind to the service, send requests, receive responses, and perform interprocess communication (IPC). A bound service typically lives only while it serves another application component and does not run in the background indefinitely. To know more about bound services in Android please follow the [link.](https://developer.android.com/guide/components/bound-services)

## About the Music Player
The music player application comprises of two parts - a client app and a server service. The client app UI contains the list of tracks and music control buttons. The UI has the following components:
+ Button to Start the Music Service.
+ Button to Stop the Music Service.
+ A Radio Button Group to select a track from the list of tracks.
+ Button to play a selected track.
+ Button to pause a track being played.
+ Button to resume a paused track.
+ Button to stop a track being played.

The other part of the project is Music Service. The music service plays in background and has the music track stored in it and performs operations as per the requests from the client application.
## Implementation
#### Music Service
The music service has been implemented as bound service. In order to establish communication between the music service and the client application an AIDL interface has been implemented. The AIDL interface declares the methods which are exposed to the client application. Since it's a bound service, we define a stub in the ```onBind()``` callback method. The interface exposes the methods ```playMusic(), pauseMusic(), stopMusic(), resumeMusic()``` of the music server which are invoked by the client application.
#### Client Application
The client application connects to the music server using the AIDL interfaces. The client application consists of buttons to perform operations pertaining to starting/stopping the music service and playing/pausing/resuming/stopping a music track. On pressing the ***Start Service*** button, the ```startService()``` method is invoked. On pressing the play button, the ```bindService()``` method is invoked. On stopping a music, the ```unBindService()``` method is invoked which unbinds the applciation from the msuic server. The buttons on the UI are greyed out to disable and enable them corresponding to the actions performed by the user.

The appliciation also uses Broadcast Receivers to establish communication between the application and music service to ensure that the controls on the UI are reset as soon a song completes. The permission ***edu.uic.cs478.s19.kaboom*** for the boradcast receiver is defined in the music service and is used by the client application.

## Setting up the environment
After cloning the repository, import the project into Android Studio. Setup an emulator using AVD manager. The application supports API level 28 (Pie) and above. The application can then be launched directly and does not require any permission form the user.
