package asia.daijizai.community.util;

import asia.daijizai.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/7 18:10
 * @description 起到一个容器的作用，持有用户的信息，用于替代session对象
 */

@Component
public class HostHolder {
    private ThreadLocal<User> users=new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}
