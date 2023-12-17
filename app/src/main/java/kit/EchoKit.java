package kit;

public class EchoKit {

    public static void p(Object value) {
        System.out.println("================================ EchoKit ================================");
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        System.out.print("\33[33;4m" + TimeKit.DateTime() + " ");
        System.out.format("\33[37;4mFrom " + ste + "\n");
        String type = "Type: " + value.getClass().getName().toString();
        System.out.format("\33[37;4m" + type + " "); // %n表示换行
        System.out.print("(" + TimeKit.DateTime() + ")\n");
        System.out.print("\033[30;4m\033[0m"); // 恢复默认样式
        System.out.println(value);
    }

    public static void trace(Object value) {
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        System.out.print("\33[33;4m" + TimeKit.DateTime() + " ");
        System.out.format("\33[37;4mFrom " + ste + "\n");
        System.out.print("\033[30;4m\033[0m"); // 恢复默认样式
        System.out.println(value + "\n");
    }
}
