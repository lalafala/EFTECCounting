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

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import utils.SPUtils;
import utils.ToastUtils;

public class StorageInBackActivity extends  BaseActivity{
    private final static String ACTION_HONEYWLL = "com.honeywell";
    private static final String TAG = "StorageInBackActivity";
    private String current_username;
    private String warehouse_id;
    private int count=1;
    private float checkCount=-1;
    private boolean checked=false;
    private boolean isorderNo =true;
    private boolean isscanned=false;
    private Map<String,String> warehouseidMaps=new HashMap<>();
    List<String> dataset = new LinkedList<>();
    @BindView(R.id.OrderNumber)
    TextView OrderNumber;
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
    @BindView(R.id.nice_spiner)
    NiceSpinner niceSpinner;
    private String url_con=SPUtils.get("url_con","").toString();
    private String barcode="";
    private String info="";
    private String wareto;
    private Dialog loadingDialog;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_HONEYWLL)) {
                if (intent.hasExtra("data")) {
                    final String decode = intent.getStringExtra("data");
                    Log.v(TAG, decode);
                    if (niceSpinner.getText().toString().equals("")){
                        showToast("先选择库房");
                        return;
                    }
                    if (isorderNo){
                        if (decode.contains(";")){
                                showToast2("订单格式错误!");
                        }else  {
                            final AlertDialog.Builder normalDialog = new AlertDialog.Builder(StorageInBackActivity.this);
                            normalDialog.setCancelable(false);
                            normalDialog.setTitle("信息确认");
                            normalDialog.setMessage("订单号:" + decode + "\n对应的仓库为:" + niceSpinner.getText().toString());
                            normalDialog.setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            OrderNumber.setText(decode);
                                            OrderNumber.setEnabled(false);
                                            isorderNo = false;
                                            isscanned=true;
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
                    }else {
                        if (!barcode.equals(decode) && !barcode.equals("")){
                            ToastUtils.showToast2("请先提交当前产品结果，再扫描其他产品");
                        }else {

                                showScandata(decode);
                                /*if (!checked) {
                                    getcount(decode);
                                }*/
                        }
                    } }
            }
        }
    };

    private void getcount(String decode){

        loadingDialog=   new ProgressDialog(StorageInBackActivity.this);
        loadingDialog.setTitle("获取库房数据");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        String url = url_con+"GetReceiptIntoCheckCount";
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5,TimeUnit.SECONDS).build();
        okHttpClient.sslSocketFactory();
        RequestBody body = new FormBody.Builder()
                .add("OrderNumber",OrderNumber.getText().toString())
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
                        showToast("超时失败!");
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
                    loadingDialog.dismiss();
                    showToast("数据错误!");
                }
                Element element= document.getRootElement();
                checkCount=Float.valueOf(element.getData().toString());
                Log.v(TAG,String.valueOf(checkCount));
                checked=true;
                loadingDialog.dismiss();
            }
        });
    }
    private void showScandata(String decode){
        if (barcode.equals("")) {
            barcode=decode;
            Barcode.setText(decode);
            OrderNumber.setEnabled(false);
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
        return R.layout.activity_storageout;
    }


    @Override
    public void initData() {
        super.initData();

    }

    @Override
    public void initView() {
        super.initView();
        ToastUtils.init(this);
        current_username=  (String) SPUtils.get(Constants.current_username, "");
        warehouse_id=  (String) SPUtils.get(Constants.warehouse_id, "");
        tv_center_title.setVisibility(View.VISIBLE);
        tv_center_title.setText("入库退货");
        iv_back.setVisibility(View.VISIBLE);

        registerReceiver(broadcastReceiver, new IntentFilter(ACTION_HONEYWLL));
        loadingDialog=   new ProgressDialog(StorageInBackActivity.this);
        loadingDialog.setTitle("获取库房信息");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        String url = url_con+"GetWarehouseID";
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
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
                        showToast("无法获取库房列表");
                        CommonUtil.exitActivityAndBackAnim(StorageInBackActivity.this, true);
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
                            int j=0;
                            boolean notempty=false;
                            for (int i = 0; i < ids.length; i++) {
                                notempty=true;
                                warehouseidMaps.put(ids[i].substring(ids[i].indexOf("/") + 1, ids[i].length()), ids[i].substring(0, ids[i].indexOf("/")));
                                dataset.add(ids[i].substring(ids[i].indexOf("/") + 1, ids[i].length()));
                                if((ids[i].substring(ids[i].indexOf("/") + 1, ids[i].length()).equals("再生品仓库"))){
                                    j=i;
                                }
                            }
                            niceSpinner.attachDataSource(dataset);
                            if (notempty) {
                                niceSpinner.setSelectedIndex(j);
                            }
                            loadingDialog.dismiss();
                            Log.v(TAG, warehouseidMaps.toString());
                        } catch (DocumentException e) {
                            e.printStackTrace();
                            showToast("获取库房信息失败!");
                            loadingDialog.dismiss();
                            CommonUtil.exitActivityAndBackAnim(StorageInBackActivity.this,true);
                        }

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
                if (isscanned) {
                    final AlertDialog.Builder normalDialog = new AlertDialog.Builder(StorageInBackActivity.this);
                    normalDialog.setCancelable(false);
                    normalDialog.setTitle("退出");
                    normalDialog.setMessage("确认退出么，扫描到数据都会消失？");
                    normalDialog.setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    CommonUtil.exitActivityAndBackAnim(StorageInBackActivity.this, true);

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
                    CommonUtil.exitActivityAndBackAnim(StorageInBackActivity.this, true);

                }

            }
        });

        niceSpinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                // This example uses String, but your type can be any
                wareto = parent.getItemAtPosition(position).toString();
                Log.v(TAG,wareto);
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder normalDialog = new AlertDialog.Builder(StorageInBackActivity.this);
                normalDialog.setCancelable(false);
                normalDialog.setTitle("重置确认");
                normalDialog.setMessage("确认重扫么，扫描到数据都会消失？");
                normalDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                isorderNo = true;
                                barcode = "";
                                Count.setText("0");
                                count=1;
                                OrderNumber.setText("扫描订单号");
                                Barcode.setText("扫描产品码");
                                infos.setText("产品信息");
                                checked=false;
                                isscanned=false;
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



        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (count == 1) {
                    showToast("没有入库退货数据！");
                } else {

                    final androidx.appcompat.app.AlertDialog.Builder normalDialog = new AlertDialog.Builder(StorageInBackActivity.this);
                    normalDialog.setCancelable(false);
                    normalDialog.setTitle("确认");
                    normalDialog.setMessage("确认提交么？");
                    normalDialog.setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    upload();
                                    /*if (Float.valueOf(Count.getText().toString())<=checkCount) {
                                        upload();
                                    }else{
                                        final androidx.appcompat.app.AlertDialog.Builder normalDialog = new AlertDialog.Builder(StorageInBackActivity.this);
                                        normalDialog.setCancelable(false);
                                        normalDialog.setTitle("确认");
                                        normalDialog.setMessage("扫描数目大于库存，确认提交么？");
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
                                    }*/
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
    }

    private void upload(){
            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
            String warename=niceSpinner.getText().toString();
            loadingDialog=   new ProgressDialog(StorageInBackActivity.this);
            loadingDialog.setTitle("正在上传数据");
            loadingDialog.setCancelable(false);
            loadingDialog.show();
            String url = url_con+"SubmitReceiptBackIntoData";
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5,TimeUnit.SECONDS).build();
            okHttpClient.sslSocketFactory();
            RequestBody body = new FormBody.Builder()
                    .add("OrderNumber",OrderNumber.getText().toString())
                    .add("Barcode",barcode)
                    .add("WarehouseCodeFrom",warehouse_id)
                    .add("WarehouseCodeTo", warehouseidMaps.get(warename).toString())
                    .add("Count",Count.getText().toString())
                    .add("UserName",current_username)
                    .add("ScanDate",sdf.format(new Date()))
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
                            showToast("上传失败，检查网络!");
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
                        loadingDialog.dismiss();
                        showToast("数据获取失败!");
                        e.printStackTrace();
                    }
                    Element element = document.getRootElement();
                    if (element.getData().toString().equals("true")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isorderNo = true;
                                barcode = "";
                                Count.setText("0");
                                OrderNumber.setText("扫描订单号");
                                count = 1;
                                Barcode.setText("扫描产品码");
                                infos.setText("产品信息");
                                showToast("上传成功!");
                                checked=false;
                                checkCount=0;
                                isscanned=false;
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
if (isscanned) {
    final AlertDialog.Builder normalDialog = new AlertDialog.Builder(StorageInBackActivity.this);
    normalDialog.setCancelable(false);
    normalDialog.setTitle("退出");
    normalDialog.setMessage("确认退出么，扫描到数据都会消失？");
    normalDialog.setPositiveButton("确定",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CommonUtil.exitActivityAndBackAnim(StorageInBackActivity.this, true);

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
    CommonUtil.exitActivityAndBackAnim(StorageInBackActivity.this, true);

}
    }
}
