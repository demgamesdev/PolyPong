package com.demgames.polypong;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.esotericsoftware.kryonet.Server;

public class Game_Selection extends AppCompatActivity {

    int ballnum = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game__selection);

        final Globals globalVariables = (Globals) getApplicationContext();

        final Button klassisch_weiter = (Button) findViewById(R.id.klassisch_weiter);
        final LinearLayout klassisch = (LinearLayout) findViewById(R.id.klassisch_layout);
        final LinearLayout pong = (LinearLayout) findViewById(R.id.pongLayout);

        final LinearLayout klassischInfo = (LinearLayout) findViewById(R.id.expandable_klassisch);
        final LinearLayout pongInfo = (LinearLayout) findViewById(R.id.pongInfo);
        final LinearLayout pongArrow = (LinearLayout) findViewById(R.id.pong_Arrow);


        final ImageView klassischarrow = (ImageView) findViewById(R.id.downarrow);
        final ImageView pongarrow = (ImageView) findViewById(R.id.pongArrow);

        final TextView ballTextView = (TextView) findViewById(R.id.ballcount);

        final SeekBar ballSeekBar = (SeekBar) findViewById(R.id.seekBarBallNum);

        klassischInfo.setVisibility(View.GONE);
        pongInfo.setVisibility(View.GONE);


        final ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 600);


        klassisch_weiter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startServer = new Intent(getApplicationContext(), OptionsActivity.class);
                startActivity(startServer);
                //myThread.stop();

            }
        });


        klassisch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (klassischInfo.getVisibility()==View.GONE){
                    expand(klassischInfo, klassischarrow);
                    collapse(pongInfo, pongarrow);
                }
                else if (klassischInfo.getVisibility()==View.VISIBLE){
                    collapse(klassischInfo, klassischarrow);
                }
                else{
                    Log.d("Expand", "onClick: "+klassischInfo.getVisibility());
                }
            }
        });

        pong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pongInfo.getVisibility()==View.GONE){
                    expand(pongInfo, pongarrow);
                    collapse(klassischInfo, klassischarrow);
                }
                else if (pongInfo.getVisibility()==View.VISIBLE){
                    collapse(pongInfo, pongarrow);
                }
                else{
                    Log.d("Expand", "onClick: "+pongInfo.getVisibility());
                }
            }
        });

        /*klassischInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("klassisch", "onClick: "+klassischInfo.getVisibility());
                if (klassischInfo.getVisibility()==View.GONE){
                    expand(klassischInfo, klassischarrow);
                    collapse(pongInfo, pongarrow);
                }
                else if (klassischInfo.getVisibility()==View.VISIBLE){
                    collapse(klassischInfo, klassischarrow);
                }
                else{
                    Log.d("Expand", "onClick: "+klassischInfo.getVisibility());
                }
            }
        });*/

        pongArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("klassisch", "onClick: "+pongInfo.getVisibility());
                if (pongInfo.getVisibility()==View.GONE){
                    expand(pongInfo, pongarrow);
                    collapse(klassischInfo, klassischarrow);
                }
                else if (pongInfo.getVisibility()==View.VISIBLE){
                    collapse(pongInfo, pongarrow);
                }
                else{
                    Log.d("Expand", "onClick: "+pongInfo.getVisibility());
                }
            }
        });

        ballTextView.setText( getString(R.string.numballs) + Integer.toString(ballnum));

        ballSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ballnum = i+1;
                ballTextView.setText(getString(R.string.numballs) + Integer.toString(ballnum));
                globalVariables.getGameVariables().numberOfBalls=ballnum;
                //Log.d("Game_Selection", "onProgressChanged: Anzahl der Bälle auf " + Integer.toString(globalVariables.getGameVariables().numberOfBalls) + " geändert");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


//Todo Alle Optionen einbauen
// Todo Weitere Spielmodi integrieren


    }


    private void expand(final LinearLayout expand, final ImageView image){

        expand.setVisibility(View.VISIBLE);

        ViewTreeObserver vto = expand.getViewTreeObserver();


        //Display Breite für Messen von Höhe des Layouts wird erfasst
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayWidth = size.x;

        //Höhe von Layout wird gemessen
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(displayWidth, View.MeasureSpec.AT_MOST);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        expand.measure(widthSpec, heightSpec);

        //Animation von Layout expand wird gestartet
        ValueAnimator mAnimator = slideAnimator(0, expand.getMeasuredHeight(), expand);
        mAnimator.start();


        Drawable uparrow = getResources().getDrawable(android.R.drawable.arrow_up_float);
        image.setImageDrawable(uparrow);

    }


    private void collapse(final LinearLayout collapse, final ImageView image){
        int finalHeight = collapse.getHeight();
        ValueAnimator mAnimator = slideAnimator(finalHeight, 0, collapse);
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                //Height=0, but it set visibility to GONE
                collapse.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        mAnimator.start();

        Drawable uparrow = getResources().getDrawable(android.R.drawable.arrow_down_float);
        image.setImageDrawable(uparrow);

    }

    private ValueAnimator slideAnimator(int start, int end, final LinearLayout LayoutAnim) {

        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = LayoutAnim.getLayoutParams();
                layoutParams.height = value;
                LayoutAnim.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }
    }