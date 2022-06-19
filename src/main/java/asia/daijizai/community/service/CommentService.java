package asia.daijizai.community.service;

import asia.daijizai.community.entity.Comment;

import java.util.List;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/10 21:43
 * @description
 */

public interface CommentService {

    List<Comment> ListCommentByEntity(int entityType, int entityId, int offset, int limit);

    int CountCommentByEntity(int entityType, int entityId);

    int insertComment(Comment comment);

    Comment getComment(int id);
}
