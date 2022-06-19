package asia.daijizai.community.util;

import asia.daijizai.community.entity.ViewObject;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/6 19:17
 * @description
 */

public class CommunityUtil {

    //生成一个随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replace("-","");
    }

    //MD5加密
    public static String md5(String key){
        if (StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJSONString(int code, String msg, ViewObject vo) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (vo != null) {
            for (String key : vo.getKeySet()) {
                json.put(key, vo.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }


}
