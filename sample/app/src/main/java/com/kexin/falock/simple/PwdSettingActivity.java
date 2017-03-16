package com.kexin.falock.simple;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.kexin.sdk.net.KexinHttp;
import com.kexin.sdk.net.KexinHttpCallback;
import com.kexin.sdk.net.NetCode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;

public class PwdSettingActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etEffectiveAt, etExpire, etLockId1, etUid1, etLockId2, etLockId3, etPwd1, etUid2;
    private TextView tv1, tv2, tv3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pwd_setting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        initView();
    }

    private void initView() {
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);

        etEffectiveAt = (EditText) findViewById(R.id.editText1);
        etExpire = (EditText) findViewById(R.id.editText2);
        etLockId1 = (EditText) findViewById(R.id.editText3);
        etUid1 = (EditText) findViewById(R.id.editText4);
        etLockId2 = (EditText) findViewById(R.id.editText5);
        etPwd1 = (EditText) findViewById(R.id.editText6);
        etLockId3 = (EditText) findViewById(R.id.editText7);
        etUid2 = (EditText) findViewById(R.id.editText8);

        tv1 = (TextView) findViewById(R.id.textView1);
        tv2 = (TextView) findViewById(R.id.textView2);
        tv3 = (TextView) findViewById(R.id.textView3);
    }

    @Override
    public void onClick(View v) {
        tv1.setText("");
        tv2.setText("");
        tv3.setText("");
        try {
            int lockId;
            int uId;
            String pwd;
            switch (v.getId()) {
                case R.id.button1: //设置并获取密码
                    String ttmp = etEffectiveAt.getText().toString().trim();
                    int effAt;
                    if(TextUtils.isEmpty(ttmp)){
                        effAt = (int) (System.currentTimeMillis() / 1000);
                    } else {
                        effAt = Integer.parseInt(ttmp);
                    }
                    int expire = Integer.parseInt(etExpire.getText().toString().trim());
                    lockId = Integer.parseInt(etLockId1.getText().toString().trim());
                    uId = Integer.parseInt(etUid1.getText().toString().trim());
                    KexinHttp.getInstance().faLockGetAndSetPwd(effAt, expire, lockId, uId, new KexinHttpCallback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(int code, String message, String response) {
                            tv1.setText(response);
                            if(code == NetCode.RESPONSE_OK){
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    jsonObject = jsonObject.getJSONObject("data");
                                    String pwd = jsonObject.getString("password");
                                    String seq = jsonObject.getString("seq");
                                    etLockId2.setText(etLockId1.getText());
                                    etLockId3.setText(etLockId1.getText());
                                    etPwd1.setText(pwd);
                                    etUid2.setText(etUid1.getText());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    break;
                case R.id.button2: //通过密码远程开锁
                    lockId = Integer.parseInt(etLockId2.getText().toString().trim());
                    pwd = etPwd1.getText().toString().trim();
                    KexinHttp.getInstance().faLockOpenLockRemoteByPwd(lockId, pwd, new KexinHttpCallback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                        }

                        @Override
                        public void onResponse(int code, String message, String response) {
                            tv2.setText(response);
                        }
                    });
                    break;
                case R.id.button3: //回收用户密码
                    lockId = Integer.parseInt(etLockId3.getText().toString().trim());
                    uId = Integer.parseInt(etUid2.getText().toString().trim());
                    KexinHttp.getInstance().faLockDelPwd(lockId, uId, new KexinHttpCallback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(int code, String message, String response) {
                            tv3.setText(response);
                        }
                    });
                    break;
                case R.id.button4: //清除数据
                    tv1.setText("");
                    tv2.setText("");
                    tv3.setText("");
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
