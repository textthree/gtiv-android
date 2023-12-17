package kit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonKit {


    // 解析返回资源
    public static Map responseMap(String jsonStr) {
        JSONObject res = JSON.parseObject(jsonStr);
        Map ret = new HashMap();
        ret.put("errorCode", res.get("errorCode"));
        ret.put("message", res.get("message"));
        // 解析data数据
        Object dataObject = res.get("data");
        Map data = null;
        data = JSONObject.parseObject(JSONObject.toJSONString(dataObject), Map.class);
        ret.put("data", data);
        // 取data数据时需要类型强转：Map data = (Map)ret.get("data");
        return ret;
    }

    // JSON 数组字符串序列化为 map
    public static Map jsonArray2map(String jsonStr) {
        Map ret = new HashMap();
        if (jsonStr == "") {
            ret.put("errorCode", "500"); // 服务器无500
            ret.put("message", "服务器无响应");
            ret.put("data", new ArrayList<>());
            return ret;
        }
        JSONObject res = JSON.parseObject(jsonStr);
        ret.put("errorCode", res.get("errorCode"));
        ret.put("message", res.get("message"));
        // 解析data数组
        Object dataObject = res.get("data");
        List list = null;
        try {
            list = JSONObject.parseArray(JSONObject.toJSONString(dataObject), Map.class);
        } catch (Exception e) {
            list = new ArrayList();
        }
        ret.put("data", list);
        // 取data数据时需要类型强转：ArrayList data = (ArrayList)rs.get("data");
        return ret;
    }

    // 异常处理
    public static void catchError(Exception e) {
        e.printStackTrace();
    }


}

