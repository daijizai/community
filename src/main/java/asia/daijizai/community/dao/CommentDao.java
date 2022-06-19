package asia.daijizai.community.dao;

import asia.daijizai.community.entity.Comment;
import asia.daijizai.community.entity.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/10 21:22
 * @description
 */

@Mapper
public interface CommentDao {

    String selectFields = "id, user_id, entity_type, entity_id, target_id, content, status, create_time";
    String insertFields = "user_id, entity_type, entity_id, target_id, content, status, create_time";


    @Select("select " + selectFields + " from comment " +
            "where status = 0 and entity_type = #{entityType} and entity_id = #{entityId} " +
            "order by create_time limit #{offset}, #{limit}")
    List<Comment> ListCommentByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId,
                                      @Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(id) from comment where status = 0 and entity_type = #{entityType} and entity_id = #{entityId}")
    int CountCommentByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId);

    @Insert("insert into comment (" + insertFields + ") " +
            "values (#{comment.userId}, #{comment.entityType}, #{comment.entityId}, #{comment.targetId}, #{comment.content}, #{comment.status}, #{comment.createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertComment(@Param("comment") Comment comment);

    @Select("select " + selectFields + " from comment where id = #{id}")
    Comment getComment(@Param("id")int id);

}
