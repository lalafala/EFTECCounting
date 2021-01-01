package com.example.efteccounting.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
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
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import utils.CommonUtil;
import utils.SPUtils;
import utils.ToastUtils;

public class LoginActivity extends BaseActivity {
    private static String TAG="LoginActivity";
    private long mExitTime = 0;
    private Map<String,String> warehouseidMaps;
    private List<String> dataset ;
    private NiceSpinner niceSpinner ;
    @BindView(R.id.bt_login)
    public Button bt_login;

    @BindView(R.id.bt_reload)
    public Button bt_reload;
    @BindView(R.id.info)
    TextView info;
    private EditText username;
    private EditText password;
    Dialog loadingDialog;
    private boolean canLogin=false;
    @Override
    protected int attachLayoutRes() {
        return R.layout.activity_login;
    }

    @Override
    public void setListener() {
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
                        String url = "http://s36309d676.qicp.vip/WebServiceForSqlserver.asmx/UserLogin";
                        OkHttpClient okHttpClient = new OkHttpClient();
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
                        Toast.makeText(getApplicationContext(), "请输入用户名", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    showToast("查看网络连接!");
                }
            }
        });

    }
    @Override
    public void initData() {
        super.initData();


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
        String url = "http://s36309d676.qicp.vip/WebServiceForSqlserver.asmx/GetWarehouseID";
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.sslSocketFactory();
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
                            for (int i = 0; i < ids.length; i++) {
                                warehouseidMaps.put(ids[i].substring(ids[i].indexOf("/") + 1, ids[i].length()), ids[i].substring(0, ids[i].indexOf("/")));
                                dataset.add(ids[i].substring(ids[i].indexOf("/") + 1, ids[i].length()));
                            }
                            niceSpinner.attachDataSource(dataset);
                            Log.v(TAG, warehouseidMaps.toString());
                            canLogin = true;
                            loadingDialog.dismiss();
                        } catch (DocumentException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });
    }
    @Override
    public void initView() {
        super.initView();
        ToastUtils.init(this);
        niceSpinner= (NiceSpinner)findViewById(R.id.nice_spiner);
        username=(EditText)findViewById(R.id.username);
        username.setText("EFT003");
        password=(EditText)findViewById(R.id.password);
        password.setText("12345");
        loadData();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /* PermissionUtils.isGrantExternalRW(this,1);*/

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
}