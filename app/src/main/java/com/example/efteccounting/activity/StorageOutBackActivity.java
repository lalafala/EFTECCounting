package com.example.efteccounting.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.tech.MifareClassic;
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

public class StorageOutBackActivity extends  BaseActivity{
    private final static String ACTION_HONEYWLL = "com.honeywell";
    private static final String TAG = "StorageOutBackActivity";
    private String current_username;
    private String warehouse_id;
    private int count=1;
    private boolean isorderNo =true;
    private float checkCount=0;
    private boolean checked=false;
    private boolean isscaned=false;
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
    @BindView(R.id.BackWarhouse)
    EditText BackWarhouse;
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
                    if (isorderNo){
                        if (decode.contains(";")) {
                            showToast2("订单格式错误!");
                        }else {
                            final AlertDialog.Builder normalDialog = new AlertDialog.Builder(StorageOutBackActivity.this);
                            normalDialog.setCancelable(false);
                            normalDialog.setTitle("信息确认");
                            //   normalDialog.setMessage("订单号:"+decode+"\n对应的仓库为:"+wareto.get().toString());
                            normalDialog.setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            OrderNumber.setText(decode);
                                            OrderNumber.setEnabled(false);
                                            isorderNo = false;
                                            isscaned = true;
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
                            if (!checked) {
                                getcount(decode);
                            }
                        }
                    } }
            }
        }
    };

    private void getcount(String decode){
        Dialog loadingDialog1=   new ProgressDialog(StorageOutBackActivity.this);
        loadingDialog1.setTitle("获取库房数据");
        loadingDialog1.setCancelable(false);
        loadingDialog1.show();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
        String url = "http://s36309d676.qicp.vip/WebServiceForSqlserver.asmx/GetTransferOutCheckCount";
        OkHttpClient okHttpClient = new OkHttpClient();
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
                        loadingDialog1.dismiss();
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
                checkCount=Float.valueOf(element.getData().toString());
                Log.v(TAG,String.valueOf(checkCount));
                checked=true;
                loadingDialog1.dismiss();
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
        tv_center_title.setText("出库退货");
        iv_back.setVisibility(View.VISIBLE);

        registerReceiver(broadcastReceiver, new IntentFilter(ACTION_HONEYWLL));

    }

    @Override
    public void setListener() {
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isscaned) {
                    final AlertDialog.Builder normalDialog = new AlertDialog.Builder(StorageOutBackActivity.this);
                    normalDialog.setCancelable(false);
                    normalDialog.setTitle("退出");
                    normalDialog.setMessage("确认退出么，扫描到数据都会消失？");
                    normalDialog.setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    CommonUtil.exitActivityAndBackAnim(StorageOutBackActivity.this, true);

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
                    CommonUtil.exitActivityAndBackAnim(StorageOutBackActivity.this, true);

                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder normalDialog = new AlertDialog.Builder(StorageOutBackActivity.this);
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
                                checkCount=0;
                                isscaned=false;
                                loadingDialog.dismiss();
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
                if (count==1) {
                    showToast("没有出库退回数据！");
                }else {

                    final AlertDialog.Builder normalDialog = new AlertDialog.Builder(StorageOutBackActivity.this);
                    normalDialog.setCancelable(false);
                    normalDialog.setTitle("确认");
                    normalDialog.setMessage("确认提交么？");
                    normalDialog.setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Float.valueOf(Count.getText().toString())<=checkCount) {
                                        upload();
                                    }else{
                                        final androidx.appcompat.app.AlertDialog.Builder normalDialog = new AlertDialog.Builder(StorageOutBackActivity.this);
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
                                    }
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

            loadingDialog=   new ProgressDialog(StorageOutBackActivity.this);
            loadingDialog.setTitle("正在上传数据");
            loadingDialog.setCancelable(false);
            loadingDialog.show();
            String url = "http://s36309d676.qicp.vip/WebServiceForSqlserver.asmx/SubmitTransferBackOutData";
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.sslSocketFactory();
            RequestBody body = new FormBody.Builder()
                    .add("OrderNumber",OrderNumber.getText().toString())
                    .add("Barcode",barcode)
                    .add("WarehouseCodeFrom",warehouse_id)
                    .add("WarehouseCodeTo",warehouse_id)
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
                                isscaned=false;
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
        if (isscaned) {
            final AlertDialog.Builder normalDialog = new AlertDialog.Builder(StorageOutBackActivity.this);
            normalDialog.setCancelable(false);
            normalDialog.setTitle("退出");
            normalDialog.setMessage("确认退出么，扫描到数据都会消失？");
            normalDialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CommonUtil.exitActivityAndBackAnim(StorageOutBackActivity.this, true);

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
            CommonUtil.exitActivityAndBackAnim(StorageOutBackActivity.this, true);

        }

    }
}
