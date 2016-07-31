package com.procasy.dubarah_nocker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.transition.Fade;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.procasy.dubarah_nocker.API.APIinterface;
import com.procasy.dubarah_nocker.API.ApiClass;
import com.procasy.dubarah_nocker.Activity.SignUpActivities.MainInfoSignUp;
import com.procasy.dubarah_nocker.Helper.SessionManager;
import com.procasy.dubarah_nocker.Model.Responses.CheckResponse;
import com.procasy.dubarah_nocker.Model.Responses.InfoNockerResponse;
import com.procasy.dubarah_nocker.Model.Responses.LoginResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements Validator.ValidationListener, View.OnClickListener {

    Button  login;

    @NotEmpty
    @Email
    EditText Email;

    @com.mobsandgeeks.saripaar.annotation.Password(min = 6, scheme = com.mobsandgeeks.saripaar.annotation.Password.Scheme.ANY)
    EditText Password;
    SessionManager sessionManager;
    Validator validator;
    APIinterface apiService;
    LinearLayout linearLayout;

    ImageView linkedIn, facebook, twitter, googleplus;

    String UDID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        LinkIDwithViews();
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn())
            startActivity(new Intent(getApplicationContext(), MainActivity.class));

        apiService =
                ApiClass.getClient().create(APIinterface.class);

        validator = new Validator(this);
        validator.setValidationListener(this);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validate();
            }
        });

        setupWindowAnimations();
        printKeyHash(LoginActivity.this);
        linkedIn.setOnClickListener(this);
        marshmallowPhoneStatePremissionCheck();

    }

    private void setupWindowAnimations() {
        Fade slide = new Fade();
        slide.setDuration(10000);
        getWindow().setExitTransition(slide);
    }

    private void LinkIDwithViews() {
        Email = (EditText) findViewById(R.id.email);
        Password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        linearLayout = (LinearLayout) findViewById(R.id.linear);
        linkedIn = (ImageView) findViewById(R.id.linkedin);
    }


    @Override
    public void onValidationSucceeded() {
        final ACProgressFlower dialog = new ACProgressFlower.Builder(LoginActivity.this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Logging in ...")
                .fadeColor(Color.DKGRAY).build();
        dialog.show();
        sessionManager.setEmail(Email.getText().toString());
        sessionManager.setPassword(Password.getText().toString());
        Call<LoginResponse> call = apiService.Login(Email.getText().toString(), Password.getText().toString(), UDID);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (dialog.isShowing())
                    dialog.dismiss();
                switch (response.body().getStatus()) {
                    case 1: {
                        sessionManager.setLogin(true);
                        sessionManager.setUDID(UDID);
                        final ACProgressFlower dialog = new ACProgressFlower.Builder(LoginActivity.this)
                                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                                .themeColor(Color.WHITE)
                                .text("Getting Info..")
                                .fadeColor(Color.DKGRAY).build();
                        dialog.show();
                        APIinterface apiService = ApiClass.getClient().create(APIinterface.class);
                        Call<InfoNockerResponse> call2 = apiService.GetInfoNocker(sessionManager.getEmail(), UDID);
                        call2.enqueue(new Callback<InfoNockerResponse>() {
                            @Override
                            public void onResponse(Call<InfoNockerResponse> call, Response<InfoNockerResponse> response) {
                                System.out.println(response.body().getUser().toString());
                                sessionManager.setEmail(response.body().getUser().getUser_email());
                                sessionManager.setFName(response.body().getUser().getUser_fname());
                                sessionManager.setLName(response.body().getUser().getUser_lname());
                                sessionManager.setPP(response.body().getUser().getUser_img());
                                sessionManager.setAVG(response.body().getAvg_charge());
                                sessionManager.setKeyIsNocker(response.body().getUser().is_nocker());
                                Log.d("nocker", response.body().getUser().is_nocker() + "");
                                if (dialog.isShowing())
                                    dialog.dismiss();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }

                            @Override
                            public void onFailure(Call<InfoNockerResponse> call, Throwable t) {
                                System.out.println("here 2" + t.toString());
                                if (dialog.isShowing())
                                    dialog.dismiss();
                            }
                        });
                        break;
                    }
                    case 0: {
                        AlertDialog.Builder alertdialog = new AlertDialog.Builder(LoginActivity.this);
                        alertdialog.setMessage("Wrong Authentication");
                        alertdialog.setTitle("Fail");
                        alertdialog.setPositiveButton("Ok", null);
                        alertdialog.show();
                        break;
                    }
                    case 2: {
                        startActivity(new Intent(getApplicationContext(),MainInfoSignUp.class));
                        break;
                    }
                }

            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                System.out.println("here 2" + t.toString());
                if (dialog.isShowing())
                    dialog.dismiss();
            }

        });
    }


    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);
            if (Password.getText().toString().length() < 6)
                Snackbar.make(linearLayout, "Your Password Is Short", Snackbar.LENGTH_SHORT).show();
            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    public static String printKeyHash(Activity context) {
        PackageInfo packageInfo;
        String key = null;
        try {
            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);

            Log.e("Package Name=", context.getApplicationContext().getPackageName());

            for (android.content.pm.Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));

                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.e("Key Hash=", key);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
    }

    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.R_EMAILADDRESS);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linkedin: {
                LISessionManager.getInstance(getApplicationContext()).init(LoginActivity.this, buildScope(), new AuthListener() {
                    @Override
                    public void onAuthSuccess() {
                        String url = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,email-address,picture-url)?format=json";
                        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
                        apiHelper.getRequest(getApplicationContext(), url, new ApiListener() {
                            @Override
                            public void onApiSuccess(ApiResponse apiResponse) {
                                final JSONObject object = apiResponse.getResponseDataAsJson();
                                try {
                                    final String EmailAddress = object.getString("emailAddress");
                                    Call<CheckResponse> call = apiService.CheckUnique("user", "user_email", object.getString("emailAddress"));
                                    call.enqueue(new Callback<CheckResponse>() {
                                        @Override
                                        public void onResponse(Call<CheckResponse> call, Response<CheckResponse> response) {
                                            if (response.body().getStatus()) // new user
                                            {

                                            } else /// old user
                                            {

                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<CheckResponse> call, Throwable t) {

                                        }
                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onApiError(LIApiError liApiError) {
                                // Error making GET request!
                            }
                        });
                    }

                    @Override
                    public void onAuthError(LIAuthError error) {
                        // Handle authentication errors
                        System.out.println(error.toString());

                    }
                }, true);
                break;
            }
        }
    }


    private void marshmallowPhoneStatePremissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getApplicationContext().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_PHONE_STATE},
                    5);
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            UDID = telephonyManager.getDeviceId();
            Log.e("UDID", UDID);
            //   gps functions.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 5 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            UDID = telephonyManager.getDeviceId();
            Log.e("UDID Marshmelo :D ", UDID);
        }
    }
}
