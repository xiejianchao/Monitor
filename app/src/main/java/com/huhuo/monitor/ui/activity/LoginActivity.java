package com.huhuo.monitor.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;

import android.content.Intent;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.huhuo.monitor.R;
import com.huhuo.monitor.constants.Constants;
import com.huhuo.monitor.net.HttpHelper;
import com.huhuo.monitor.net.callback.SimpleHttpRequestCallBack;
import com.huhuo.monitor.utils.Logger;
import com.huhuo.monitor.utils.SPUtil;
import com.huhuo.monitor.utils.ToastUtil;

import org.json.JSONObject;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;


/**
 * A login screen that offers login via email/password.
 */
@ContentView(R.layout.activity_login)
public class LoginActivity extends BaseActivity{

    private static final String TAG = LoginActivity.class.getSimpleName();
    // 填写从短信SDK应用后台注册得到的APPKEY
    private static String APPKEY = "f0e80fad47ea";

    // 填写从短信SDK应用后台注册得到的APPSECRET
    private static String APPSECRET = "a1fdc4cd4dc3cf4f4df1ebace93b3f2e";

    @ViewInject(R.id.btn_get_code)
    private Button btnVerifcationCode;

    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mMobilePhoneView;
    private EditText mVerificationCodeView;
    private View mProgressView;
    private View mLoginFormView;

    public SMSCountDown smsCountDown;
    private String phoneVerify = null;

