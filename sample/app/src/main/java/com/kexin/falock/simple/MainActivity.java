package com.kexin.falock.simple;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kexin.sdk.net.KexinHttp;
import com.kexin.sdk.net.KexinHttpCallback;
import com.kexin.sdk.net.NetCode;
import com.kexin.sdk.service.BleService;
import com.kexin.sdk.service.BleServiceBinder;
import com.kexin.sdk.service.listener.OnGetLockStateListener;
import com.kexin.sdk.service.listener.OnInitLockListener;
import com.kexin.sdk.service.listener.OnOpenLockListener;
import com.kexin.sdk.service.listener.OnSyncLockListener;
import com.kexin.sdk.utils.EasyToast;
import com.kexin.sdk.utils.MyLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    /**
     * 数据、状态
     */
    private TextView tv1, tv2, tv3, tv4, tv5, tv6, tv7;
    private EditText et1, et2;
    private String pwd;
    private String privateKey, activeKey, keySeq;
    private String key1, key2, seq;

    private BleService mBleService;
    private boolean isDel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        et1 = (EditText) findViewById(R.id.editText1);
        et2 = (EditText) findViewById(R.id.editText2);

        et2.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0) {
                    isDel = true;
                    return;
                } else {
                    isDel = false;
                }
                switch (start) {
                    case 1:
                    case 4:
                    case 7:
                    case 10:
                    case 13:
                        et2.append(":");
                        break;
                    default:
                        break;
                }

                if (s.length() >= 17) {
                    String mac = "";
                    mac = s.subSequence(0, 17).toString();
                    if (!mac.contains(":")) {
                        Toast.makeText(MainActivity.this, "mac地址非法", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] temp = mac.split(":");
                    if (temp.length != 6) {
                        Toast.makeText(MainActivity.this, "mac地址非法", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (String aTemp : temp) {
                        if (aTemp.length() != 2) {
                            Toast.makeText(MainActivity.this, "mac地址非法", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isDel) {
                    return;
                }
                int length = s.length();
                switch (length) {
                    case 3:
                    case 6:
                    case 9:
                    case 12:
                    case 15:
                        char c = s.charAt(length - 1);
                        MyLog.i("c:" + c);
                        if (c != ':') {
                            s.insert(length - 1, ":");
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        tv1 = (TextView) findViewById(R.id.textView);
        tv2 = (TextView) findViewById(R.id.textView2);
        tv3 = (TextView) findViewById(R.id.textView3);
        tv4 = (TextView) findViewById(R.id.textView4);
        tv5 = (TextView) findViewById(R.id.textView5);
        tv6 = (TextView) findViewById(R.id.textView6);
        tv7 = (TextView) findViewById(R.id.textView7);

        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
        findViewById(R.id.button5).setOnClickListener(this);
        findViewById(R.id.button6).setOnClickListener(this);
        findViewById(R.id.button7).setOnClickListener(this);
        findViewById(R.id.button8).setOnClickListener(this);

        //绑定开锁服务
        Intent gattServiceIntent = new Intent(this, BleService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBleService = ((BleServiceBinder)service).getService();
            if (!mBleService.initialize()) {
                MyLog.e("蓝牙初始化失败！");
                return;
            }
            if (!mBleService.isBleEnabled()) {
                mBleService.requestBleOpen();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBleService = null;
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button: //获取开锁秘钥
                tv1.setText("加载中...");
                pwd = "";
                int lockId = 0;
                try {
                    lockId = Integer.parseInt(et1.getText().toString().trim());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                KexinHttp.getInstance().faLockGetLockPassword(lockId, (int) (System.currentTimeMillis() / 1000),
                        300, new KexinHttpCallback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(int code, String message, String response) {
                                MyLog.i("==============response=" + response);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
//                                    int codeRes = jsonObject.getInt("code"); // 也可以使用json返回的code
                                    if (code == 200) {
                                        JSONObject obj = jsonObject.getJSONObject("data");
                                        pwd = obj.getString("password");
                                        int seq = obj.getInt("seq"); //用于上报开锁结果
                                        tv1.setText(pwd);
                                    } else {
                                        // 错误提示
                                        EasyToast.showLong(mContext, NetCode.getMessageForCode(code));
                                        tv1.setText(response);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                break;
            case R.id.button2: //开锁
                try {
                    mBleService.openLock(pwd, et1.getText().toString().trim(), et2.getText().toString().trim().toUpperCase(), new OnOpenLockListener() {
                        @Override
                        public void onConnecting(int state) {
                            MainActivity.this.onConnecting(state, tv2);
                        }

                        @Override
                        public void onResult(int state, boolean openResult, int power) {
                            MyLog.e("state:" + state + " openResult:" + openResult + " power:" + power);
                            if (openResult) {
                                tv2.setText("开锁成功");
                            } else {
                                tv2.setText("开锁失败");
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button3: //获取初始化数据
                tv3.setText("加载中...");
                KexinHttp.getInstance().faLockInitLock(et2.getText().toString().trim().toUpperCase(),
                        new KexinHttpCallback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(int code, String message, String response) {
                                MyLog.e("faLockInitLock json:" + response);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    int codeRes = jsonObject.getInt("code");
                                    if (codeRes == 200) {
                                        JSONObject obj = jsonObject.getJSONObject("data");
                                        privateKey = obj.getString("private_key");
                                        activeKey = obj.getString("active_key");
                                        keySeq = obj.getString("key_seq");
                                        tv3.setText("pk:" + privateKey + "\nak:" + activeKey + "\nseq:" + keySeq);
                                    } else {
                                        tv3.setText(response);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                break;
            case R.id.button4: //初始化
                try {
                    mBleService.initLock(privateKey, activeKey, keySeq, et1.getText().toString().trim(), et2.getText().toString().trim().toUpperCase(), new OnInitLockListener() {
                        @Override
                        public void onResult(boolean result) {
                            MyLog.e("result:" + result);
                            if (result) {
                                tv4.setText("初始化成功");
                            } else {
                                tv4.setText("初始化失败");
                            }
                        }

                        @Override
                        public void onConnecting(int state) {
                            MainActivity.this.onConnecting(state, tv4);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button5: //获取时间同步数据
                tv5.setText("加载中...");
                lockId = 0;
                try {
                    lockId = Integer.parseInt(et1.getText().toString().trim());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                KexinHttp.getInstance().faLockGetLockSeq(lockId,
                        new KexinHttpCallback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(int code, String message, String response) {
                                MyLog.i("faLockGetLockSeq json:" + response);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    int codeRes = jsonObject.getInt("code");
                                    if (codeRes == 200) {
                                        JSONObject obj = jsonObject.getJSONObject("data");
                                        key1 = obj.getString("key1");
                                        key2 = obj.getString("key2");
                                        seq = obj.getString("seq");
                                        tv5.setText("key1:" + key1 + "\nkey2:" + key2 + "\nseq:" + seq);
                                    } else {
                                        tv5.setText(response);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                break;
            case R.id.button6: //时间同步
                try {
                    mBleService.syncLockTime(key1, key2, seq, et1.getText().toString().trim(),
                            et2.getText().toString().trim().toUpperCase(), new OnSyncLockListener() {
                                @Override
                                public void onResult(boolean result) {
                                    if (result) {
                                        tv6.setText("时间同步成功");
                                    } else {
                                        tv6.setText("时间同步失败");
                                    }
                                }

                                @Override
                                public void onConnecting(int state) {
                                    MainActivity.this.onConnecting(state, tv6);
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button7: //获取锁状态信息
                try {
                    mBleService.getLockStatus(et1.getText().toString().trim(), et2.getText().toString().trim().toUpperCase(), new OnGetLockStateListener() {

                        @Override
                        public void onResult(int power, boolean isNeedSyncTime) {
                            tv7.setText("锁电量：" + power + "%" + "\n是否需要时间同步：" + isNeedSyncTime);
                        }

                        @Override
                        public void onConnecting(int state) {
                            MainActivity.this.onConnecting(state, tv7);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button8: //清空数据
                keySeq = "";
                privateKey = "";
                activeKey = "";

                key1 = "";
                key2 = "";
                seq = "";

                tv1.setText("数据");
                tv3.setText("数据");
                tv5.setText("数据");
                tv7.setText("数据");
                tv2.setText("状态");
                tv4.setText("状态");
                tv6.setText("状态");
//                et1.setText("");
//                et2.setText("");
                break;
            default:
                break;
        }
    }

    private void onConnecting(int state, TextView tv) {
        MyLog.e("state:" + state/* + " current thread:" + Thread.currentThread().getName()*/);
        switch (state) {
            case BleService.STATE_GATT_CONNECTED:
                tv.setText("已连接");
                break;
            case BleService.STATE_GATT_CONNECTING:
                tv.setText("正在连接..");
                break;
            case BleService.STATE_GATT_DISCONNECTED:
                tv.setText("未连接");
                break;
            case BleService.STATE_SERVICE_DISCOVERED:
                tv.setText("已发现服务");
                break;
            default:
                break;
        }
    }
}
