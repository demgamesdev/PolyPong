package com.demgames.polypong;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class Splashscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splashscreen);

        TextView DemGames = (TextView) findViewById(R.id.SplashLogo);
        ImageView Logo = (ImageView) findViewById(R.id.splashLogoImage);

        Animation transition = AnimationUtils.loadAnimation(this,R.anim.transition);
        DemGames.startAnimation(transition);
        Logo.startAnimation(transition);

        final MediaPlayer splashSound = MediaPlayer.create(this, R.raw.splash_sound);
        //splashSound.start();
        final Intent startmain = new Intent(getApplicationContext(), MainActivity.class);


        Thread timer = new Thread(){
            public void  run(){
                try {
                    sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    startActivity(startmain);
                    finish();
                }
            }
        };
        timer.start();
    }
}
