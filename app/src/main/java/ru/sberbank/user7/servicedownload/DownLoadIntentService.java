package ru.sberbank.user7.servicedownload;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationBuilderWithBuilderAccessor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class DownLoadIntentService extends IntentService {
    public static final String EXTRA_FILENAME = "file_name";
    public static final String ACTION_STATECHANGED = "downloadStateChanged" ;

    private boolean complited=false;
    private boolean withErrors = false;
    private int progress = 0;
    private final int NOTIFICATIONID =5;
    private boolean foregroundMode;

    public DownLoadIntentService() {
        super("DownLoadIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.e("OnHandleIntent","start");
        URL url = null;
        try {
            url = new URL("http://pikabu.ru/story/oboi_na_rabochiy_stol__mrachnyiy_les_3618730");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String fileName = intent.getStringExtra(EXTRA_FILENAME);

        try {
            DownLoadFile(url, fileName);
            withErrors = false;
            progress = 100;
        } catch (IOException e) {
            withErrors = true;
            progress = 0;
            e.printStackTrace();
        }finally {
            complited = true;
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    private void DownLoadFile(URL url, String fileName) throws IOException{
        Log.e("DownLoadFile","start");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        Log.e("DownLoadFile","openConnection");
        connection.connect();
        int size = connection.getContentLength();
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        Log.e("DownLoadFile",   "Directory = " + directory);
        directory.mkdir();
        File file = new File(directory, fileName);
        BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));

        Log.e("DownLoadFile", "size of content =" + String.valueOf(size));
        byte[] buffer = new byte[8096];
        int readed;
        int downloaded=0;
        while ((readed= inputStream.read(buffer))>0){
            outputStream.write(buffer,0,readed);
            downloaded += readed;
            int percent = calculatrPercent(size, downloaded);
            if (percent!=progress){
                notifyStateChanged();

             //   Notification notification = buildNotification();
          //      NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                nm.notify(NOTIFICATIONID, notification);
                //
            }
        }
        Log.e("DownLoadFile","finish");
        outputStream.flush();
        outputStream.close();
        connection.disconnect();

    }

    private int calculatrPercent(int total, int current){

        Log.e("CalculatePercent","finish");
        int onePercent = Math.max(1,total/100);
        return Math.min(current/onePercent,100);
    }

    private void notifyStateChanged(){

        Log.e("NotifyState","finish");
        Intent data = new Intent(ACTION_STATECHANGED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(data);
    }

    public class LocalBinder extends Binder {

        public boolean isComlited(){
            return complited;
        }
        public boolean hasError(){
            return withErrors;
        }
        public int getProgress(){
            return progress;
        }
    }
    private Notification buildNotification(){
        Notification.Builder builder = new Notification.Builder(this);
        //builder.setSmallIcon(R.drawable.);
       //         builder.setProgress(100, );
        builder.setContentTitle("loading");
        return builder.build();

    }
    public void setForeground1(boolean foreground){
        if (foreground){
            startForeground(NOTIFICATIONID, buildNotification());
        }else{stopForeground(true);}
    }
}
