package asia.daijizai.community.service;

import asia.daijizai.community.util.RedisKeyUtil;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/12 20:49
 * @description
 */
public interface LikeService {

    void like(int userId,int entityType,int entityId,int entityUserId);

    //查询实体被点赞的数量
    long countLike(int entityType, int entityId);

    //查询某人对某实体的点赞状态
    int getLikeStatus(int userId, int entityType, int entityId);

    // 查询某个用户获得的赞
    int countUserLike(int userId);
}
