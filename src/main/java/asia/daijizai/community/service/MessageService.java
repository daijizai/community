package asia.daijizai.community.service;

import asia.daijizai.community.entity.Message;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/11 22:03
 * @description
 */

public interface MessageService {

    List<Message> listConversation(int userId, int offset, int limit);

    //查询当前用户的会话数量
    int countConversation(int userId);

    //查询某个会话的私信列表
    List<Message> listLetter(String conversationId, int offset, int limit);

    //查询某个会话所包含的私信数量
    int countLetter(String conversationId);

    //查询未读私信的数量
    int countUnreadLetter(int userId, String conversationId);


    int insertMessage(Message message);

    int readMessage(List<Integer> ids);

    Message getLastNotice(int userId, String topic);

    int countNotice(int userId, String topic);

    int countUnreadNotice(int userId, String topic);

    List<Message> listNotice(int userId, String topic,int offset,int limit);

}
