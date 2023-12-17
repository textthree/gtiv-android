package kit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FileKit {

    public FileKit() {
    }

    // 判断文件是否存在
    static public boolean exists(String dir, String filename) {
        dir = dir.replace("file://", "");
        File file = new File(dir, filename);
        return file.exists();
    }

    static public boolean exists(String path) {
        path = path.replace("file://", "");
        File file = new File(path);
        return file.exists();
    }

    /**
     * 从 url 下载文件到本地（同步 io）
     *
     * @param cacheFilePath
     * @param fromUrl
     */
    static public void cacheFile(String fromUrl, String cacheFilePath) {
        File file = new File(cacheFilePath);
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            URL url = new URL(fromUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream is = connection.getInputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
            fos.close();
            is.close();
        } catch (IOException e) {
            LogKit.p("[cacheFile error] 缓存失败", cacheFilePath, e);
        }
    }

    // 写入
    static public File filePutContent(String filePath, String... str) {
        File file = new File(filePath);
        String content = "";
        if (str.length > 0) {
            content = str[0];
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(file); // 覆盖
            fw.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    // 读出
    static public String file_get_content(String filePath) {
        File file = new File(filePath);
        FileReader fr = null;
        String ret = new String();
        try {
            fr = new FileReader(file);
            int data = 0;
            while (data != -1) {  // read()返回读入的一个字符，如果达到文件末尾则返回-1
                //System.out.print((char)data); // 不换行输出
                char ch = (char) data;
                ret += String.valueOf(ch);
                try {
                    data = fr.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        }
        // 去除bom头
       /* if (funcs.isWindows()) {
            char[] bomChar = ret.toCharArray();//转为char数组
            char[] noneBomchar = new char[bomChar.length - 1];//数组第一个元素是bom头，去掉它
            for (int j = 0; j < noneBomchar.length; j++) {
                noneBomchar[j] = bomChar[j + 1];
            }
            ret = String.valueOf(noneBomchar); //重组转String
        }*/
        return ret;
    }

    // 传入路径获取后缀，如：jpg
    public static String getSuffix(String filePath) {
        if (filePath == null) {
            return "";
        }
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            return filePath.substring(lastDotIndex + 1);
        } else {
            // 如果没有找到点号（.），表示没有后缀
            return "";
        }
    }

    // 获取文件第几行的内容
    public static String readLine(String filePath, int num) throws IOException {
        File file = new File(filePath);

        if (!file.exists() || !file.isFile()) {
            throw new IOException("File not found or is not a regular file.");
        }

        if (num <= 0) {
            throw new IllegalArgumentException("Line number must be greater than zero.");
        }

        BufferedReader reader = null;
        int currentLineNumber = 0;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null) {
                currentLineNumber++;
                if (currentLineNumber == num) {
                    return line;
                }
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    // 获取文件倒数第几行
    public static String readLastLine(String filePath, int num) throws IOException {
        ArrayList<String> arr = new ArrayList<String>();
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return "";
        }
        if (num <= 0) {
            throw new IllegalArgumentException("Line number must be greater than zero.");
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                arr.add(line);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return arr.get(arr.size() - num);
    }

}
