package ru.sberbank.user7.servicedownload;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class MainActivity extends Activity {

    private DownLoadIntentService.LocalBinder binder;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (DownLoadIntentService.LocalBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
        }
    };
    Button downLoadButton;
    ProgressBar progressBar;
    BroadcastReceiver changesReciever ;
    @Override

    protected void onCreate(Bundle savedInstanceState) {

        Log.e("OnCreate","Start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        progressBar.setMax(100);
        downLoadButton = (Button) findViewById(R.id.buttonDownLoad);
        changesReciever =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (DownLoadIntentService.ACTION_STATECHANGED.equals(intent.getAction())){
                    displayState();
                }
            }
        };


        downLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("OnClick","Start");
                if (PermissionChecker.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 13);
                }else{
                    startDownLoad();}
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.e("OnStart","Start");
        Intent service = new Intent(this, DownLoadIntentService.class);

        bindService(service, serviceConnection, 0);

        IntentFilter filter = new IntentFilter(DownLoadIntentService.ACTION_STATECHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(changesReciever,filter);

    }
    public void startDownLoad(){

        Log.e("StartDownLoad","start");
        Intent service = new Intent(MainActivity.this, DownLoadIntentService.class);
        service.setData(Uri.parse("https://www.google.ru/search?biw=1600&bih=794&tbm=isch&sa=1&q=%2Cjkmifz+rfhnbyrf&oq=%2Cjkmifz+rfhnbyrf&gs_l=img.3...5714.8410.0.8943.0.0.0.0.0.0.0.0..0.0....0...1.1.64.img..0.0.0.RRDvpHpVqk0#imgrc=wNVIjm17CRHtMM:"));


        service.putExtra(DownLoadIntentService.EXTRA_FILENAME, "android.jpeg" );
        startService(service);
        bindService(service, serviceConnection,0);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("OnRequesResult","start");
        if(permissions.length>0&&grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startDownLoad();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (binder!=null){
         //   binder.setForeground();
            unbindService(serviceConnection);
    }

    LocalBroadcastManager.getInstance(this).unregisterReceiver(changesReciever);
    }



    private void displayState(){
        if (binder==null){
            downLoadButton.setEnabled(true);
            progressBar.setProgress(0);
        }else{
            downLoadButton.setEnabled(false);
            progressBar.setProgress(binder.getProgress());
            if (binder.isComlited()){
                unbindService(serviceConnection);
                binder=null;
                downLoadButton.setEnabled(true);
            }
        }
    }

}
