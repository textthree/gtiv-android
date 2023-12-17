package com.dqd2022.helpers;

import android.content.Context;
import android.widget.Toast;

public class AlertUtils {


    public static void toast(String str) {
        Toast.makeText(App.context, str, Toast.LENGTH_SHORT).show();
    }
}
