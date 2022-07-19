package asia.daijizai.community.service.impl;

import asia.daijizai.community.dao.LoginTicketDao;
import asia.daijizai.community.dao.UserDao;
import asia.daijizai.community.entity.LoginTicket;
import asia.daijizai.community.entity.User;
import asia.daijizai.community.service.UserService;
import asia.daijizai.community.util.CommunityConstant;
import asia.daijizai.community.util.CommunityUtil;
import asia.daijizai.community.util.MailClient;
import asia.daijizai.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/5 23:17
 * @description
 */

@Service
public class UserServiceImpl implements UserService, CommunityConstant {

    @Autowired
    private UserDao userDao;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

//    @Autowired
//    private LoginTicketDao loginTicketDao;

    @Autowired
    private RedisTemplate redisTemplate;

    public User getUser(int id) {
//        return userDao.getUser(id);
        User user = getCache(id);
        if(user==null){
            user = initCache(id);
        }
        return user;
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = null;

        //空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map = new HashMap<>();
            map.put("usernameMsg", "用户名不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map = new HashMap<>();
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map = new HashMap<>();
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        //验证账号
        if (null != userDao.getByUsername(user.getUsername())) {
            map = new HashMap<>();
            map.put("usernameMsg", "用户名已存在!");
            return map;
        }
        //验证邮箱
        if (null != userDao.getByEmail(user.getEmail())) {
            map = new HashMap<>();
            map.put("emailMsg", "邮箱已存在!");
            return map;
        }

        //将新用户插入数据库
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("https://api.multiavatar.com/%s.png", CommunityUtil.generateUUID().substring(0, 5)));
        user.setCreateTime(new Date());
        userDao.insert(user);

        //发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String activationUrl = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("activationUrl", activationUrl);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活帐号", content);

        return map;
    }

    public int activation(int userId, String activationCode) {
        User user = userDao.getUser(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(activationCode)) {
            userDao.updateStatus(userId, 1);//更新用户状态
            clearCache(userId);//更新用户数据时，清理用户缓存
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login(String username,String password,int expiredSeconds){
        Map<String,Object>map=new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userDao.getByUsername(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketDao.insert(loginTicket);

        String key = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(key,loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
//        loginTicketDao.updateStatus(ticket,1);

        String key = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket =(LoginTicket) redisTemplate.opsForValue().get(key);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(key,loginTicket);
    }

    public LoginTicket getLoginTicket(String ticket){
//        return loginTicketDao.getByTicket(ticket);

        String key = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(key);
    }

    public int updateHeader(int userId,String headerUrl){
//        return userDao.updateHeaderUrl(userId,headerUrl);
        int rows = userDao.updateHeaderUrl(userId, headerUrl);//更新用户头像
        clearCache(userId);//更新用户数据时，清理用户缓存
        return rows;
    }

    public User getUserByUsername(String username){
        return userDao.getByUsername(username);
    }

    //优先从缓存中取数据
    private User getCache(int userId){
        String key = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(key);
    }

    //缓存取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userDao.getUser(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);//设置过期时间为1小时
        return user;
    }

    //数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.getUser(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }

}
