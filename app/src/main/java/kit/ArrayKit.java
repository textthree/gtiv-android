package kit;

import java.util.Arrays;

public class ArrayKit {

    /**
     * 判断元素是否在数组中
     *
     * @param array 数组
     * @param value 元素
     * @return
     */
    public static Boolean inArray(int value, int[] array) {
        Boolean ret = false;
        for(int item : array) {
            if(item == value) {
                ret = true;
                break;
            }
        }
        return ret;
    }



}