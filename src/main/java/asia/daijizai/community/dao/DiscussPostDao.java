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

    String selectFields = "id, user_id, title, content, type, status, create_time, comment_count, score";

    String insertFields = "user_id, title, content, type, status, create_time, comment_count, score";


    @Select("<script>" +
            "select count(id) from discuss_post " +
            "<where>" +
            "status != 2 " +
            "<if test='userId != 0'>" +
            "and user_id = #{userId}" +
            "</if>" +
            "</where>" +
            "</script>")
    int countDiscussPost(@Param("userId") int userId);

    @Insert("insert into discuss_post (" + insertFields + ") " +
            "values (#{discussPost.userId}, #{discussPost.title}, #{discussPost.content}, #{discussPost.type}, " +
            "#{discussPost.status}, #{discussPost.createTime}, #{discussPost.commentCount}, #{discussPost.score})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(@Param("discussPost") DiscussPost discussPost);

    @Select("select " + selectFields + " from discuss_post where id = #{id}")
    DiscussPost getDiscussPost(@Param("id") int id);

    @Update("update discuss_post set comment_count = #{commentCount} where id = #{id}")
    int updateCommentCount(@Param("id") int id, @Param("commentCount") int commentCount);

    @Update("update discuss_post set type = #{type} where id = #{id}")
    int updateType(@Param("id") int id, @Param("type") int type);

    @Update("update discuss_post set status = #{status} where id = #{id}")
    int updateStatus(@Param("id") int id, @Param("status") int status);

    @Update("update discuss_post set score = #{score} where id = #{id}")
    int updateScore(@Param("id") int id, @Param("score") double score);

    @Select("<script>" +
            "select " + selectFields + " from discuss_post" +
            "<where>" +
            "status != 2 " +
            "<if test='userId != 0'>" +
            "and user_id = #{userId}" +
            "</if>" +
            "</where>" +
            "<if test='orderMode == 0'>" +
            "order by type desc, create_time desc limit #{offset}, #{limit}" +
            "</if>" +
            "<if test='orderMode == 1'>" +
            "order by type desc, score desc, create_time desc limit #{offset}, #{limit}" +
            "</if>" +
            "</script>")
    List<DiscussPost> listDiscussPost(@Param("userId") int userId,
                                      @Param("offset") int offset, @Param("limit") int limit,
                                      @Param("orderMode") int orderMode);
}
