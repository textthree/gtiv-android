package override;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

// 重写 requestLayout() 解决更新了数据后 rn 上需要滑一下或者改变组件 key 才刷新问题
public class RnRecyclerView extends RecyclerView {
    private boolean mRequestedLayout = false;

    public RnRecyclerView(@NonNull Context context) {
        super(context);
    }

    public RnRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RnRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void requestLayout() {
        super.requestLayout();
        // We need to intercept this method because if we don't our children will never update
        // Check https://stackoverflow.com/questions/49371866/recyclerview-wont-update-child-until-i-scroll
        mRequestedLayout = true;
        this.post(new Runnable() {
            @SuppressLint("WrongCall")
            @Override
            public void run() {
                mRequestedLayout = false;
                layout(getLeft(), getTop(), getRight(), getBottom());
                onLayout(false, getLeft(), getTop(), getRight(), getBottom());
            }
        });
    }
}