package kit;

import com.dqd2022.helpers.App;
import com.tencent.mmkv.MMKV;

import java.util.HashSet;

// https://github.com/Tencent/MMKV
public class MmkvKit {
    static MMKV mmkv;

    public static void init(MMKV mk) {
        mmkv = mk;
    }

    // 往集合中添加一个元素
    public static void HashSetAdd(String mmkvKey, String value) {
        HashSet set = (HashSet) mmkv.getStringSet(mmkvKey, null);
        if (set == null) {
            set = new HashSet();
        }
        set.add(value);
        App.mmkv.putStringSet(mmkvKey, set);
    }

    public static void HashSetAdd(String mmkvKey, Integer value) {
        HashSetAdd(mmkvKey, String.valueOf(value));
    }

    // 获取整个集合
    public static HashSet<String> HashSetGet(String mmkvKey) {
        HashSet set = (HashSet) mmkv.getStringSet(mmkvKey, null);
        if (set != null) {
            return set;
        }
        return null;
    }

    public static HashSet<String> HashSetGet(Integer mmkvKey) {
        return HashSetGet(String.valueOf(mmkvKey));
    }

    // 判断集合中是否有某个值
    public static boolean HashSetContains(String mmkvKey, String value) {
        HashSet set = (HashSet) mmkv.getStringSet(mmkvKey, null);
        if (set != null) {
            return set.contains(value);
        }
        return false;
    }

    public static boolean HashSetContains(String mmkvKey, Integer value) {
        return HashSetContains(mmkvKey, String.valueOf(value));
    }

    // 从集合中移除指定值
    public static void HashSetRemoveItem(String mmkvKey, String itemValue) {
        HashSet set = (HashSet) mmkv.getStringSet(mmkvKey, null);
        if (set != null) {
            set.remove(itemValue);
            App.mmkv.putStringSet(mmkvKey, set);
        }
    }

    public static void HashSetRemoveItem(String mmkvKey, Integer itemValue) {
        HashSetRemoveItem(mmkvKey, String.valueOf(itemValue));
    }


}


