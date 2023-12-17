package kit;

import android.util.Log;

import com.dqd2022.Config;

import java.lang.reflect.Field;

public class LogKit {

    static public void p(String output) {
        if (!Config.Debug) return;
        Log.i("logkit -> ", output);
    }

    static public void p(int output) {
        if (!Config.Debug) return;
        Log.d("logkit", " -> " + output);
    }

    static public void p(Object... args) {
        if (!Config.Debug) return;
        String opt = "";
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null)
                opt += args[i].toString() + " ";
        }
        Log.d("logkit", " -> " + opt);
    }

    // 遍历对象
    static public void obj(Object object) {
        Class<?> clazz = object.getClass();
        // 使用反射遍历对象的所有属性和值
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true); // 允许访问私有字段
            try {
                String fieldName = field.getName();
                Object fieldValue = field.get(object);
                p(fieldName + ": " + fieldValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


}
