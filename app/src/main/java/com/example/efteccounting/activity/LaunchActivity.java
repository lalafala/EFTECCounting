package com.example.efteccounting.activity;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.example.efteccounting.R;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LaunchActivity extends BaseActivity{

    private static String TAG="LaunchActivity";
    @BindView(R.id.txtver)
    TextView txtver;


    @Override
    protected int attachLayoutRes() {
        return R.layout.activity_launch;
    }

    @Override
    public void initData() {
        super.initData();

        String url = "http://s36309d676.qicp.vip/WebServiceForSqlserver.asmx/DBTest";
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
                Log.d(TAG, "onFailure: "+e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "onResponse: " + response.body().string());
                
            }
        });
      /*  File file =new File(Environment.getExternalStorageDirectory() +"/import");
        if (!file.exists())
        {
            file.mkdir();
        }

        file =new File(Environment.getExternalStorageDirectory() +"/export");
        if (!file.exists())
        {
            file.mkdir();
        }*/
      /* String filePath = this.getExternalCacheDir().getPath()+"/import";
        File file = new File(filePath);
        if (!file.exists()) {
            // 创建文件夹
            file.mkdirs();
        }
        filePath = this.getExternalCacheDir().getPath()+"/export";
      file = new File(filePath);
        if (!file.exists()) {
            // 创建文件夹
            file.mkdirs();
        }*/
        new Handler().postDelayed(new Runnable() {
            public void run() {

                startActivity(LoginActivity.class);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }

        }, 1000 * 2);
    }

    @Override
    public void initView() {
        super.initView();
        try {
            txtver.setText(getPackageManager().
                    getPackageInfo(getPackageName(), 0).versionName);
        }catch (Exception e)
        {e.printStackTrace();}
    }

    @Override
    public void setListener() {

    }

}
