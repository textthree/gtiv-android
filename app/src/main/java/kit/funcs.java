//package kit;
//
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.net.SocketException;
//import java.net.UnknownHostException;
//import java.util.HashMap;
//
//public class funcs {
//
//
//    // 判断当前是否windows系统
//    public static boolean isWindows() {
//        return System.getProperties().getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1;
//    }
//
//    private static int steNum = 1;
//
//
//    // 获取电脑信息
//    public static HashMap hostInfo() {
//        InetAddress addr = null;
//        try {
//            addr = InetAddress.getLocalHost();
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        String ip = addr.getHostAddress();
//        String hostName = addr.getCanonicalHostName();
//        byte[] mac = new byte[0];
//        try {
//            mac = NetworkInterface.getByInetAddress(addr).getHardwareAddress();
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
//        StringBuffer macAddr = new StringBuffer();
//        for (int i = 0; i < mac.length; i++) {
//            if (i != 0) {
//                macAddr.append("-");
//            }
//            // mac[i] & 0xFF 把byte转化为正整数
//            String s = Integer.toHexString(mac[i] & 0xFF);
//            macAddr.append(s.length() == 1 ? 0 + s : s);
//        }
//        //macAddr.toString().toUpperCase();
//
//        HashMap ret = new HashMap();
//        ret.put("ip", ip);
//        ret.put("hostName", hostName);
//        ret.put("mac", macAddr);
//        return ret;
//    }
//
//
//    /**
//     * 十六进制颜色转RGB色
//     */
//    public static String hex2rgb(String hex) {
//        StringBuffer sb = new StringBuffer();
//        // 用Integer转为十六进制的rgb值
//        hex = hex.replace("#", "0x");
//        int rgb = Integer.parseInt(hex.substring(2), 16);
//        // 实例化java.awt.Color,获取对应的r、g、b值
//        java.awt.Color color = new java.awt.Color(rgb);
//        int red = color.getRed();
//        int greed = color.getGreen();
//        int blue = color.getBlue();
//        sb.append(red).append(",").append(greed).append(",").append(blue);
//        return sb.toString();
//    }
//
//
//}
