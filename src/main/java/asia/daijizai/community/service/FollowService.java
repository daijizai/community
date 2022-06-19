package asia.daijizai.community.service;

import asia.daijizai.community.util.RedisKeyUtil;

import java.util.List;
import java.util.Map;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/16 7:57
 * @description
 */
public interface FollowService {

    void follow(int userId, int entityType, int entityId);

    void unfollow(int userId, int entityType, int entityId);

    //查询用户关注某种实体的数量
    long countFollowee(int userId, int entityType);

    //查询某个实体粉丝的数量
    long countFollower(int entityType, int entityId);

    //查询当前用户对某个实体的关注情况
    boolean hasFollowed(int userId, int entityType, int entityId);

    List<Map<String,Object>> listFollowee(int userId, int offset, int limit);

    List<Map<String,Object>> listFollower(int userId, int offset, int limit);
}
