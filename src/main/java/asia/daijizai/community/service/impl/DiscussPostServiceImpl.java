package asia.daijizai.community.service.impl;

import asia.daijizai.community.dao.DiscussPostDao;
import asia.daijizai.community.entity.DiscussPost;
import asia.daijizai.community.service.DiscussPostService;
import asia.daijizai.community.util.SensitiveFilter;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/5 23:09
 * @description
 */

@Service
public class DiscussPostServiceImpl implements DiscussPostService {

    @Autowired
    private DiscussPostDao discussPostDao;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<DiscussPost> listDiscussPost(int userId, int offset, int limit) {
        return discussPostDao.list(userId, offset, limit);
    }

    public int countDiscussPost(@Param("userId") int userId) {
        return discussPostDao.count(userId);
    }

    public int insertDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        return discussPostDao.insert(discussPost);

    }

    public DiscussPost getDiscussPost(int id) {
        return discussPostDao.getDiscussPost(id);
    }

    public int updateCommentCount(int id,int commentCount){
        return discussPostDao.updateCommentCount(id, commentCount);
    }

}
