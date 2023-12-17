package kit;

public class StringKit {

    /**
     * 字符串截取前几位或后几位
     *
     * @param str   要截取的字符串
     * @param start 传整数时从第 start 位开始截取到末尾，如: substr("abcd", 3) 得到 cd
     *              传负数时截取后 start 位，如：substr("abcd", -2) 得到 cd
     * @return
     */
    public static String substr(String str, int start) {
        String ret;
        int strLen = str.length();
        if (Math.abs(start) > strLen) {
            start = strLen;
        }
        if (start > 0) {
            ret = str.substring(start - 1, strLen);
        } else {
            ret = str.substring(strLen - Math.abs(start));
        }
        return ret;
    }

    /**
     * 按索引截取字符串
     *
     * @param str
     * @param start 下标，从 0 开始算
     * @param end   截取到结束未知的索引
     * @return
     */
    public static String substr(String str, int start, int end) {
        return str.substring(start, end);
    }

    // 字符串转int
    public static int parseInt(String str) {
        int _int = 0;
        try {
            _int = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return _int;
    }

    // 字符串转 long
    public static long parseLong(String str) {
        return Long.valueOf(str);
    }

    /**
     * 将字符串分割为数组
     *
     * @param delimiter 分隔符
     * @param str       字符串
     * @return
     */
    public static String[] expload(String delimiter, String str) {
        String[] s = str.split(delimiter);
        return s;
    }

    public static Integer[] expload(String delimiter, String str, Boolean toInt) {
        String[] arr = str.split(delimiter);
        Integer[] intArr = new Integer[arr.length];
        int i = 0;
        for (String v : arr) {
            intArr[i] = Integer.parseInt(v);
            i++;
        }
        return intArr;
    }

    public static String uuid() {
        return java.util.UUID.randomUUID().toString();
    }

    // 将纯文本格式化为 html
    public static String fromHtml() {
//        TextView textView = findViewById(R.id.textView);
//        String textFromDatabase = "第一行\n第二行";
//        String formattedText = textFromDatabase.replaceAll("\n", "<br>");
//        textView.setText(Html.fromHtml(formattedText, Html.FROM_HTML_MODE_COMPACT));

        return "";
    }

    // 去除右边带好
    public static String removeRightComma(String input) {
        if (input == null || input.isEmpty()) {
            return input; // 输入为空或者已经是空字符串，不需要处理
        }
        // 查找最右边的逗号的索引
        int lastIndex = input.lastIndexOf(",");
        // 如果找到了逗号，就去掉它
        if (lastIndex != -1 && lastIndex == input.length() - 1) {
            return input.substring(0, lastIndex); // 返回去掉逗号的子串
        }
        // 如果没有找到逗号或者逗号在字符串的末尾之外，返回原始字符串
        return input;
    }

}