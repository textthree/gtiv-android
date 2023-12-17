package com.dqd2022;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.dqd2022.constant.CommonConstants;
import com.dqd2022.services.DownloadApkService;

import java.io.File;

import kit.LogKit;

/**
 * 下载安装 apk
 * 使用：
 * Intent intent = new Intent(reactContext, DownloadApkActivity.class);
 * intent.putExtra("url", url);
 * intent.putExtra("saveFileName", saveFileName); // 注意：saveFileName 需要每次不一样，如果本地已存在同名文件则无法进行下载
 * // 由于没有在 Activity 环境下启动 Activity,设置下面的标签
 * intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 * reactContext.startActivity(intent);
 */
public class DownloadApkActivity extends AppCompatActivity {
    private Context context;
    private AlertDialog mDialog;
    // 是否下载完成后直接弹出安装界面，需要权限 REQUEST_INSTALL_PACKAGES，如果要上架应用市场这个权限一般会导致上架被拒绝
    private Boolean install = false;


    public DownloadApkActivity() {

    }

    public DownloadApkActivity(Context context) {
        this.context = context;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        String saveFileName = intent.getStringExtra("saveFileName");
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.setTitle("downloading");
        dialog.setMessage(saveFileName + "");
        dialog.setProgress(0);
        dialog.setMax(100);
        dialog.show();
        DownloadApkService.download(url, saveFileName, new DownloadApkService.UpdateCallback() {
            @Override
            public void onSuccess() {
                LogKit.p("下载成功");
                if (!install) {
                    return;
                }
                dialog.dismiss();
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    dialog.dismiss();
                    return;
                }
                File file = new File(CommonConstants.DOWNLOAD_PATH + saveFileName);
                try {
                    LogKit.p("安装文件目录：", file + "");
                    installApk(file);
                } catch (Exception e) {
                    LogKit.p("获取打开方式错误", e + "");
                    finish();
                }
            }

            @Override
            public void onProgress(int progress) {
                dialog.setProgress(progress);
            }

            @Override
            public void onFailure() {
                LogKit.p("onFailure:", "true");
                dialog.dismiss();
                finish();
            }
        });
    }

    private void installApk(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri fileUri;
        LogKit.p("安装文件:", file);
        // Android 7.0 以上
        if (Build.VERSION.SDK_INT >= 24) {
            fileUri = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + "" + ".fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            fileUri = Uri.fromFile(file);
        }
        LogKit.p("安装文件2:", fileUri);
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        startActivity(intent);
        finish();
    }
}