package asia.daijizai.community.dao;

import asia.daijizai.community.entity.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/11 19:49
 * @description
 */

@Mapper
public interface MessageDao {

    String selectFields = "id, from_id, to_id, conversation_id, content, status, create_time";
    String insertFields = "from_id, to_id, conversation_id, content, status, create_time";

    //查询当前用户的会话列表，针对每个会话只返回一条最新的私信
    @Select("select " + selectFields + " from message " +
            "where id in " +
            "(" +
            "select max(id) from message " +
            "where status != 2 " +
            "and from_id != 1 " +
            "and (from_id = #{userId} or to_id = #{userId})" +
            "group by conversation_id" +
            ")" +
            "order by id desc " +
            "limit #{offset}, #{limit}")
    List<Message> listConversation(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    //查询当前用户的会话数量
    @Select("select count(m.maxid) from " +
            "(" +
            "select max(id) as maxid from message " +
            "where status != 2 " +
            "and from_id != 1 " +
            "and (from_id = #{userId} or to_id = #{userId})" +
            "group by conversation_id " +
            ")" +
            "as m")
    int countConversation(@Param("userId") int userId);

    //查询某个会话的私信列表
    @Select("select " + selectFields + " from message " +
            "where status != 2 and from_id != 1 " +
            "and conversation_id = #{conversationId} " +
            "order by id desc " +
            "limit #{offset}, #{limit}")
    List<Message> listLetter(@Param("conversationId") String conversationId, @Param("offset") int offset, @Param("limit") int limit);

    //查询某个会话所包含的私信数量
    @Select("select count(id) from message " +
            "where status != 2 and from_id != 1 " +
            "and conversation_id = #{conversationId}")
    int countLetter(@Param("conversationId") String conversationId);

    //查询未读私信的数量
    @Select("<script>" +
            "select count(id) from message" +
            "<where>" +
            "status = 0 and from_id != 1 and to_id = #{userId} " +
            "<if test='conversationId != null'>" +
            "and conversation_id = #{conversationId}" +
            "</if>" +
            "</where>" +
            "</script>")
    int countLetterUnread(@Param("userId") int userId, @Param("conversationId") String conversationId);

    //新增私信
    @Insert("insert into message (" + insertFields + ") " +
            "values (#{message.fromId}, #{message.toId}, #{message.conversationId}, #{message.content}, #{message.status}, #{message.createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertMessage(@Param("message") Message message);

    //修改消息的状态
    @Update("<script>" +
            "update message set status = #{status}" +
            "<where>" +
            "id in " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</where>" +
            "</script>")
    int updateStatus(@Param("ids") List<Integer> ids, @Param("status") int status);


    //查询某个主题下最新的通知
    @Select("select " + selectFields + " from message " +
            "where id in (" +
            "select max(id) from message " +
            "where status != 2 " +
            "and from_id = 1 " +
            "and to_id = #{userId} " +
            "and conversation_id = #{topic})")
    Message getLastNotice(@Param("userId") int userId, @Param("topic") String topic);

    //查询某个主题下通知的数量
    @Select("select count(id) from message " +
            "where status != 2 " +
            "and from_id = 1 " +
            "and to_id = #{userId} " +
            "and conversation_id = #{topic}")
    int countNotice(@Param("userId") int userId, @Param("topic") String topic);

    //查询某个主题下未读的数量
    @Select("<script>" +
            "select count(id) from message" +
            "<where>" +
            "status = 0 and from_id = 1 and to_id = #{userId} " +
            "<if test='topic != null'>" +
            "and conversation_id = #{topic}" +
            "</if>" +
            "</where>" +
            "</script>")
    int countUnreadNotice(@Param("userId") int userId, @Param("topic") String topic);


    //查询某个主题包含的通知列表
    @Select("select " + selectFields + " from message " +
            "where status != 2 and from_id = 1 and to_id = #{userId} " +
            "and conversation_id = #{topic} " +
            "order by id desc " +
            "limit #{offset}, #{limit}")
    List<Message> listNotice(@Param("userId") int userId, @Param("topic") String topic, @Param("offset") int offset, @Param("limit") int limit);
}
