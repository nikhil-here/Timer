package com.nikhil.clock.timer.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.le.ScanRecord;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.nikhil.clock.timer.Fragment.StopwatchFragment;
import com.nikhil.clock.timer.Fragment.TimerFragment;
import com.nikhil.clock.timer.R;

import java.sql.Time;

public class MainActivity extends AppCompatActivity implements View.OnSystemUiVisibilityChangeListener,ChipNavigationBar.OnItemSelectedListener{
    public static FragmentManager fragmentManager;
    public static ChipNavigationBar navigationBar;
    private View decorView;
    private final String CHANNEL_ID = "clock_notification";
    private final int NOTIFICATION_ID = 001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null)
        {
            initViews();
            initListeners();
            fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(R.id.activity_main_fl_fragment_container,new StopwatchFragment(),null).commit();

        }

    }
    @Override
    public void onItemSelected(int i) {
        switch (i)
        {
            case R.id.stopwatch:
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_right_to_left,R.anim.exit_right_to_left).replace(R.id.activity_main_fl_fragment_container,new StopwatchFragment(),null).commit();
                break;
            case R.id.timer:
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_left_to_right,R.anim.exit_left_to_right).replace(R.id.activity_main_fl_fragment_container,new TimerFragment(),null).commit();
                break;
        }

    }
    //notification on activity pause
    @Override
    protected void onPause() {
        super.onPause();
        if(StopwatchFragment.running | TimerFragment.running) {
            displayNotification();
        }
    }
    //removing navigation buttons (on-screen buttons)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
        {
            decorView.setSystemUiVisibility(hideSystemBars());
        }
    }
    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        if(visibility == 0)
        {
            decorView.setSystemUiVisibility(hideSystemBars());
        }
    }
    private int hideSystemBars()
    {
        int i = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        return i;
    }

    private void initViews() {
        navigationBar = findViewById(R.id.activity_main_nb);
        decorView = getWindow().getDecorView();
    }
    private void initListeners() {
        //selecting stopwatch menu by default
        navigationBar.setItemSelected(R.id.stopwatch,true);
        navigationBar.setOnItemSelectedListener(this);
    }

    private void displayNotification() {
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_stopwatch);
        builder.setContentTitle("Clock Running in Background");
        builder.setContentText("Tap to Resume");
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setAutoCancel(true);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        Intent notificationIntent = new Intent(this.getBaseContext(), MainActivity.class);
        //specifying an action and its category to be triggered once clicked on the notification
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setAction("android.intent.action.MAIN");
        resultIntent.addCategory("android.intent.category.LAUNCHER");
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }
    private void createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            CharSequence name = "clock_notification";
            String description = "include all clock notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,name,importance);
            notificationChannel.setDescription(description);
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }


}
