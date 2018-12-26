package com.demgames.polypong;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.esotericsoftware.kryonet.Server;

public class Game_Selection extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game__selection);

        final Button klassisch_weiter = (Button) findViewById(R.id.klassisch_weiter);
        final LinearLayout klassisch = (LinearLayout) findViewById(R.id.klassisch_layout);
        final LinearLayout pong = (LinearLayout) findViewById(R.id.pongLayout);

        final LinearLayout klassischInfo = (LinearLayout) findViewById(R.id.expandable_klassisch);
        final LinearLayout pongInfo = (LinearLayout) findViewById(R.id.pongInfo);

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
                    expand(klassischInfo);
                }
                else if (klassischInfo.getVisibility()==View.VISIBLE){
                    collapse(klassischInfo);
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
                    expand(pongInfo);
                }
                else if (pongInfo.getVisibility()==View.VISIBLE){
                    collapse(pongInfo);
                }
                else{
                    Log.d("Expand", "onClick: "+pongInfo.getVisibility());
                }
            }
        });

        klassischInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("klassisch", "onClick: "+klassischInfo.getVisibility());
                if (klassischInfo.getVisibility()==View.GONE){
                    expand(klassischInfo);
                }
                else if (klassischInfo.getVisibility()==View.VISIBLE){
                    collapse(klassischInfo);
                }
                else{
                    Log.d("Expand", "onClick: "+klassischInfo.getVisibility());
                }


            }
        });

        pongInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("klassisch", "onClick: "+pongInfo.getVisibility());
                if (pongInfo.getVisibility()==View.GONE){
                    expand(pongInfo);
                }
                else if (pongInfo.getVisibility()==View.VISIBLE){
                    collapse(pongInfo);
                }
                else{
                    Log.d("Expand", "onClick: "+pongInfo.getVisibility());
                }
            }
        });

    }


    private void expand(final LinearLayout expand){

        expand.setVisibility(View.VISIBLE);
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        expand.measure(widthSpec, heightSpec);

        ValueAnimator mAnimator = slideAnimator(0, expand.getMeasuredHeight(), expand);
        mAnimator.start();
        ImageView downarrow = (ImageView) findViewById(R.id.downarrow);
        Drawable uparrow = getResources().getDrawable(android.R.drawable.arrow_up_float);
        downarrow.setImageDrawable(uparrow);

    }


    private void collapse(final LinearLayout collapse){
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
        ImageView downarrow = (ImageView) findViewById(R.id.downarrow);
        Drawable uparrow = getResources().getDrawable(android.R.drawable.arrow_down_float);
        downarrow.setImageDrawable(uparrow);

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