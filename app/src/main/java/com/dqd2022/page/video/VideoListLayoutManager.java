package com.dqd2022.page.video;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class VideoListLayoutManager extends LinearLayoutManager implements RecyclerView.OnChildAttachStateChangeListener {
    private PagerSnapHelper mPagerSnapHelper;
    private OnViewPagerListener mListenerCallback;

    // 为了布局与逻辑解耦，不直接在这里面实现播放相关操作，
    // 创建接口，把要暴露给外部代用的函数通过回调执行的方式传传参给接口实现者，在实现者类中编写播放控制逻辑
    public interface OnViewPagerListener {
        // 滑动切页完成，surplus：播放列表中剩余还有多少个视频没有播放
        void onShowPage(View view, int position, int surplus);

        // 被滑走的页
        void onRemovePage(int position);

        // position：移进来页的 position
        void onBeforShowPage(View view);

    }

    public VideoListLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        mPagerSnapHelper = new PagerSnapHelper();
    }

    public void setViewPagerListener(OnViewPagerListener mOnViewPagerListener) {
        this.mListenerCallback = mOnViewPagerListener;
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        view.addOnChildAttachStateChangeListener(this);
        mPagerSnapHelper.attachToRecyclerView(view);
        super.onAttachedToWindow(view);
    }


    @Override
    public void onChildViewAttachedToWindow(@NonNull View view) {
        //LogKit.p("第", getPosition(view), "页滑进来了");
        mListenerCallback.onBeforShowPage(view);
    }

    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {
        //LogKit.p("第", getPosition(view), "页被滑走了");
        mListenerCallback.onRemovePage(getPosition(view));
    }


    // state 取值：1.手指按下并开始滑 2.手指抬起 0.手指抬起后并且新页面对齐完成
    @Override
    public void onScrollStateChanged(int state) {
        switch (state) {
            case RecyclerView.SCROLL_STATE_IDLE:
                View view = mPagerSnapHelper.findSnapView(this);
                int position = getPosition(view);
                int surplus = getItemCount() - position; // 是否已经是最后一页了
                if (mListenerCallback != null) {
                    mListenerCallback.onShowPage(view, position, surplus);
                }
                break;
        }
        super.onScrollStateChanged(state);
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

}
