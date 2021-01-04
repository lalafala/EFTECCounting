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
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.efteccounting.R;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

public class CountActivity extends  BaseActivity{
    private final static String ACTION_HONEYWLL = "com.honeywell";
    private static final String TAG = "CountActivity";
    private String current_username;
    private String warehouse_id;
    private int count=1;

    @BindView(R.id.Barcode)
    EditText Barcode;
    @BindView(R.id.Count)
    EditText Count;
    @BindView(R.id.infos)
    EditText infos;

    @BindView(R.id.post)
    Button post;


    @BindView(R.id.reset)
    Button reset;
    private String barcode="";
    private String info="";

    private Dialog loadingDialog;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_HONEYWLL)) {
                if (intent.hasExtra("data")) {
                    final String decode = intent.getStringExtra("data");
                    Log.v(TAG, decode);

                            if (!barcode.equals(decode) && !barcode.equals("")){
                                ToastUtils.showToast2("请先提交当前产品结果，再扫描其他产品");
                            }else {
                                showScandata(decode);
                            }
                }
            }
        }
    };
    private void showScandata(String decode){
        if (barcode.equals("")) {
            barcode=decode;
            Barcode.setText(decode);
        }
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
            Count.setText(String.valueOf(count));
            count++;
        }else {
            showToast("货物码格式错误!");
            barcode="";
        }
    }
    @Override
    protected int attachLayoutRes() {
        return R.layout.activity_count;
    }

    @Override
    public void initView() {
        super.initView();
        ToastUtils.init(this);
        current_username=  (String) SPUtils.get(Constants.current_username, "");
        warehouse_id=  (String) SPUtils.get(Constants.warehouse_id, "");
        tv_center_title.setVisibility(View.VISIBLE);
        tv_center_title.setText("盘点");
        tv_right_title.setText("结束盘点");
        tv_right_title.setVisibility(View.VISIBLE);
        iv_back.setVisibility(View.VISIBLE);
        registerReceiver(broadcastReceiver, new IntentFilter(ACTION_HONEYWLL));
        startCount();
    }

    private void startCount(){
        loadingDialog=   new ProgressDialog(CountActivity.this);
        loadingDialog.setTitle("开始盘点");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        String url = "http://s36309d676.qicp.vip/WebServiceForSqlserver.asmx/SubmitStartTakeInventory";
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.sslSocketFactory();
        RequestBody body = new FormBody.Builder()
                .add("WarehouseCode",warehouse_id)
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
                        showToast("开始盘点失败");
                        CommonUtil.exitActivityAndBackAnim(CountActivity.this,true);
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
                if (element.getData().toString().equals("true")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.dismiss();
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("开始盘点失败");
                            loadingDialog.dismiss();
                        }
                    });
                }
            }
        });
    }
    private void finishCount() {
        loadingDialog=   new ProgressDialog(CountActivity.this);
        loadingDialog.setTitle("结束盘点");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        String url = "http://s36309d676.qicp.vip/WebServiceForSqlserver.asmx/SubmitFinishTakeInventory";
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.sslSocketFactory();
        RequestBody body = new FormBody.Builder()
                .add("WarehouseCode",warehouse_id)
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
                        showToast("结束盘点失败");
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
                if (element.getData().toString().equals("true")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.dismiss();
                            CommonUtil.exitActivityAndBackAnim(CountActivity.this,true);
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("结束盘点失败");
                            loadingDialog.dismiss();
                        }
                    });
                }
            }
        });

    }
    private void upload(){

            loadingDialog = new ProgressDialog(CountActivity.this);
            loadingDialog.setTitle("正在上传数据");
            loadingDialog.setCancelable(false);
            loadingDialog.show();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String url = "http://s36309d676.qicp.vip/WebServiceForSqlserver.asmx/SubmitTakeInventoryData";
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.sslSocketFactory();
            RequestBody body = new FormBody.Builder()
                    .add("Barcode", barcode)
                    .add("WarehouseCode", warehouse_id)
                    .add("Count", Count.getText().toString())
                    .add("UserName", current_username)
                    .add("ScanDate", sdf.format(new Date()))
                    .build();
            final Request request = new Request.Builder()
                    .url(url)
                    .post(body)//默认就是GET请求，可以不写
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "onFailure: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast(e.getMessage());
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    Log.d(TAG, "onResponse: " + result);
                    Document document = null;
                    try {
                        document = DocumentHelper.parseText(result);
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                    Element element = document.getRootElement();
                    if (element.getData().toString().equals("true")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                barcode = "";
                                Count.setText("0");
                                Barcode.setText("扫描产品码");
                                infos.setText("产品信息");
                                count = 1;
                                showToast("上传成功!");
                                loadingDialog.dismiss();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast("上传失败!");
                                loadingDialog.dismiss();
                            }
                        });
                    }
                }
            });

    }

    @Override
    public void setListener() {
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (count == 1) {
                    showToast("没有盘点数据!");
                } else {
                    final androidx.appcompat.app.AlertDialog.Builder normalDialog = new AlertDialog.Builder(CountActivity.this);
                    normalDialog.setCancelable(false);
                    normalDialog.setTitle("上传确认");
                    normalDialog.setMessage("确认上传么？");
                    normalDialog.setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    upload();

                                }
                            });
                    normalDialog.setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                    normalDialog.show();

                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final androidx.appcompat.app.AlertDialog.Builder normalDialog = new AlertDialog.Builder(CountActivity.this);
                normalDialog.setCancelable(false);
                normalDialog.setTitle("重扫");
                normalDialog.setMessage("确认重扫么？");
                normalDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                barcode = "";
                                Count.setText("0");
                                Barcode.setText("扫描产品码");
                                infos.setText("产品信息");
                                count = 1;
                            }
                        });
                normalDialog.setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                normalDialog.show();

            }
        });
        tv_right_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Count.getText().toString().equals("") || Count.getText().toString().equals("0")) {
                    final androidx.appcompat.app.AlertDialog.Builder normalDialog = new AlertDialog.Builder(CountActivity.this);
                    normalDialog.setCancelable(false);
                    normalDialog.setTitle("结束盘点");
                    normalDialog.setMessage("确认结束盘点么？");
                    normalDialog.setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finishCount();
                                }
                            });
                    normalDialog.setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                    normalDialog.show();

                }else {
                    showToast("还有盘点数据未上传");
                }
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final androidx.appcompat.app.AlertDialog.Builder normalDialog = new AlertDialog.Builder(CountActivity.this);
                normalDialog.setCancelable(false);
                normalDialog.setTitle("退出");
                normalDialog.setMessage("确认退出么，扫描到数据都会消失？");
                normalDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CommonUtil.exitActivityAndBackAnim(CountActivity.this,true);

                            }
                        });
                normalDialog.setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                normalDialog.show();

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

        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(CountActivity.this);
        normalDialog.setCancelable(false);
        normalDialog.setTitle("退出");
        normalDialog.setMessage("确认退出么，扫描到数据都会消失？");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CommonUtil.exitActivityAndBackAnim(CountActivity.this,true);

                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        normalDialog.show();

    }
}
