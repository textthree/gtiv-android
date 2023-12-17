/*
package com.dqd2022.helpers.azure;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.InputStream;

import kit.LogKit;

public class EAzureBlobStorageFile {
    private static final String E_LAYOUT_ERROR = "E_LAYOUT_ERROR";
    private static final String MODULE_NAME = "EAzureBlobStorageFile";

    public static String ACCOUNT_NAME = "";
    public static String ACCOUNT_KEY = "";
    public static String CONTAINER_NAME = "";
    public static boolean SAS = false;

    private Context ctx;

    public EAzureBlobStorageFile(@NonNull Context context, String account_name, String account_key, String constainer_name, boolean sas_token) {
        ctx = context;
        ACCOUNT_NAME = account_name;
        ACCOUNT_KEY = account_key;
        CONTAINER_NAME = constainer_name;
        SAS = sas_token;
    }

    public String uploadFile(String filePath, String objectKey, String contentType) {
        try {
            String file = filePath.contains("file://") ? filePath : "file://".concat(filePath);
            final InputStream imageStream = ctx.getContentResolver().openInputStream(Uri.parse(file));
            final int imageLength = imageStream.available();
            try {
                final String imageName = EAzureBlobStorageFile.SAS ? FileManager.UploadFileSas(imageStream, imageLength, objectKey, contentType)
                        : FileManager.UploadFile(imageStream, imageLength, objectKey, contentType);
                //LogKit.p("Image Uploaded Successfully.", imageName);
                return imageName;
            } catch (final Exception ex) {
                final String exceptionMessage = ex.getMessage();
                // LogKit.p("Image Uploaded Fail.", exceptionMessage);
            }

        } catch (Exception ex) {
            LogKit.p(E_LAYOUT_ERROR, ex);
        }
        return "";
    }

}*/
