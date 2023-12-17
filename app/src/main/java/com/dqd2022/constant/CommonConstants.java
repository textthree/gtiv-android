package com.dqd2022.constant;

import android.os.Environment;

import java.io.File;


public class CommonConstants {
    private static final String DEMO_PATH = Environment.getExternalStorageDirectory() + "/download" + File.separator;
    public static final String DOWNLOAD_PATH = DEMO_PATH + File.separator + "download" + File.separator;
}