    @Override
    protected void init(Bundle savedInstanceState) {
        initSMSSDK();
        // Set up the login form.
        mMobilePhoneView = (AutoCompleteTextView) findViewById(R.id.email);
//        populateAutoComplete();

        mVerificationCodeView = (EditText) findViewById(R.id.password);
        mVerificationCodeView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
//                    attemptLogin();
                    checkVerificationCodeAndLogin();
                    return true;
                }
                return false;
            }
        });

        SPUtil.saveMobile("13466608793");
        Button btnLoginButton = (Button) findViewById(R.id.btn_login);
        btnLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                attemptLogin();
                checkVerificationCodeAndLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            Logger.e("event", "event="+event);
            if (result == SMSSDK.RESULT_COMPLETE) {
                Logger.d(TAG,"RESULT_COMPLETE");
                //回调完成
                Logger.d(TAG,"RESULT_COMPLETE event:" + event + ",result:" + result + ",data:" + data);
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    //提交验证码成功
                    Logger.d(TAG,"提交验证码成功 event:" + event + ",result:" + result + ",data:" + data);
                    startLogin();
                }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                    //获取验证码成功
                    Logger.d(TAG,"获取验证码成功 event:" + event + ",result:" + result + ",data:" + data);
                    ToastUtil.showShortToast(R.string.prompt_verify_code_sended);
                }else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                    //返回支持发送验证码的国家列表
                    Logger.d(TAG,"返回支持发送验证码的国家列表 event:" + event + ",result:" + result + ",data:" + data);
                }
            }else{
                ((Throwable)data).printStackTrace();
                int status = 0;
                try {
                    Throwable throwable = (Throwable) data;
                    Logger.e(TAG,"短信回调",((Throwable) data));

                    JSONObject object = new JSONObject(throwable.getMessage());
                    String des = object.optString("detail");
                    status = object.optInt("status");
                    if(status == 478){
                        mMobilePhoneView.setError(getString(R.string.prompt_verify_code_too_much));
                    }
                    if (!TextUtils.isEmpty(des)) {
                        showProgress(false);
                        ToastUtil.showShortToast(des);
                        return;
                    }
                } catch (Exception e) {
                    Logger.d(TAG,"getCode",e);
                }
            }
        };
    };

    private void startLogin() {
        final RequestParams params = new RequestParams(Constants.getLoginUrl(phoneVerify));
        showProgress(true);
        HttpHelper.post(params, new SimpleHttpRequestCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                showProgress(false);
                SPUtil.saveMobile(phoneVerify);
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                finish();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                showProgress(false);
            }
        });
    }

    @Event(value = R.id.btn_get_code)
    private void getCode(View view) {
        phoneVerify = mMobilePhoneView.getText().toString();

        if (TextUtils.isEmpty(phoneVerify)) {
            mMobilePhoneView.setError(getString(R.string.error_mobiel_phone_required));
            return;
        }

        SMSSDK.getVerificationCode("86", phoneVerify);
        setSendSmsAndStartCountDown();
    }

    private void initSMSSDK() {
        //初始化短信sdk并获取验证码
        SMSSDK.initSDK(context, APPKEY, APPSECRET);
        EventHandler eh = new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = Message.obtain();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                mHandler.sendMessage(msg);
            }
        };
        SMSSDK.registerEventHandler(eh); //注册短信回调
    }


    private void setSendSmsAndStartCountDown() {
        smsCountDown = new SMSCountDown(60000, 1000);
        smsCountDown.start();
        btnVerifcationCode.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }

    private void checkVerificationCodeAndLogin() {
        if (TextUtils.isEmpty(phoneVerify)) {
            mMobilePhoneView.setError(getString(R.string.error_mobiel_phone_required));
            mMobilePhoneView.requestFocus();
            return;
        }

        if (phoneVerify.length() != 11) {
            mMobilePhoneView.setError(getString(R.string.error_invalid_mobile_phone));
            mMobilePhoneView.requestFocus();
            return;
        }

        String password = mVerificationCodeView.getText().toString();
        SMSSDK.submitVerificationCode("86",phoneVerify,password);
        showProgress(true);
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mMobilePhoneView.setError(null);
        mVerificationCodeView.setError(null);

        // Store values at the time of the login attempt.
        String mobilePhone = mMobilePhoneView.getText().toString();
        String password = mVerificationCodeView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid mobilePhone address.
        if (TextUtils.isEmpty(mobilePhone)) {
            mMobilePhoneView.setError(getString(R.string.error_mobiel_phone_required));
            focusView = mMobilePhoneView;
            cancel = true;
            focusView.requestFocus();
            return;
        } else if (mobilePhone.length() != 11) {
            mMobilePhoneView.setError(getString(R.string.error_invalid_mobile_phone));
            focusView = mMobilePhoneView;
            cancel = true;
            focusView.requestFocus();
            return;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isVerificationCodeValid(password)) {
            mVerificationCodeView.setError(getString(R.string.error_invalid_password));
            focusView = mVerificationCodeView;
            cancel = true;
            focusView.requestFocus();
            return;
        }

        showProgress(true);
        mAuthTask = new UserLoginTask(mobilePhone, password);
        mAuthTask.execute((Void) null);
    }

    private boolean isVerificationCodeValid(String password) {
        return password.length() == 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mobile;
        private final String verifyCode;

        UserLoginTask(String mobile, String password) {
            this.mobile = mobile;
            verifyCode = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
//                finish();
                ToastUtil.showShortToast("登录成功");
            } else {
                mVerificationCodeView.setError(getString(R.string.error_incorrect_password));
                mVerificationCodeView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public class SMSCountDown extends CountDownTimer {

        public SMSCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            try {
                int interval = (int) (millisUntilFinished / 1000);
                btnVerifcationCode
                        .setText(getString(R.string.renew_request_verification_code, interval));
                btnVerifcationCode.setEnabled(false);
//                btnVerifcationCode.setTextColor(getResources().getColor(R.color.xy_gray_m));
            } catch (Exception e) {
                Logger.e(TAG, "SMSCountDown onTick()", e);
            }
        }

        @Override
        public void onFinish() {
            try {
                btnVerifcationCode.setEnabled(true);
                btnVerifcationCode.setText(getString(R.string.renew_request_verification_code_2));
//                btnVerifcationCode.setTextColor(getResources().getColor(R.color.xy_blue_l));
            } catch (Exception e) {
                Logger.d(TAG, "SMSCountDown onFinish()", e);
            }
        }
    }

}

