package asia.daijizai.community.dao;

import asia.daijizai.community.entity.DiscussPost;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/5 19:34
 * @description
 */

@Mapper
public interface DiscussPostDao {

    String selectFields="id, user_id, title, content, type, status, create_time, comment_count, score";

    String insertFields="user_id, title, content, type, status, create_time, comment_count, score";

    @Select("<script>" +
            "select "+selectFields+ " from discuss_post" +
            "<where>" +
            "status != 2 " +
            "<if test='userId != 0'>" +
            "and user_id = #{userId}" +
            "</if>" +
            "</where>" +
            "order by type desc, create_time desc limit #{offset}, #{limit}" +
            "</script>")
    List<DiscussPost> list(@Param("userId") int userId, @Param("offset")int offset, @Param("limit")int limit);


    @Select("<script>" +
            "select count(id) from discuss_post " +
            "<where>" +
            "status != 2 " +
            "<if test='userId != 0'>" +
            "and user_id = #{userId}" +
            "</if>" +
            "</where>" +
            "</script>")
    int count(@Param("userId") int userId);

    @Insert("insert into discuss_post (" + insertFields + ") " +
            "values (#{discussPost.userId}, #{discussPost.title}, #{discussPost.content}, #{discussPost.type}, " +
            "#{discussPost.status}, #{discussPost.createTime}, #{discussPost.commentCount}, #{discussPost.score})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(@Param("discussPost") DiscussPost discussPost);

    @Select("select "+selectFields+" from discuss_post where id = #{id}")
    DiscussPost getDiscussPost(@Param("id")int id);

    @Update("update discuss_post set comment_count = #{commentCount} where id = #{id}")
    int updateCommentCount(@Param("id") int id,@Param("commentCount") int commentCount);
}
