package asia.daijizai.community.service.impl;

import asia.daijizai.community.dao.CommentDao;
import asia.daijizai.community.entity.Comment;
import asia.daijizai.community.service.CommentService;
import asia.daijizai.community.service.DiscussPostService;
import asia.daijizai.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/10 21:43
 * @description
 */

@Service
public class CommentServiceImpl implements CommentService, CommunityConstant {

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> ListCommentByEntity(int entityType, int entityId, int offset, int limit) {
        return commentDao.ListCommentByEntity(entityType, entityId, offset, limit);
    }

    public int CountCommentByEntity(int entityType, int entityId) {
        return commentDao.CountCommentByEntity(entityType, entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int insertComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        int rows = commentDao.insertComment(comment);

        //更新帖子评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentDao.CountCommentByEntity(ENTITY_TYPE_POST, comment.getEntityId());
//            int commentCount = discussPostService.getDiscussPost(comment.getEntityId()).getCommentCount();
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }


    public Comment getComment(int id){
        return commentDao.getComment(id);
    }

}
