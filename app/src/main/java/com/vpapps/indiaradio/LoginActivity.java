package com.vpapps.indiaradio;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.vpapps.interfaces.LoginListener;
import com.vpapps.interfaces.SocialLoginListener;
import com.vpapps.item.ItemUser;
import com.vpapps.utils.Constant;
import com.vpapps.utils.Methods;
import com.vpapps.utils.SharedPref;
import com.onesignal.OneSignal;
import com.vpapps.asyncTask.LoadLogin;
import com.vpapps.asyncTask.LoadRegister;
import com.vpapps.indiaradio.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import cn.refactor.library.SmoothCheckBox;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class LoginActivity extends AppCompatActivity {

    private String from = "";

    SharedPref sharedPref;
    EditText editText_email, editText_password;
    Button button_login, button_skip;
    TextView textView_register, textView_forgotpass;
    Methods methods;
    ProgressDialog progressDialog;
    LinearLayout ll_checkbox;
    SmoothCheckBox cb_rememberme;
    private MaterialButton btn_login_google, btn_login_fb;
    private FirebaseAuth mAuth;

    /*Facebook Login*/
    LoginButton loginButtonFB;
    CallbackManager callbackManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        try {
            LoginManager.getInstance().logOut();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception e) {
            e.printStackTrace();
        }

        from = getIntent().getStringExtra("from");

        sharedPref = new SharedPref(this);
        methods = new Methods(this);
        methods.forceRTLIfSupported(getWindow());
        methods.setStatusColor(getWindow());

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);

        btn_login_google = findViewById(R.id.btn_login_google);
        btn_login_fb = findViewById(R.id.btn_login_fb);
        loginButtonFB = findViewById(R.id.login_button);
        loginButtonFB.setReadPermissions(Arrays.asList("email"));
        callbackManager = CallbackManager.Factory.create();

        ll_checkbox = findViewById(R.id.ll_checkbox);
        cb_rememberme = findViewById(R.id.cb_rememberme);
        editText_email = findViewById(R.id.et_login_email);
        editText_password = findViewById(R.id.et_login_password);
        button_login = findViewById(R.id.button_login);
        button_skip = findViewById(R.id.button_skip);
        textView_register = findViewById(R.id.tv_login_signup);
        textView_forgotpass = findViewById(R.id.tv_forgotpass);

        button_login.setBackground(methods.getRoundDrawable(getResources().getColor(R.color.colorPrimary)));
        button_skip.setBackground(methods.getRoundDrawable(Color.WHITE));
        button_skip.setTextColor(getResources().getColor(R.color.colorPrimary));

        TextView tv_welcome = findViewById(R.id.tv);

        tv_welcome.setTypeface(tv_welcome.getTypeface(), Typeface.BOLD);
        textView_forgotpass.setTypeface(textView_forgotpass.getTypeface(), Typeface.BOLD);
        textView_register.setTypeface(textView_register.getTypeface(), Typeface.BOLD);
        button_login.setTypeface(button_login.getTypeface(), Typeface.BOLD);
        button_skip.setTypeface(button_skip.getTypeface(), Typeface.BOLD);

        if(sharedPref.getIsRemember()) {
            editText_email.setText(sharedPref.getEmail());
            editText_password.setText(sharedPref.getPassword());
            cb_rememberme.setChecked(true);
        }

        ll_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb_rememberme.setChecked(!cb_rememberme.isChecked());
            }
        });

        button_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMainActivity();
            }
        });

        textView_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        textView_forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //attemptLogin();
            }
        });

        btn_login_fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButtonFB.performClick();
            }
        });

        btn_login_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (methods.isNetworkAvailable()) {
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build();

                    GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);

                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, 112);
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.err_internet_not_conn), Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginButtonFB.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                //loginResult.getAccessToken();
                //loginResult.getRecentlyDeniedPermissions()
                //loginResult.getRecentlyGrantedPermissions()
                boolean loggedIn = AccessToken.getCurrentAccessToken() != null;
                if (loggedIn) {
                    getUserProfile(AccessToken.getCurrentAccessToken());
                }

            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }

    private void attemptLogin() {
        editText_email.setError(null);
        editText_password.setError(null);

        // Store values at the time of the login attempt.
        String email = editText_email.getText().toString();
        String password = editText_password.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            editText_password.setError(getString(R.string.error_password_sort));
            focusView = editText_password;
            cancel = true;
        }
        if (editText_password.getText().toString().endsWith(" ")) {
            editText_password.setError(getString(R.string.pass_end_space));
            focusView = editText_password;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            editText_email.setError(getString(R.string.cannot_empty));
            focusView = editText_email;
            cancel = true;
        } else if (!isEmailValid(email)) {
            editText_email.setError(getString(R.string.error_invalid_email));
            focusView = editText_email;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            //loadLogin();
        }
    }

    private void loadLogin() {
        if (methods.isNetworkAvailable()) {
            LoadLogin loadLogin = new LoadLogin(new LoginListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String loginSuccess, String message, String user_id, String user_name) {
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        if (loginSuccess.equals("1")) {
                            Constant.itemUser = new ItemUser(user_id, user_name, editText_email.getText().toString(), "", "", Constant.LOGIN_TYPE_NORMAL);
                            if (cb_rememberme.isChecked()) {
                                sharedPref.setLoginDetails(Constant.itemUser, cb_rememberme.isChecked(), editText_password.getText().toString(), Constant.LOGIN_TYPE_NORMAL);
                            } else {
                                sharedPref.setRemeber(false);
                            }
                            sharedPref.setIsAutoLogin(true);
                            Constant.isLogged = true;
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

                            if (from.equals("app")) {
                                finish();
                            } else {
                                openMainActivity();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.err_server), Toast.LENGTH_SHORT).show();
                    }
                }
            }, methods.getAPIRequest(Constant.METHOD_LOGIN, 0,"","","",Constant.LOGIN_TYPE_NORMAL,"","","","","", editText_email.getText().toString(), editText_password.getText().toString(),"","","","", null));
            loadLogin.execute();
        } else {
            Toast.makeText(LoginActivity.this, getString(R.string.err_internet_not_conn), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && !email.contains(" ");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 0;
    }

    private void openMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void loadLoginSocial(final String loginType, final String name, String email, final String authId) {
        if (methods.isNetworkAvailable()) {

            LoadRegister loadRegister = new LoadRegister(new SocialLoginListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String registerSuccess, String message, String user_id, String user_name, String email, String auth_id) {
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        switch (registerSuccess) {
                            case "1":
                                Constant.itemUser = new ItemUser(user_id, user_name, email, "", authId, loginType);
                                sharedPref.setLoginDetails(Constant.itemUser, true, "", loginType);
                                sharedPref.setIsAutoLogin(true);

                                Constant.isLogged = true;
                                OneSignal.sendTag("user_id", user_id);

                                Toast.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();

                                if (from.equals("app")) {
                                    finish();
                                } else {
                                    openMainActivity();
                                }
                                break;
                            case "-1":
                                methods.getVerifyDialog(getString(R.string.error_unauth_access), message);
                                break;
                            default:
                                if (message.contains("already") || message.contains("Invalid email format")) {
                                    editText_email.setError(message);
                                    editText_email.requestFocus();
                                } else {
                                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                }

                                try {
                                    if (loginType.equals(Constant.LOGIN_TYPE_FB)) {
                                        LoginManager.getInstance().logOut();
                                    } else if (loginType.equals(Constant.LOGIN_TYPE_GOOGLE)) {
                                        FirebaseAuth.getInstance().signOut();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                }
            }, methods.getAPIRequest(Constant.METHOD_REGISTER, 0, authId, "", "", loginType, "", "", "", "", "", email, "", name, "", "", "", null));
            loadRegister.execute();
        } else {
            Toast.makeText(LoginActivity.this, getString(R.string.err_internet_not_conn), Toast.LENGTH_SHORT).show();
        }
    }

    private void getUserProfile(AccessToken currentAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            String first_name="", email = "",last_name = "";

                            if(object.has("first_name")) {
                                first_name = object.getString("first_name");
                            }
                            if(object.has("last_name")) {
                                last_name = object.getString("last_name");
                            }
                            if(object.has("email")) {
                                email = object.getString("email");
                            }
                            String id = object.getString("id");
                           // loadLoginSocial(Constant.LOGIN_TYPE_FB, first_name + " " + last_name, email, id);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();

    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            //loadLoginSocial(Constant.LOGIN_TYPE_GOOGLE, user.getDisplayName(), user.getEmail(), user.getUid());
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to Sign IN", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 112) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            try {
                if (resultCode != 0) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    firebaseAuthWithGoogle(task.getResult().getIdToken());
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.err_login_goole), Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e) {
                Toast.makeText(LoginActivity.this, getString(R.string.err_login_goole), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}