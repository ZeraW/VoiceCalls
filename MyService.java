package gmsproduction.com.voicecalls;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;

public class MyService extends Service {
    private NotificationManager notificationManager;
    private boolean isShowingForegroundNotification;

    private static final String APP_KEY = "b2bc33d0-d001-4cb0-9543-5dd89cac35d0";
    private static final String APP_SECRET = "Mo4e3WQKL0KY05ugwWClxg==";
    private static final String ENVIRONMENT = "clientapi.sinch.com";

    private Call call;
    private TextView callState;
    private SinchClient sinchClient;
    private String callerId = "hihi";
    private String recipientId = "byebye";


    public MyService() {
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "onCreate Called", Toast.LENGTH_SHORT).show();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        myOnCreade();

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Toast.makeText(this, "onStartCommand Called", Toast.LENGTH_SHORT).show();
        if (isShowingForegroundNotification) {
            stopImportantJob();
            stopSelf();//you have to stop it still, it is not enough with stopforeground
        } else {
            doImportatJob();
        }
        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "onDestroy Called", Toast.LENGTH_SHORT).show();
        notificationManager.cancelAll();

        if (isShowingForegroundNotification)
            stopImportantJob();
    }
    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "onUnbind Called", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }
    @Override
    public void onRebind(Intent intent) {
        Toast.makeText(this, "onRebind Called", Toast.LENGTH_SHORT).show();
        super.onRebind(intent);
    }
    void doImportatJob() {
        //...  perform important job
        //make this service a foreground service, so it will be as important as the Activity
        PendingIntent contentIntent =
                PendingIntent.getActivity(this, 0,
                        new Intent(this, MainActivity.class), 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("This notification is from a foreground service")
                .setContentText("Touch to open activity handling this service")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Starting up!!!")
                .setContentIntent(contentIntent)
                .setOngoing(false) //Always true in startForeground
                .build();
        startForeground(1992, notification); //notification can not be dismissed until detached,// or stopped service or stopForeground()
        isShowingForegroundNotification = true;
    }
    private void stopImportantJob() {
        //... Stop your work
        //notificationManager.cancel(1992); Will not work in the notification started with startForeground
        //notificationManager.cancelAll(); neither
        stopForeground(true);
        isShowingForegroundNotification = false;
        if (false) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH); //now you can dismiss the notification
                stopForeground(STOP_FOREGROUND_REMOVE);
            }
        }
    }

    private void myOnCreade(){
        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(callerId)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.start();
        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());


    }


    private void hungUpBtn(){
        call.hangup();
    }

    private class SinchCallListener implements CallListener {
        @Override
        public void onCallEnded(Call endedCall) {
            call = null;
            SinchError a = endedCall.getDetails().getError();
            //setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        }

        @Override
        public void onCallEstablished(Call establishedCall) {
            //setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallProgressing(Call progressingCall) {
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
        }
    }
    private class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            call = incomingCall;
            //Toast.makeText(MainActivity.this, "incoming call", Toast.LENGTH_SHORT).show();
            call.answer();

            call.addCallListener(new SinchCallListener());
        }
    }


}
