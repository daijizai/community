package asia.daijizai.community.service.impl;

import asia.daijizai.community.dao.MessageDao;
import asia.daijizai.community.entity.Message;
import asia.daijizai.community.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/11 22:02
 * @description
 */

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageDao messageDao;

    //查询当前用户的会话列表，针对每个会话只返回一条最新的私信
    public List<Message> listConversation(int userId, int offset, int limit) {
        return messageDao.listConversation(userId, offset, limit);
    }

    //查询当前用户的会话数量
    public int countConversation(int userId) {
        return messageDao.countConversation(userId);
    }

    //查询某个会话的私信列表
    public List<Message> listLetter(String conversationId, int offset, int limit) {
        return messageDao.listLetter(conversationId, offset, limit);
    }

    //查询某个会话所包含的私信数量
    public int countLetter(String conversationId) {
        return messageDao.countLetter(conversationId);
    }

    //查询未读私信的数量
    public int countUnreadLetter(int userId, String conversationId) {
        return messageDao.countLetterUnread(userId, conversationId);
    }

    public int insertMessage(Message message) {
        return messageDao.insertMessage(message);
    }

    public int readMessage(List<Integer> ids) {
        return messageDao.updateStatus(ids, 1);
    }

    public Message getLastNotice(int userId, String topic) {
        return messageDao.getLastNotice(userId, topic);
    }

    public int countNotice(int userId, String topic) {
        return messageDao.countNotice(userId, topic);
    }

    public int countUnreadNotice(int userId, String topic) {
        return messageDao.countUnreadNotice(userId, topic);
    }

    public List<Message> listNotice(int userId, String topic, int offset, int limit) {
        return messageDao.listNotice(userId, topic, offset, limit);
    }


}
