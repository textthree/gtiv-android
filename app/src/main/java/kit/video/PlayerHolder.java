package kit.video;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.io.IOException;

import kit.LogKit;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayerHolder implements SurfaceHolder.Callback {
    Activity activity;
    private IjkMediaPlayer ijkMediaPlayer;
    private SurfaceHolder holder;
    private SurfaceView surfaceView;
    OnPlayerListener playerCallback;
    String playingUri; // 当前正在播放的资源
    Scale scaleType;
    int initProgress = 0;

    // 内容填充模式
    public enum Scale {
        scaleX, // 宽度百分百，高度等比缩放
        scaleY  // 高度百分百，宽度等比缩放
    }

    public interface OnPlayerListener {
        // 开始播放后的回调
        void onStart(IjkMediaPlayer ijkMediaPlayer); // duration 视频长度，毫秒

        // 播放结束
        void onPlayComplete(IjkMediaPlayer ijkMediaPlayer);
    }


    public void setOnPlayerListener(OnPlayerListener playerListener) {
        this.playerCallback = playerListener;
    }

    public PlayerHolder(Activity activity, SurfaceView surfaceView, Scale scale) {
        this.activity = activity;
        this.surfaceView = surfaceView;
        scaleType = scale;
        initPlayer();
    }

    // 恢复播放
    public void start() {
        ijkMediaPlayer.start();
        ijkMediaPlayer.setDisplay(holder);
        surfaceView.requestFocus();
    }

    public void pause() {
        ijkMediaPlayer.pause();
    }


    public IjkMediaPlayer getPlayer() {
        return ijkMediaPlayer;
    }

    public String getPlayingUri() {
        return playingUri;
    }


    /**
     * 开始播放
     *
     * @param uri
     * @param seekTo 从指定进度开始播放
     */
    public void playAsync(String uri, int... seekTo) {
        ijkMediaPlayer.setScreenOnWhilePlaying(true); // 播放时保持屏幕常亮
        if (seekTo.length > 0) {
            initProgress = seekTo[0];
        }
        try {
            ijkMediaPlayer.setDataSource(uri);
        } catch (IOException e) {
            e.printStackTrace();
            LogKit.p("初始化失败");
        }
        ijkMediaPlayer.prepareAsync();
        playingUri = uri;
    }

    // 切换播放 uri
    private void switchPlay(String uri) {
        ijkMediaPlayer.reset();
        ijkMediaPlayer.setDisplay(holder);
        try {
            ijkMediaPlayer.setDataSource(uri);
            ijkMediaPlayer.prepareAsync();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        playingUri = uri;
    }

    private void initPlayer() {
        ijkMediaPlayer = new IjkMediaPlayer();
        setOption();
        holder = surfaceView.getHolder();
        holder.addCallback(this);

        ijkMediaPlayer.setOnPreparedListener(iMediaPlayer -> {
            switch (scaleType) {
                case scaleX:
                    fillX();
                    break;
                case scaleY:
                    fillY();
                    break;
            }
            if (initProgress > 0) {
                ijkMediaPlayer.seekTo(initProgress);
                initProgress = 0;
            }
            ijkMediaPlayer.setDisplay(holder); // 显示视频
            if (playerCallback != null) {
                playerCallback.onStart(ijkMediaPlayer);
            }
        });
        ijkMediaPlayer.setOnErrorListener((iMediaPlayer, i, i1) -> {
            LogKit.p("播放器出错", iMediaPlayer, i, i1);
            return false;
        });

        // 播放完成
        ijkMediaPlayer.setOnCompletionListener(mp -> {
            LogKit.p("PlayerHolder onPlayComplete");
            if (playerCallback != null) playerCallback.onPlayComplete(ijkMediaPlayer);
        });

    }

    private void setOption() {
        ijkMediaPlayer.setSpeed(1); // 变速播放，取值范围：0.5 到 2
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0); // 0 为 ffmpeg 软解码
        // ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "safe", 0);
        // ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        // ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "fastseek");

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        LogKit.p("[surfaceCreated]");
        this.holder = holder;
        //start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        //LogKit.p("[surfaceChanged]", format, width, height);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        LogKit.p("[surfaceDestroyed]");
    }


    // 宽度铺满，等比计算高度
    private void fillX() {
        DisplayMetrics dp = Resources.getSystem().getDisplayMetrics();
        int videoW = ijkMediaPlayer.getVideoWidth();
        int videoH = ijkMediaPlayer.getVideoHeight();
        // 宽度始终百分百，用宽度计算缩放比
        float scaling = dp.widthPixels / (float) videoW;
        float targetH = videoH * scaling;
        ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
        layoutParams.height = (int) targetH;
        surfaceView.setLayoutParams(layoutParams);
    }

    // 高度铺满，等比计算宽度
    private void fillY() {
        DisplayMetrics dp = Resources.getSystem().getDisplayMetrics();
        int videoW = ijkMediaPlayer.getVideoWidth();
        int videoH = ijkMediaPlayer.getVideoHeight();
        float scaling = dp.heightPixels / (float) videoH;
        float targetW = videoW * scaling;
        ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
        layoutParams.width = (int) targetW;
        surfaceView.setLayoutParams(layoutParams);
    }

    // 不销毁播放，则 new 多个对象会同时播放
    public void destroy() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.reset();
            ijkMediaPlayer.release();
            ijkMediaPlayer = null;
            AudioManager am = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }
}
