package kit;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.File;
import java.io.IOException;

public class AudioMediaPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private Activity activity;
    MediaPlayer player;
    private boolean playing = false; // 播放状态
    private String playingFile;      // 当前播放中的的文件，为空代表播放器没有在工作

    public AudioMediaPlayer(Activity activity) {
        this.activity = activity;
    }

    // 不要直接 player.prepare() ，这是同步的，会导致 page 线程阻塞
    // 只要有 100 毫秒的延迟，人就能感觉到不跟手，而 prepare 从远程下载资源可能要 1 秒以上，
    // 如果点击播放，UI 卡顿 1 秒以上完全无法接受
    // 因此必须使用 prepareAsync()
    public void paly(String filepath) {
        if (playing) {
            stop();
        }
        playing = true;
        playingFile = filepath;
        init(filepath);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.prepareAsync();
    }

    public void stop() {
        if (playing) {
            player.stop();
            player.release();
        }
        playing = false;
        playingFile = "";
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playingFile = "";
        playing = false;
        mp.release();
    }

    @Override
    public synchronized void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    public boolean isPlaying() {
        return playing;
    }

    public String getPlayingFile() {
        return playingFile;
    }

    /**
     * 初始化 MediaPlayer 支持传入不同格式的地址
     *
     * @param file
     */
    private void init(final String file) {
        LogKit.p("音频文件地址:", file);
        player = new MediaPlayer();
//        int res = activity.getResources().getIdentifier(file, "raw", activity.getPackageName());
//        if (res != 0) {
//            LogKit.p("[MediaPlayer assets type]:", "raw");
//            try {
//                AssetFileDescriptor afd = activity.getResources().openRawResourceFd(res);
//                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
//                afd.close();
//            } catch (IOException e) {
//                LogKit.p("[MediaPlayer Exception]:", e);
//                player = null;
//                return;
//            }
//            return;
//        }

        if (file.startsWith("http://") || file.startsWith("https://")) {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            LogKit.p("[MediaPlayer assets type]:", "http");
            try {
                player.setDataSource(file);
            } catch (IOException e) {
                LogKit.p("[MediaPlayer Exception]:", e);
                player = null;
                return;
            }
            return;
        }

        if (file.startsWith("asset:/")) {
            LogKit.p("[MediaPlayer assets type]:", "asset:/");
            try {
                AssetFileDescriptor descriptor = activity.getAssets().openFd(file.replace("asset:/", ""));
                player.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();
                return;
            } catch (IOException e) {
                LogKit.p("[MediaPlayer Exception]:", e);
                player = null;
                return;
            }
        }

        if (file.startsWith("file:/")) {
            LogKit.p("[MediaPlayer assets type]:", "file:/");
            try {
                player.setDataSource(file);
            } catch (IOException e) {
                LogKit.p("[MediaPlayer Exception]:", e);
                player = null;
                return;
            }
            return;
        }

        File _file = new File(file);
        if (_file.exists()) {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            LogKit.p("[MediaPlayer assets type]:", "default");
            try {
                player.setDataSource(file);
            } catch (IOException e) {
                LogKit.p("[MediaPlayer Exception]:", e);
                player = null;
                return;
            }
            return;
        }
        return;
    }


}