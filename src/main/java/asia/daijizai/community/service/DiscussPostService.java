package asia.daijizai.community.service;

import asia.daijizai.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/5 23:09
 * @description
 */

public interface DiscussPostService {

    List<DiscussPost> listDiscussPost(int userId, int offset, int limit,int orderMode);

    int countDiscussPost(int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost getDiscussPost(int id);

    int updateCommentCount(int id, int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);
    int updateScore(int id, double score);
}
