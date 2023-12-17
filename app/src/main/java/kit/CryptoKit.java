package kit;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoKit {

    public static String md5Encrypt(String input) {
        try {
            // 创建 MessageDigest 对象，指定使用 MD5 算法
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 将输入字符串转换为字节数组，并进行哈希计算
            byte[] bytes = md.digest(input.getBytes());

            // 将字节数组转换为十六进制字符串表示
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
