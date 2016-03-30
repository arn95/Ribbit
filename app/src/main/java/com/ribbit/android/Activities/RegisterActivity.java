package com.ribbit.android.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ValueAnimator;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.Params;
import com.ribbit.R;
import com.ribbit.android.CustomClasses.AnimatedEditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

public class RegisterActivity extends Activity {

    @Bind(R.id.register_relative_layout) RelativeLayout relativeLayout;
    @Bind(R.id.register_sun) ImageView sun;
    @Bind(R.id.register_button_done)
    Button registerButton;
    @Bind(R.id.register_password)
    AnimatedEditText passwordField;
    @Bind(R.id.register_password_again) AnimatedEditText passwordFieldAgain;
    @Bind(R.id.register_username) AnimatedEditText usernameField;
    @Bind(R.id.register_email) AnimatedEditText emailField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        new JobManager(this).addJobInBackground(new RegisterAnimationsJob());

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConnection()) {
                    if (checkFields()) {
                        if (validatePassword()) {
                            attemptRegister(usernameField.getText().toString(), emailField.getText().toString(), passwordField.getText().toString());
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        usernameField.addTextChangedListener(new RegisterButtonTextWatcher());
        emailField.addTextChangedListener(new RegisterButtonTextWatcher());
        passwordField.addTextChangedListener(new RegisterButtonTextWatcher());
        passwordFieldAgain.addTextChangedListener(new RegisterButtonTextWatcher());
    }

    public void attemptRegister(String username, String email, String password){
        ParseUser newUser = new ParseUser();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                e.printStackTrace();
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            }
        });
    }

    public boolean checkFields(){
        if (usernameField.getText().toString().isEmpty()){
            YoYo.with(Techniques.Shake).playOn(usernameField);
            YoYo.with(Techniques.Flash).playOn(usernameField);
            return false;
        }
        if (emailField.getText().toString().isEmpty()){
            YoYo.with(Techniques.Shake).playOn(emailField);
            YoYo.with(Techniques.Flash).playOn(emailField);
            return false;
        }
        if (passwordField.getText().toString().isEmpty()){
            YoYo.with(Techniques.Shake).playOn(passwordField);
            YoYo.with(Techniques.Flash).playOn(passwordField);
            return false;
        }
        if (passwordFieldAgain.getText().toString().isEmpty()){
            YoYo.with(Techniques.Shake).playOn(passwordFieldAgain);
            YoYo.with(Techniques.Flash).playOn(passwordFieldAgain);
            return false;
        }
        else
            return true;
    }

    public boolean checkConnection(){
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        final boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public boolean validatePassword(){
        if(passwordField.getText().toString().equals(passwordFieldAgain.getText().toString())){
            return true;
        }

        else{
            YoYo.with(Techniques.Shake).playOn(passwordField);
            YoYo.with(Techniques.Shake).playOn(passwordFieldAgain);
            return false;
        }

    }

    public void animateRegisterScreen(){
        Animation sunAnimation = AnimationUtils.loadAnimation(this,R.anim.sun_translate);
        sunAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        sun.startAnimation(sunAnimation);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Integer colorFrom = getResources().getColor(R.color.Nightfall);
        Integer colorTo = getResources().getColor(R.color.AntiqueWhite);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                relativeLayout.setBackgroundColor((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation.setDuration(4000).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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

    class RegisterAnimationsJob extends Job {
        public static final int PRIORITY = 1;

        protected RegisterAnimationsJob() {

            super(new Params(PRIORITY));
        }

        @Override
        public void onAdded() {
        }

        @Override
        public void onRun() throws Throwable {
            RegisterActivity.this.animateRegisterScreen();
        }

        @Override
        protected void onCancel() {

        }

        @Override
        protected boolean shouldReRunOnThrowable(Throwable throwable) {
            return false;
        }
    }

    class RegisterButtonTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(!passwordFieldAgain.getText().toString().isEmpty() || !passwordField.getText().toString().isEmpty() || !emailField.getText().toString().isEmpty() || !usernameField.getText().toString().isEmpty()){
                registerButton.setVisibility(View.VISIBLE);
            }
            else
                registerButton.setVisibility(View.INVISIBLE);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
