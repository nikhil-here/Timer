package com.nikhil.clock.timer.Fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.nikhil.clock.timer.Activity.MainActivity;
import com.nikhil.clock.timer.R;

import me.tankery.lib.circularseekbar.CircularSeekBar;

/**
 * A simple {@link Fragment} subclass.
 */
public class StopwatchFragment extends Fragment implements Chronometer.OnChronometerTickListener, View.OnTouchListener,View.OnClickListener {
    private Chronometer chronometer;
    private CircularSeekBar circularSeekBar;
    private ScrollView scrollView;
    private Button btnStart;
    private TextView tvReset, tvLap, tvLapTime;
    private Animation blinkAnimation;
    private Vibrator vibe;
    private LinearLayout llBottom;
    private long pauseOffset,progress;
    private double progressPercentage;
    //orientation true for landscape false for portrait
    //flag true if lap button is  ever pressed false when it is not
    public static boolean running,orientation,flag = false;
    private int lapCount = 0;

    public StopwatchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stopwatch, container, false);
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
            case R.id.fragment_stopwatch_btn_start:
                if (!running)
                {
                    chronometer.clearAnimation();
                    btnStart.setBackground(getResources().getDrawable(R.drawable.pause_btn_bg));
                    tvReset.setVisibility(View.VISIBLE);
                    tvLap.setVisibility(View.VISIBLE);
                    chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                    chronometer.start();
                    running = true;
                    vibe.vibrate(40);
                    break;
                }
                if (running)
                {
                    chronometer.startAnimation(blinkAnimation);
                    btnStart.setBackground(getResources().getDrawable(R.drawable.play_btn_bg));
                    tvLap.setVisibility(View.INVISIBLE);
                    pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
                    chronometer.stop();
                    running = false;
                    vibe.vibrate(40);
                    break;
                }
                break;

            case R.id.fragment_stopwatch_tv_reset:
                chronometer.clearAnimation();
                btnStart.setBackground(getResources().getDrawable(R.drawable.play_btn_bg));
                tvReset.setVisibility(View.INVISIBLE);
                tvLap.setVisibility(View.INVISIBLE);
                addConstraints(getView());
                chronometer.stop();
                circularSeekBar.setProgress(0);
                tvLapTime.setText(null);
                chronometer.setBase(SystemClock.elapsedRealtime());
                lapCount = 0;
                pauseOffset = 0;
                flag = false;
                running = false;
                vibe.vibrate(40);
                break;

            case R.id.fragment_stopwatch_tv_lap:
                if (!orientation) {
                    removeConstraints(getView());
                    setMargins(circularSeekBar,0,50,0,0);
                }
                long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                int h   = (int)(time /3600000);
                int m = (int)(time - h*3600000)/60000;
                int s= (int)(time - h*3600000- m*60000)/1000 ;
                String hh = h < 10 ? "0"+h: h+"";
                String mm = m < 10 ? "0"+m: m+"";
                String ss = s < 10 ? "0"+s: s+"";
                lapCount++;
                String newline = "#"+(lapCount < 10 ? "0" : "") + lapCount+"   "+hh+":"+mm+":"+ss;
                tvLapTime.append("\n"+newline);
                scrollView.scrollTo(0,scrollView.getBottom());
                flag = true;
                vibe.vibrate(40);
                break;
        }
    }

    @Override
    public void onChronometerTick(Chronometer chronometer) {
        progress = (SystemClock.elapsedRealtime() - chronometer.getBase());
        progressPercentage = (int)((double)progress/(double)600);
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
            tvLapTime.setVisibility(View.GONE);
            addConstraints(getView());
            setMargins(circularSeekBar,0,0,0,5);
            setMargins(MainActivity.navigationBar,0,0,0,0);
            setMargins(llBottom,0,0,0,10);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            orientation = false;
            tvLapTime.setVisibility(View.VISIBLE);
            if (flag)
            {
                removeConstraints(getView());
                setMargins(circularSeekBar,0,100,0,0);
            }
            setMargins(MainActivity.navigationBar,0,20,0,0);
            setMargins(llBottom,0,0,0,40);
        }
    }

    private void initViews(View view) {
        chronometer = view.findViewById(R.id.fragment_stopwatch_cm);
        circularSeekBar = view.findViewById(R.id.fragment_stopwatch_csb);
        scrollView = view.findViewById(R.id.fragment_stopwatch_sv);
        btnStart = view.findViewById(R.id.fragment_stopwatch_btn_start);
        tvReset = view.findViewById(R.id.fragment_stopwatch_tv_reset);
        tvLap = view.findViewById(R.id.fragment_stopwatch_tv_lap);
        tvLapTime = view.findViewById(R.id.fragment_stopwatch_tv_laptime);
        llBottom = view.findViewById(R.id.fragment_stopwatch_ll);
    }
    private void initAnimation() {
        blinkAnimation = AnimationUtils.loadAnimation(getContext(),R.anim.blink);
    }


    private void initListeners() {
        chronometer.setOnChronometerTickListener(this);
        circularSeekBar.setOnTouchListener(this);
        btnStart.setOnClickListener(this);
        tvReset.setOnClickListener(this);
        tvLap.setOnClickListener(this);
    }

    private void initVibration(View view) {
        vibe = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
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
        layout = (ConstraintLayout) view.findViewById(R.id.fragment_stopwatch_cl);
        set.clone(layout);
        set.clear(R.id.fragment_stopwatch_csb, ConstraintSet.BOTTOM);
        // set.connect(R.id.bottomText, ConstraintSet.TOP, R.id.imageView, ConstraintSet.BOTTOM, 0);
        set.applyTo(layout);
    }
    private void addConstraints(View view) {
        ConstraintSet set = new ConstraintSet();
        ConstraintLayout layout;
        layout = (ConstraintLayout) view.findViewById(R.id.fragment_stopwatch_cl);
        set.clone(layout);
        //set.clear(R.id.activity_main_csb, ConstraintSet.BOTTOM);
        set.connect(R.id.fragment_stopwatch_csb, ConstraintSet.BOTTOM,R.id.fragment_stopwatch_ll, ConstraintSet.TOP, 0);
        set.applyTo(layout);
    }

}
