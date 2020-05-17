package com.nikhil.clock.timer.Fragment;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.nikhil.clock.timer.Activity.MainActivity;
import com.nikhil.clock.timer.Dialog.AddTimerDialog;
import com.nikhil.clock.timer.R;

import me.tankery.lib.circularseekbar.CircularSeekBar;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * A simple {@link Fragment} subclass.
 */
public class TimerFragment extends Fragment implements Chronometer.OnChronometerTickListener, View.OnTouchListener,View.OnClickListener {
    private CircularSeekBar circularSeekBar;
    private Button btnStart;
    private TextView tvReset, tvaddTimer,tvDialogSet,tvTimer;
    private Animation blinkAnimation;
    private Vibrator vibe;
    private LinearLayout llBottom;
    private int progressPercentage;
    private NumberPicker npMin, npSec;
    //orientation true for landscape false for portrait
    //flag true if lap button is  ever pressed false when it is not
    public static boolean running,orientation,flag = false;
    private Dialog addTimerDialog;
    //for countDownTimer
    private  CountDownTimer countDownTimer;
    //timeremaining variable can be use to save pause stage time
    private static long totalTime, timeRemaining;
    private NumberFormat f;
    private boolean isReset = false;
    public TimerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_timer, container, false);
        if(savedInstanceState == null) {
            initViews(view);
            initAnimation();
            initListeners();
            initVibration(view);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.fragment_timer_btn_start:
                if (totalTime==0)
                {
                    vibe.vibrate(40);
                    break;
                }
                if (!running)
                {

                    btnStart.setBackground(getResources().getDrawable(R.drawable.pause_btn_bg));
//                    chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                    setCountDown(timeRemaining);
                    running = true;
                    vibe.vibrate(40);
                    break;
                }
                if (running)
                {
                    btnStart.setBackground(getResources().getDrawable(R.drawable.play_btn_bg));
                    running = false;
                    countDownTimer.cancel();
                    vibe.vibrate(40);
                    break;
                }
                break;

            case R.id.fragment_timer_tv_reset:
                if (countDownTimer!=null) {
                    countDownTimer.cancel();
                }
                timeRemaining = 0;
                totalTime = 0;
                circularSeekBar.setProgress(0);
                tvTimer.setText("00:00");
                tvTimer.clearAnimation();
                tvReset.clearAnimation();
                btnStart.setVisibility(View.VISIBLE);
                tvaddTimer.setVisibility(View.VISIBLE);
                btnStart.setBackground(getResources().getDrawable(R.drawable.play_btn_bg));
                circularSeekBar.setProgress(0);
                flag = false;
                running = false;
                vibe.vibrate(40);
                break;

            case R.id.fragment_timer_tv_add_timer:
                tvTimer.setVisibility(View.INVISIBLE);
                openDialog();
                flag = true;
                vibe.vibrate(40);
                break;
        }
    }


    @Override
    public void onChronometerTick(Chronometer chronometer) {

        circularSeekBar.setProgress((int)progressPercentage);
    }

    //disabling the movement of seekbar
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

    //handling orientation
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            orientation = true;
            addConstraints(getView());
            setMargins(circularSeekBar,0,0,0,5);
            setMargins(MainActivity.navigationBar,0,0,0,0);
            setMargins(llBottom,0,0,0,10);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            orientation = false;
            setMargins(MainActivity.navigationBar,0,20,0,0);
            setMargins(llBottom,0,0,0,40);
        }
    }

    private void initViews(View view) {
        circularSeekBar = view.findViewById(R.id.fragment_timer_csb);
        btnStart = view.findViewById(R.id.fragment_timer_btn_start);
        tvReset = view.findViewById(R.id.fragment_timer_tv_reset);
        tvaddTimer = view.findViewById(R.id.fragment_timer_tv_add_timer);
        tvTimer = view.findViewById(R.id.fragment_timer_tv_timer);
        llBottom = view.findViewById(R.id.fragment_timer_ll);
    }
    private void initAnimation() {
        blinkAnimation = AnimationUtils.loadAnimation(getContext(),R.anim.blink);
    }


    private void initListeners() {
        circularSeekBar.setOnTouchListener(this);
        btnStart.setOnClickListener(this);
        tvReset.setOnClickListener(this);
        tvaddTimer.setOnClickListener(this);
    }

    private void initVibration(View view) {
        vibe = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }


    private void openDialog() {
//        AddTimerDialog addTimerDialog = new AddTimerDialog();
//        addTimerDialog.show(MainActivity.fragmentManager,"Add Timer");
        addTimerDialog = new Dialog(getContext());
        addTimerDialog.setContentView(R.layout.add_timer);
        addTimerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        tvDialogSet = (TextView)addTimerDialog.findViewById(R.id.layout_add_timer_tv_set);
        npMin = (NumberPicker) addTimerDialog.findViewById(R.id.layout_add_timer_np_min);
        npSec = (NumberPicker)addTimerDialog.findViewById(R.id.layout_add_timer_np_sec);

        npMin.setMaxValue(60);
        npMin.setMinValue(0);
        npSec.setMinValue(0);
        npSec.setMaxValue(59);
        addTimerDialog.setCancelable(false);
        addTimerDialog.show();
        tvDialogSet.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                int min = npMin.getValue();
                int sec = npSec.getValue();
                totalTime = ((min*60)+sec)*1000;
                timeRemaining = totalTime;
                f = new DecimalFormat("00");
                tvTimer.setVisibility(View.VISIBLE);
                tvTimer.setText(f.format(min) + ":" + f.format(sec));
                addTimerDialog.cancel();
                running = false;
                isReset = false;


            }
        });

    }

    //adding and removing constraints of Seekbar on Lap button Click
    private void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }
    private void removeConstraints(View view) {
        ConstraintSet set = new ConstraintSet();
        ConstraintLayout layout;
        layout = (ConstraintLayout) view.findViewById(R.id.fragment_timer_cl);
        set.clone(layout);
        set.clear(R.id.fragment_timer_csb, ConstraintSet.BOTTOM);
        // set.connect(R.id.bottomText, ConstraintSet.TOP, R.id.imageView, ConstraintSet.BOTTOM, 0);
        set.applyTo(layout);
    }
    private void addConstraints(View view) {
        ConstraintSet set = new ConstraintSet();
        ConstraintLayout layout;
        layout = (ConstraintLayout) view.findViewById(R.id.fragment_timer_cl);
        set.clone(layout);
        //set.clear(R.id.activity_main_csb, ConstraintSet.BOTTOM);
        set.connect(R.id.fragment_timer_csb, ConstraintSet.BOTTOM,R.id.fragment_timer_ll, ConstraintSet.TOP, 0);
        set.applyTo(layout);
    }
    private void setCountDown(long millisInFuture )
    {
        countDownTimer = new CountDownTimer(millisInFuture,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long min = (millisUntilFinished / 60000) % 60;
                long sec = (millisUntilFinished / 1000) % 60;
                tvTimer.setText(f.format(min) + ":" + f.format(sec));
                timeRemaining = millisUntilFinished;
                progressPercentage = (int)(((double)timeRemaining/(double)totalTime)*100);
                circularSeekBar.setProgress(100-progressPercentage);


            }

            @Override
            public void onFinish() {
                circularSeekBar.setProgress(100);
                running = false;
                tvTimer.setText("Time Up!");
                tvTimer.startAnimation(blinkAnimation);
                tvReset.startAnimation(blinkAnimation);
                timeRemaining = 0;
                btnStart.setVisibility(View.INVISIBLE);
                tvaddTimer.setVisibility(View.INVISIBLE);

            }
        };
        countDownTimer.start();
    }


}
