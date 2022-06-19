package asia.daijizai.community.service.impl;

import asia.daijizai.community.dao.UserDao;
import asia.daijizai.community.entity.User;
import asia.daijizai.community.service.FollowService;
import asia.daijizai.community.service.UserService;
import asia.daijizai.community.util.CommunityConstant;
import asia.daijizai.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/15 20:24
 * @description
 */

@Service
public class FollowServiceImpl implements FollowService, CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private  UserService userService;

    @Autowired
    private UserDao userDao;

    //关注操作
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {

                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                redisOperations.multi();//开启事务

                redisOperations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());//用户关注实体的列表，只有用户才能关注
                redisOperations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());//实体的粉丝列表，实体的粉丝只能是用户

                return redisOperations.exec();//提交事务
            }
        });
    }

    //取消操作
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {

                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                redisOperations.multi();//开启事务

                redisOperations.opsForZSet().remove(followeeKey, entityId);//用户关注实体的列表，只有用户才能关注
                redisOperations.opsForZSet().remove(followerKey, userId);//实体的粉丝列表，实体的粉丝只能是用户

                return redisOperations.exec();//提交事务
            }
        });
    }

    //查询用户关注某种实体的数量
    public long countFollowee(int userId,int entityType){
        String followeeKey=RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //查询某个实体粉丝的数量
    public long countFollower(int entityType,int entityId){
        String followerKey=RedisKeyUtil.getFollowerKey(entityType,entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询当前用户对某个实体的关注情况
    public boolean hasFollowed(int userId, int entityType, int entityId){
        String followeeKey=RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().score(followeeKey,entityId)!=null;
    }

    //获取用户的关注列表
    public List<Map<String,Object>> listFollowee(int userId,int offset,int limit){
        String followeeKey=RedisKeyUtil.getFolloweeKey(userId,ENTITY_TYPE_USER);
        Set targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (targetIds==null){
            return null;
        }

        List<Map<String,Object>> list=new ArrayList<>();
        for(Object targetId:targetIds){
            Map<String,Object> map=new HashMap<>();
            User user = userDao.getUser((Integer) targetId);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }

    //获取用户的粉丝
    public List<Map<String,Object>> listFollower(int userId,int offset,int limit){
        String followerKey=RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER,userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (targetIds==null){
            return null;
        }

        List<Map<String,Object>> list=new ArrayList<>();
        for(Integer targetId:targetIds){
            Map<String,Object> map=new HashMap<>();
            User user = userDao.getUser(targetId);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }

}
