package com.example.efteccounting.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.efteccounting.R;

import java.util.ArrayList;
import java.util.List;

import adapter.FunTypeAdapter;
import bean.FunctionType;
import butterknife.BindView;
import utils.CommonUtil;

/**
 * Created by apple on 17/9/29.
 */

public class MainActivity extends BaseActivity {
    private long mExitTime = 0;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    private FunTypeAdapter mAdapter;
    private static int REQUESTCODE_FROM_ACTIVITY = 1000;
    private List<String> pathList;
    private String user_name;
    Dialog loadingDialog;
    private ArrayList<FunctionType> mList = new ArrayList<>();


    @Override
    protected int attachLayoutRes() {
        return R.layout.activity_main2;
    }



    @Override
    public void initView() {
        super.initView();
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        View v = LayoutInflater.from(this).inflate(R.layout.top_iv,null);
        FunctionType functionType=new FunctionType();
        functionType.des="收货入库";
        functionType.img=R.mipmap.p2;
        FunctionType functionType1=new FunctionType();
        functionType1.des="发退";
        functionType1.img=R.mipmap.down;
        FunctionType functionType2=new FunctionType();
        functionType2.des="收退";
        functionType2.img=R.mipmap.up;
        FunctionType functionType3=new FunctionType();
        functionType3.des="发货出库";
        functionType3.img=R.mipmap.p3;
        FunctionType functionType4=new FunctionType();
        functionType4.des="盘点";
        functionType4.img=R.mipmap.l2;
        FunctionType functionType5=new FunctionType();
        functionType5.des="查询";
        functionType5.img=R.mipmap.l1;
        mList.add(functionType);
        mList.add(functionType2);
        mList.add(functionType3);
        mList.add(functionType1);
        mList.add(functionType4);
        mList.add(functionType5);
        mAdapter = new FunTypeAdapter(R.layout.item_iv_tv1,mList);
        mAdapter.addHeaderView(v);
        recyclerView.setAdapter(mAdapter);
    }


    @Override
    public void initData() {

    }

    @Override
    public void onBackPressed() {

        if (System.currentTimeMillis() - mExitTime > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    public void initTitle() {
        tv_center_title.setVisibility(View.VISIBLE);
        tv_center_title.setText("首页");
    }

    @Override
    public void setListener() {
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                switch (position) {
                    case 0:
                        //入库
                       Intent intent = new Intent(MainActivity.this, StorageInActivity.class);
                        startActivity(intent);
                        CommonUtil.openNewActivityAnim(MainActivity.this, false);
                        break;
                    case 3:
                        break;
                    case 1:
                        break;
                    case 2:
                        //入库
                        Intent intent2 = new Intent(MainActivity.this, StorageOutActivity.class);
                        startActivity(intent2);
                        CommonUtil.openNewActivityAnim(MainActivity.this, false);
                       break;
                    case 4:
                        Intent intent4 = new Intent(MainActivity.this, CountActivity.class);
                        startActivity(intent4);
                        CommonUtil.openNewActivityAnim(MainActivity.this, false);
                       break;
                    case 5:
                       break;
                }
            }
        });
    }


}
