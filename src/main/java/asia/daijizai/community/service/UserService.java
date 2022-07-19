package asia.daijizai.community.service;

import asia.daijizai.community.entity.LoginTicket;
import asia.daijizai.community.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/5 23:17
 * @description
 */
public interface UserService {

    User getUser(int id);

    Map<String, Object> register(User user);

    int activation(int userId, String activationCode);

    Map<String,Object> login(String username, String password, int expiredSeconds);

    void logout(String ticket);

    LoginTicket getLoginTicket(String ticket);

    int updateHeader(int userId,String headerUrl);

    User getUserByUsername(String username);

    Collection<? extends GrantedAuthority> getAuthorities(int userId);
}
