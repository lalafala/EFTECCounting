package com.example.efteccounting.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.efteccounting.R;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import bean.Constants;
import butterknife.BindView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import utils.CommonUtil;
import utils.OkhttpClientUtil;
import utils.PermissionUtils;
import utils.SPUtils;
import utils.StreamUtils;
import utils.ToastUtils;

public class LoginActivity extends BaseActivity {
    private static String TAG="LoginActivity";
    private long mExitTime = 0;
    private Map<String,String> warehouseidMaps;
    private List<String> dataset ;
    private NiceSpinner niceSpinner ;
    @BindView(R.id.bt_login)
    public Button bt_login;
    @BindView(R.id.bt_set)
    public Button bt_set;
    @BindView(R.id.bt_reload)
    public Button bt_reload;
    @BindView(R.id.info)
    TextView info;
    private boolean isopen=false;
    private EditText username;
    private EditText password;
    private String url_con;
    Dialog loadingDialog;
    private boolean canLogin=false;
    EditText input;
    private final static String ACTION_HONEYWLL = "com.honeywell";

    @Override
    protected int attachLayoutRes() {
        return R.layout.activity_login;
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_HONEYWLL)) {
                if (intent.hasExtra("data")) {
                    final String decode = intent.getStringExtra("data");
                    Log.v(TAG, decode);
                    if (isopen) {
                        if (decode.startsWith("http://") ||decode.startsWith("https://") ) {
                            input.setText(decode);
                        }else {
                            showToast("服务器地址必须以http://或者https://开头");
                        }
                    }

                }
            }
        }
    };
    @Override
    public void setListener() {
        bt_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isopen=true;
                final AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
                alert.setCancelable(false);
              //  url_con=SPUtils.get("url_con","").toString();
                input=new EditText(LoginActivity.this);
                if (url_con.equals("")){
                    url_con="https://as-barcode.eftec.com.cn/WebServiceForSqlserver.asmx/";
                }
                input.setText(url_con);
               alert.setView(input);
                alert.setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String value = input.getText().toString().trim();
                        if (value.startsWith("http://") ||value.startsWith("https://")) {
                            url_con=value;
                            try {
                                StreamUtils.write(value);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            test();
                        }else {
                            showToast("服务器地址必须以http://或者https://开头");
                        }
                        isopen=false;
                    }
                });

                alert.setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                isopen=false;
                            }
                        });
                alert.show();

            }
        });
        bt_reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });

        niceSpinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                // This example uses String, but your type can be any
                String item = parent.getItemAtPosition(position).toString();
                Log.v(TAG,item);
            }
        });
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //登录信息验证
                if (canLogin) {
                    String login_user = username.getText().toString();
                    String password_user = password.getText().toString();
                    String warhouse = niceSpinner.getText().toString();
                    String warhouseid = warehouseidMaps.get(warhouse).toString();
                    Log.v(TAG, login_user + " " + password_user + " " + warhouseid);
                    if (!login_user.equals("") && !password_user.equals("") && !warhouseid.equals("")) {
                        loadingDialog = new ProgressDialog(LoginActivity.this);
                        loadingDialog.setTitle("登录...");
                        loadingDialog.setCancelable(false);
                        loadingDialog.show();
                        String url = url_con+"UserLogin";
                        //String url = "https://as-barcode.eftec.com.cn/WebServiceForSqlserver.asmx/"+"UserLogin";
                        OkHttpClient okHttpClient =OkhttpClientUtil.getUnsafeOkHttpClient();
                        okHttpClient.newBuilder()
                                .connectTimeout(5, TimeUnit.SECONDS)
                                .readTimeout(5,TimeUnit.SECONDS).build();
                        okHttpClient.sslSocketFactory();
                        RequestBody body = new FormBody.Builder()
                                .add("User", login_user)
                                .add("Password", password_user)
                                .add("WarehouseCode", warhouseid).build();
                        final Request request = new Request.Builder()
                                .url(url)
                                .post(body)//默认就是GET请求，可以不写
                                .build();
                        Call call = okHttpClient.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG, "onFailure: " + e.getMessage());
                                        loadingDialog.dismiss();
                                        showToast("连接服务器失败，请检查网络或者设置服务器地址");
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {

                                String warehouseIds = response.body().string();
                                Document document = null;
                                try {
                                    document = DocumentHelper.parseText(warehouseIds);
                                } catch (DocumentException e) {
                                    e.printStackTrace();
                                }
                                Element element = document.getRootElement();
                                if (element.getData().toString().equals("true")) {
                                    SPUtils.put("current_username", login_user);
                                    SPUtils.put("warehouse_id", warhouseid);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            loadingDialog.dismiss();
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            CommonUtil.openNewActivityAnim(LoginActivity.this, false);
                                        }
                                    });
                                } else {
                                    loadingDialog.dismiss();
                                    info.setText("请核对库房，账号密码!");
                                }

                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "请输入用户名或密码", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    showToast("查看网络或者设置服务器地址并点击重新获取按钮!");
                }
            }

        });

    }
    @Override
    public void initData() {
        super.initData();
        ToastUtils.init(this.getApplicationContext());
    }



    @Override
    public void initTitle() {
      
       tv_center_title.setVisibility(View.VISIBLE);
       tv_center_title.setText("登   录");
    }

    private void _exit() {
        if (System.currentTimeMillis() - mExitTime > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }
    private void loadData(){
         warehouseidMaps=new HashMap<>();
         dataset = new LinkedList<>();
        loadingDialog=   new ProgressDialog(LoginActivity.this);
        loadingDialog.setTitle("获取库房信息");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        Log.e(TAG,url_con);
       String url = url_con+"GetWarehouseID";
       // String url = "https://as-barcode.eftec.com.cn/WebServiceForSqlserver.asmx/"+"GetWarehouseID";
        OkHttpClient okHttpClient =OkhttpClientUtil.getUnsafeOkHttpClient();
        okHttpClient.newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5,TimeUnit.SECONDS).build();
        okHttpClient.sslSocketFactory();
        try {
            final Request request = new Request.Builder()
                    .url(url)
                    .get()//默认就是GET请求，可以不写
                    .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                        loadingDialog.dismiss();
                        showToast("连接服务器失败，请检查网络或者设置服务器地址");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String warehouseIds=response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Document document = DocumentHelper.parseText(warehouseIds);
                            Element element = document.getRootElement();
                            String ids[] = element.getData().toString().split(";");
                            if (ids.length>1) {
                                for (int i = 0; i < ids.length; i++) {
                                    warehouseidMaps.put(ids[i].substring(ids[i].indexOf("/") + 1, ids[i].length()), ids[i].substring(0, ids[i].indexOf("/")));
                                    dataset.add(ids[i].substring(ids[i].indexOf("/") + 1, ids[i].length()));
                                }
                                niceSpinner.attachDataSource(dataset);
                                Log.v(TAG, warehouseidMaps.toString());
                                canLogin = true;
                                loadingDialog.dismiss();
                            }else {
                                loadingDialog.dismiss();
                                showToast("网址错误，请设置正确的服务器地址!");
                            }
                        } catch (DocumentException e) {
                            loadingDialog.dismiss();
                            showToast("数据验证失败，请设置正确的服务器地址!");
                            e.printStackTrace();
                        }
                    }
                });

            }
        });}catch (Exception e){
            loadingDialog.dismiss();
            showToast("地址格式错误，请检查!");
        }
    }

    private void test(){
        loadingDialog=   new ProgressDialog(LoginActivity.this);
        loadingDialog.setTitle("测试服务器地址");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        String url = url_con+"DBTest";
        //String url = "https://as-barcode.eftec.com.cn/WebServiceForSqlserver.asmx/"+"DBTest";
        OkHttpClient okHttpClient =OkhttpClientUtil.getUnsafeOkHttpClient();
        okHttpClient.newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5,TimeUnit.SECONDS).build();
        okHttpClient.sslSocketFactory();
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: "+e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismiss();
                        showToast("网址错误，请设置正确的服务器地址!");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String result = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onResponse: " + result);
                        if (!result.startsWith("<!DOCTYPE html>")) {
                            try {
                                Document document = DocumentHelper.parseText(result);
                                Element element = document.getRootElement();
                                String values = element.attributeValue("string");
                                if (element.getData().toString().equals("admin/EFT001/EFT002/EFT003/EFT004/")) {
                                    loadingDialog.dismiss();
                                    showToast("网址设置成功,点击重新获取按钮!");
                                } else {
                                    loadingDialog.dismiss();
                                    showToast("网址错误，请设置正确的服务器地址!");
                                }
                            } catch (Exception e) {
                                loadingDialog.dismiss();
                                showToast("网址错误，请设置正确的服务器地址!");
                            }
                        } else {
                            loadingDialog.dismiss();
                            //  bt_login.setEnabled(false);
                            showToast("网址错误，请设置正确的服务器地址!");
                        }
                    }
                });
            }
        });
    }
    @Override
    public void initView() {
        super.initView();
       url_con= StreamUtils.read();
        ToastUtils.init(this);
        input=new EditText(LoginActivity.this);
        niceSpinner= (NiceSpinner)findViewById(R.id.nice_spiner);
        username=(EditText)findViewById(R.id.username);
        password=(EditText)findViewById(R.id.password);

        loadData();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionUtils.isGrantExternalRW(this,1);
        registerReceiver(broadcastReceiver, new IntentFilter(ACTION_HONEYWLL));

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //检验是否获取权限，如果获取权限，外部存储会处于开放状态，会弹出一个toast提示获得授权
                    String sdCard = Environment.getExternalStorageState();

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "未获取权限，请手动设置存储权限", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(ACTION_HONEYWLL));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}