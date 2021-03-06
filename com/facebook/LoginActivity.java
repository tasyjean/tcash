package com.facebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.facebook.android.C0426R;

public class LoginActivity extends Activity {
    private static final String EXTRA_REQUEST = "request";
    private static final String NULL_CALLING_PKG_ERROR_MSG = "Cannot call LoginActivity with a null calling package. This can occur if the launchMode of the caller is singleInstance.";
    static final String RESULT_KEY = "com.facebook.LoginActivity:Result";
    private static final String SAVED_AUTH_CLIENT = "authorizationClient";
    private static final String SAVED_CALLING_PKG_KEY = "callingPackage";
    private static final String TAG = LoginActivity.class.getName();
    private AuthorizationClient authorizationClient;
    private String callingPackage;
    private AuthorizationRequest request;

    class C03951 implements OnCompletedListener {
        C03951() {
        }

        public void onCompleted(Result result) {
            LoginActivity.this.onAuthClientCompleted(result);
        }
    }

    class C03962 implements BackgroundProcessingListener {
        C03962() {
        }

        public void onBackgroundProcessingStarted() {
            LoginActivity.this.findViewById(C0426R.id.com_facebook_login_activity_progress_bar).setVisibility(0);
        }

        public void onBackgroundProcessingStopped() {
            LoginActivity.this.findViewById(C0426R.id.com_facebook_login_activity_progress_bar).setVisibility(8);
        }
    }

    private void onAuthClientCompleted(Result result) {
        this.request = null;
        int i = result.code == Code.CANCEL ? 0 : -1;
        Bundle bundle = new Bundle();
        bundle.putSerializable(RESULT_KEY, result);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(i, intent);
        finish();
    }

    static Bundle populateIntentExtras(AuthorizationRequest authorizationRequest) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_REQUEST, authorizationRequest);
        return bundle;
    }

    protected void onActivityResult(int i, int i2, Intent intent) {
        this.authorizationClient.onActivityResult(i, i2, intent);
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0426R.layout.com_facebook_login_activity_layout);
        if (bundle != null) {
            this.callingPackage = bundle.getString(SAVED_CALLING_PKG_KEY);
            this.authorizationClient = (AuthorizationClient) bundle.getSerializable(SAVED_AUTH_CLIENT);
        } else {
            this.callingPackage = getCallingPackage();
            this.authorizationClient = new AuthorizationClient();
            this.request = (AuthorizationRequest) getIntent().getSerializableExtra(EXTRA_REQUEST);
        }
        this.authorizationClient.setContext((Activity) this);
        this.authorizationClient.setOnCompletedListener(new C03951());
        this.authorizationClient.setBackgroundProcessingListener(new C03962());
    }

    public void onPause() {
        super.onPause();
        this.authorizationClient.cancelCurrentHandler();
        findViewById(C0426R.id.com_facebook_login_activity_progress_bar).setVisibility(8);
    }

    public void onResume() {
        super.onResume();
        if (this.callingPackage == null) {
            Log.e(TAG, NULL_CALLING_PKG_ERROR_MSG);
            finish();
            return;
        }
        this.authorizationClient.startOrContinueAuth(this.request);
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(SAVED_CALLING_PKG_KEY, this.callingPackage);
        bundle.putSerializable(SAVED_AUTH_CLIENT, this.authorizationClient);
    }
}
