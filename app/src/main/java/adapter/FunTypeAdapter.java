package adapter;



import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.efteccounting.R;

import java.util.List;

import bean.FunctionType;

public class FunTypeAdapter extends BaseQuickAdapter<FunctionType, BaseViewHolder> {

    public FunTypeAdapter(int layoutResId, @Nullable List data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, FunctionType item) {
        helper.setImageResource(R.id.iv, item.img)
                .setText(R.id.tv, item.des);
    }


}
