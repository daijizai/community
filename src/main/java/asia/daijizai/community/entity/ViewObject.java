package asia.daijizai.community.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/6 7:19
 * @description
 */

public class ViewObject {
    private Map<String,Object> viewObject=new HashMap<>();

    public void put(String key, Object value){
        viewObject.put(key,value);
    }

    public Object get(String key){
        return viewObject.get(key);
    }

    public Set<String> getKeySet(){
        return viewObject.keySet();
    }
}
