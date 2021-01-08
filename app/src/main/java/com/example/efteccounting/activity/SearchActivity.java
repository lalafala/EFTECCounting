package com.example.efteccounting.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.example.efteccounting.R;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import utils.SPUtils;
import utils.ToastUtils;

public class SearchActivity extends  BaseActivity{
    private final static String ACTION_HONEYWLL = "com.honeywell";
    private static final String TAG = "CountActivity";
    private String current_username;
    private String warehouse_id;

    private String url_con=SPUtils.get("url_con","").toString();
    @BindView(R.id.Barcode)
    EditText Barcode;
    @BindView(R.id.Count)
    EditText Count;
    @BindView(R.id.infos)
    EditText infos;





    private String barcode="";
    private String info="";

    private Dialog loadingDialog;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_HONEYWLL)) {
                if (intent.hasExtra("data")) {
                    final String decode = intent.getStringExtra("data");
                    Log.v(TAG, decode);


                                showScandata(decode);
                                getCount(decode);

                }
            }
        }
    };
    private void showScandata(String decode){

        String infosString[]=decode.split(";");
        if (infosString.length==6) {
            for (int i = 0; i < infosString.length; i++) {
                switch (i) {
                    case 0:
                        info = "日期:" + infosString[i] + "\n";
                        break;
                    case 1:
                        info = info + "仓库代号:" + infosString[i] + "\n";
                        break;
                    case 2:
                        info = info + "材料代号:" + infosString[i] + "\n";
                        break;
                    case 3:
                        info = info + "批次号:" + infosString[i] + "\n";
                        break;
                    case 4:
                        info = info + "数量:" + infosString[i] + "\n";
                        break;
                    case 5:
                        info = info + "单位:" + infosString[i];
                        break;
                }
            }
            infos.setText(info);
        }else {
            showToast("货物码格式错误!");
        }
    }
    @Override
    protected int attachLayoutRes() {
        return R.layout.activity_search;
    }

    @Override
    public void initView() {
        super.initView();
        ToastUtils.init(this);
        current_username=  (String) SPUtils.get(Constants.current_username, "");
        warehouse_id=  (String) SPUtils.get(Constants.warehouse_id, "");
        tv_center_title.setVisibility(View.VISIBLE);
        tv_center_title.setText("查询");
        iv_back.setVisibility(View.VISIBLE);
        registerReceiver(broadcastReceiver, new IntentFilter(ACTION_HONEYWLL));
    }

    private void getCount(String decode){
        loadingDialog=   new ProgressDialog(SearchActivity.this);
        loadingDialog.setTitle("查询中");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        String url = url_con+"QueryGoodsStatus";
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5,TimeUnit.SECONDS).build();
        okHttpClient.sslSocketFactory();
        RequestBody body = new FormBody.Builder()
                .add("WarehouseCode",warehouse_id)
                .add("Barcode",decode)
                .build();
        final Request request = new Request.Builder()
                .url(url)
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: "+e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(e.getMessage());
                        showToast("查询失败");
                        loadingDialog.dismiss();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result=response.body().string();
                Log.d(TAG, "onResponse: "+result);
                Document document = null;
                try {
                    document = DocumentHelper.parseText(result);
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
                Element element= document.getRootElement();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Count.setText(element.getData().toString());
                            loadingDialog.dismiss();
                        }
                    });
            }
        });
    }


    @Override
    public void setListener() {


        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                                CommonUtil.exitActivityAndBackAnim(SearchActivity.this,true);

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(ACTION_HONEYWLL));
    }

    @Override
    public void onBackPressed() {

                        CommonUtil.exitActivityAndBackAnim(SearchActivity.this,true);


    }
}
