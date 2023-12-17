package kit;

import android.Manifest;
import android.app.Activity;
import android.media.MediaRecorder;

import com.dqd2022.R;

import java.io.File;

import pub.devrel.easypermissions.EasyPermissions;

// 录语音聊天用，采样率和编码率都设置得很低
public class VoiceMediaRecorder {
    private Activity activity;
    private MediaRecorder recorder;
    private boolean isRecording = false;
    String recordFilePath; // 录音文件保存地址

    public VoiceMediaRecorder(Activity activity) {
        this.activity = activity;
    }

    // 开始录音
    public boolean start() {
        checkPermission();
        if (!init()) {
            return false;
        }
        if (isRecording) {
            return false;
        }
        recorder.start();
        isRecording = true;
        return true;
    }

    /**
     * 检查权限
     */
    private void checkPermission() {
        String[] perms = {
                Manifest.permission.RECORD_AUDIO
        };
        if (!EasyPermissions.hasPermissions(activity, perms)) {
            EasyPermissions.requestPermissions(
                    activity,
                    activity.getString(R.string.GetCameraPermission),
                    0,
                    perms);
        }
    }


    // 初始化录音设置
    public boolean init() {
        if (isRecording) {
            // 请先停止，再录音
        }
        String filename = StringKit.uuid();
        // /data/user/0/com.dqd2022/files/t3im/voice
        recordFilePath = activity.getFilesDir().getAbsolutePath() + "/t3im/voice/" + filename + ".aac";
        File destFile = new File(recordFilePath);
        if (destFile.getParentFile() != null) {
            destFile.getParentFile().mkdirs();
        }
        recorder = new MediaRecorder();
        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);    // 设置麦克风
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS); // 这行必须放在 setAudioEncoder() 前面
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);  // 音频编码格式
            recorder.setAudioSamplingRate(16000);
            recorder.setAudioEncodingBitRate(128000);
            recorder.setAudioChannels(1); // 通道，应该是单声道双声道的意思
            recorder.setOutputFile(destFile.getPath());
            recorder.prepare();
        } catch (final Exception e) {
            LogKit.p("录音初始化失败", e);
            return false;
        }
        return true;
    }


    // 停止录音
    public String stop() {
        if (!isRecording) {
            return "";
        }
        isRecording = false;
        try {
            recorder.stop();
            recorder.release();
        } catch (final RuntimeException e) {
            // https://developer.android.com/reference/android/media/MediaRecorder.html#stop()
            // No valid audio data received. You may be using a device that can't record audio.
            LogKit.p(e.getMessage());
            return "";
        } finally {
            recorder = null;
        }
        LogKit.p("录音完成 recordFilePath: ", recordFilePath);
        return recordFilePath;
    }


}
