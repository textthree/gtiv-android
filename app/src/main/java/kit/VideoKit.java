package kit;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import java.util.HashMap;

public class VideoKit {
    /**
     * 视频生成缩图
     *
     * @return
     */
    public HashMap genVideoThumb(Activity activity, String videoPath, String targetDir) {
        // filePath = filePath.replace("file://", "");
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        // 设置截取的关键帧，获取 bitmap
        Bitmap bitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        String saveJpgFile = ImageKit.saveBitmap(activity, bitmap, targetDir, "jpg");
        HashMap map = new HashMap<>();
        map.put("uri", saveJpgFile);
        map.put("w", bitmap.getWidth());
        map.put("h", bitmap.getHeight());
        return map;
    }
}
