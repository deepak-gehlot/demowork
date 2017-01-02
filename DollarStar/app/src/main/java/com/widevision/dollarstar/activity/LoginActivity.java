package com.widevision.dollarstar.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.widevision.dollarstar.R;
import com.widevision.dollarstar.dao.GsonClass;
import com.widevision.dollarstar.dao.LoginDao;
import com.widevision.dollarstar.dao.RegistrationDao;
import com.widevision.dollarstar.model.HideKeyActivity;
import com.widevision.dollarstar.util.AsyncCallback;
import com.widevision.dollarstar.util.Extension;
import com.widevision.dollarstar.util.PreferenceConnector;
import com.widevision.dollarstar.util.ProgressLoaderHelper;
import com.widevision.dollarstar.util.ValidationTemplate;

import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends HideKeyActivity {

    /*--For Facebook login--*/
    private CallbackManager callbackManager;
    private AccessToken access_token;

    private EditText mEmailEdt, mPasswordEdt;
    private Extension extension;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PreferenceConnector.readString(LoginActivity.this, PreferenceConnector.IS_LOGIN, "No").equals("Yes")) {
            Bundle translateBundle = ActivityOptions.makeCustomAnimation(LoginActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent, translateBundle);
            finish();
        }

        try {
            LoginManager.getInstance().logOut();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_login);
        init();

        mPasswordEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                }
                return false;
            }
        });
    }

    private void init() {
        setupUI(findViewById(R.id.main_container));
        extension = new Extension();
        callbackManager = CallbackManager.Factory.create();
        mEmailEdt = (EditText) findViewById(R.id.email_EDT);
        mPasswordEdt = (EditText) findViewById(R.id.password_EDT);
    }

    public void onClickFacebookBtn(View view) {

        if (extension.executeStrategy(LoginActivity.this, "", ValidationTemplate.INTERNET)) {
            LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "email"));
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    access_token = AccessToken.getCurrentAccessToken();

                    final ProgressLoaderHelper loaderHelper = ProgressLoaderHelper.getInstance();
                    loaderHelper.showProgress(LoginActivity.this);
                    GraphRequest request = GraphRequest.newMeRequest(access_token,
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    loaderHelper.dismissProgress();
                                    // Application code
                                    JSONObject jsonObject = response.getJSONObject();
                                    Log.e("string value-----", jsonObject.toString());
                                    try {
                                        attamptToSend(jsonObject);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "first_name,last_name,email,gender");

                    request.setParameters(parameters);
                    request.executeAsync();
                }

                @Override
                public void onCancel() {
                    showSnackBar("Cancel");
                }

                @Override
                public void onError(FacebookException exception) {
                    if (exception instanceof FacebookAuthorizationException) {
                        if (AccessToken.getCurrentAccessToken() != null) {
                            LoginManager.getInstance().logOut();
                        }
                    }
                    showSnackBar("" + exception);
                }
            });
        }
    }

    private void attamptToSend(JSONObject jsonObject) {
/*(first_name,last_name,email,gender,profile_pic,facebook_id)*/
        final ProgressLoaderHelper loaderHelper = ProgressLoaderHelper.getInstance();
        loaderHelper.showProgress(LoginActivity.this);
        try {
            String first_name = jsonObject.getString("first_name");
            String email = jsonObject.getString("email");
            String gender = jsonObject.getString("gender");
            String facebook_id = jsonObject.getString("id");


            RegistrationDao registrationDao = new RegistrationDao(first_name, email, email, gender, facebook_id, "");
            registrationDao.query(new AsyncCallback<GsonClass>() {
                @Override
                public void onOperationCompleted(GsonClass result, Exception e) {
                    loaderHelper.dismissProgress();
                    if (result != null && e == null) {
                        if (result.success.equals("1")) {
                            PreferenceConnector.writeString(LoginActivity.this, PreferenceConnector.IS_LOGIN, "Yes");
                            PreferenceConnector.writeString(LoginActivity.this, PreferenceConnector.LOGIN_UserId, result.id);
                            Bundle translateBundle = ActivityOptions.makeCustomAnimation(LoginActivity.this, R.anim.slide_out_left, R.anim.slide_in_left).toBundle();
                            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(i, translateBundle);
                            finish();
                        } else if (result.message.equals("Email already exists.")) {
                            PreferenceConnector.writeString(LoginActivity.this, PreferenceConnector.IS_LOGIN, "Yes");
                            PreferenceConnector.writeString(LoginActivity.this, PreferenceConnector.LOGIN_UserId, result.id);
                            Bundle translateBundle = ActivityOptions.makeCustomAnimation(LoginActivity.this, R.anim.slide_out_left, R.anim.slide_in_left).toBundle();
                            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(i, translateBundle);
                            finish();
                        } else {
                            showSnackBar(result.message);
                        }
                    } else {
                        showSnackBar(getString(R.string.wrong));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            loaderHelper.dismissProgress();
        }
    }


    public void onClickSignIn(View view) {
        attemptLogin();
    }

    public void onClickSignUp(View view) {
        Bundle translateBundle = ActivityOptions.makeCustomAnimation(LoginActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
        Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
        startActivity(intent, translateBundle);
        finish();
    }

    private void attemptLogin() {
        String email = mEmailEdt.getText().toString().trim();
        String password = mPasswordEdt.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            showSnackBar("Enter email and password");
        }/* else if (!extension.executeStrategy(LoginActivity.this, email, ValidationTemplate.EMAIL)) {
            showSnackBar("Invalid email id.");
        } */ else if (!extension.executeStrategy(LoginActivity.this, "", ValidationTemplate.INTERNET)) {
            showSnackBar(getString(R.string.no_internet));
        } else {
            final ProgressLoaderHelper loaderHelper = ProgressLoaderHelper.getInstance();
            loaderHelper.showProgress(LoginActivity.this);
            LoginDao loginDao = new LoginDao(email, password);
            loginDao.query(new AsyncCallback<GsonClass>() {
                @Override
                public void onOperationCompleted(GsonClass result, Exception e) {
                    loaderHelper.dismissProgress();
                    if (result != null && e == null) {
                        if (result.success.equals("1")) {
                            Bundle translateBundle = ActivityOptions.makeCustomAnimation(LoginActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent, translateBundle);
                            finish();
                            PreferenceConnector.writeString(LoginActivity.this, PreferenceConnector.IS_LOGIN, "Yes");
                            PreferenceConnector.writeString(LoginActivity.this, PreferenceConnector.LOGIN_UserId, result.id);
                        } else {
                            showSnackBar(result.message);
                        }
                    } else {
                        showSnackBar(getString(R.string.wrong));
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void showSnackBar(String message) {
        Snackbar.make(findViewById(R.id.main_container), message, Snackbar.LENGTH_LONG).show();
    }
}
