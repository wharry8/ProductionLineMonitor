package com.production.w.productionlinemonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zgkxzx.modbus4And.requset.ModbusParam;
import com.zgkxzx.modbus4And.requset.ModbusReq;
import com.zgkxzx.modbus4And.requset.OnRequestBack;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "LoginActivity";

    EditText et_host;
    EditText et_password;
    EditText et_port;
    Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init ();
    }
    public void init () {
        et_host = findViewById(R.id.login_et_host);
        et_port = findViewById(R.id.login_et_port);
        et_password = findViewById(R.id.login_et_password);
        btn_login = findViewById(R.id.login_btn_login);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input_host = et_host.getText().toString();
                String input_port = et_port.getText().toString();
                String input_password = et_password.getText().toString();

                input_host = input_host.trim();
                input_port = input_port.trim();
                input_password = input_password.trim();

                // todo
                // 1. 检查Ip,端口的格式是否正确
                // 2. 检查密码
                // 3. 尝试连接
                // 4. 成功则进入，失败则返回

                /*
                Log.e(TAG, "onClick: 1");
                if (!input_host.equals(getString(R.string.host))) {
                    Toast.makeText(getApplicationContext(), "请输入正确的服务器地址", Toast.LENGTH_LONG);
                }
                if (!input_port.equals(getString(R.string.port))) {
                    Toast.makeText(getApplicationContext(), "请输入正确的端口", Toast.LENGTH_LONG);
                    return;
                }
                if (!input_password.equals(getString(R.string.password))) {
                    Toast.makeText(getApplicationContext(), "请输入正确的密码", Toast.LENGTH_LONG);
                    return;
                }
                */
                 ModbusReq.getInstance().setParam(new ModbusParam()
                .setHost(input_host)
                .setPort(Integer.valueOf(input_port))
                .setEncapsulated(false)
                .setKeepAlive(true)
                .setTimeout(2000)
                .setRetries(0))
                .init(new OnRequestBack<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "onSuccess 连接服务器成功" + s);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                    @Override
                    public void onFailed(String msg) {
                        Log.d(TAG, "onFailed 连接服务器失败" + msg);
                        Toast.makeText(LoginActivity.this, "can't connect to server.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }
}
