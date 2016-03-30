package com.ribbit.android.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.Params;
import com.ribbit.R;

import com.ribbit.android.CustomClasses.AnimatedEditText;
import butterknife.Bind;
import butterknife.ButterKnife;


public class LoginActivity extends Activity {

    @Bind(R.id.login_logo) ImageView logo;
    @Bind(R.id.login_mountains) ImageView mountains;
    @Bind(R.id.login_stars) ImageView stars;
    @Bind(R.id.login_username)
    AnimatedEditText usernameField;
    @Bind(R.id.login_password) AnimatedEditText passwordField;
    @Bind(R.id.login_register)
    Button registerButton;
    @Bind(R.id.login_sign_in) Button loginButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(LoginActivity.this);

        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //check if user is logged in already
        if (ParseUser.getCurrentUser() != null)
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        else {
            new JobManager(this).addJobInBackground(new LoginAnimationsJob());
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkConnection()) {
                        if (checkFields()) {//check if fields are filled
                            attemptLogin();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_login_large);
    }

    public void animateLoginScreen(){
        final Animation animTranslate = AnimationUtils.loadAnimation(this,R.anim.translate);
        Animation mountainAnimation = AnimationUtils.loadAnimation(this,R.anim.translate_mountains);

        stars.startAnimation(animTranslate);
        mountains.startAnimation(mountainAnimation);
        mountainAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                YoYo.with(Techniques.Wobble).playOn(logo);
                usernameField.setVisibility(View.VISIBLE);
                passwordField.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void attemptLogin(){
        ParseUser.logInInBackground(usernameField.getText().toString().toLowerCase(), passwordField.getText().toString(), new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                } else {
                    e.printStackTrace();
                    YoYo.with(Techniques.Shake).playOn(usernameField);
                    YoYo.with(Techniques.Shake).playOn(passwordField);
                    Toast.makeText(getApplicationContext(), "Username or Password is invalid", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    public boolean checkConnection(){
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        final boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public boolean checkFields(){
        if (usernameField.getText().toString().isEmpty()){
            YoYo.with(Techniques.Shake).playOn(usernameField);
            YoYo.with(Techniques.Flash).playOn(usernameField);
            return false;
        }
        if (passwordField.getText().toString().isEmpty()){
            YoYo.with(Techniques.Shake).playOn(passwordField);
            YoYo.with(Techniques.Flash).playOn(passwordField);
            return false;
        }
        else
            return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class LoginAnimationsJob extends Job {
        public static final int PRIORITY = 1;

        protected LoginAnimationsJob() {

            super(new Params(PRIORITY));
        }

        @Override
        public void onAdded() {

        }

        @Override
        public void onRun() throws Throwable {
            LoginActivity.this.animateLoginScreen();
        }

        @Override
        protected void onCancel() {

        }

        @Override
        protected boolean shouldReRunOnThrowable(Throwable throwable) {
            return false;
        }
    }
}


