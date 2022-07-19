package asia.daijizai.community;

import asia.daijizai.community.dao.DiscussPostDao;
import asia.daijizai.community.dao.LoginTicketDao;
import asia.daijizai.community.dao.MessageDao;
import asia.daijizai.community.dao.UserDao;
import asia.daijizai.community.entity.DiscussPost;
import asia.daijizai.community.entity.LoginTicket;
import asia.daijizai.community.entity.Message;
import asia.daijizai.community.entity.User;
import asia.daijizai.community.util.CommunityUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/5 20:58
 * @description
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class MapperTests {

    @Autowired
    DiscussPostDao discussPostDao;

    @Autowired
    UserDao userDao;

    @Autowired
    LoginTicketDao loginTicketDao;

    @Autowired
    MessageDao messageDao;


    @Test
    public void testSelectPost(){
        List<DiscussPost> discussPosts = discussPostDao.listDiscussPost(149, 0, 10,0);
        for (DiscussPost post:discussPosts){
            System.out.println(post);
        }

        System.out.println(discussPostDao.countDiscussPost(149));
    }

    @Test
    public void testInsertUser(){
        User user=new User();
        user.setUsername("TEST");
        user.setPassword("TEST");
        user.setEmail("TEST@qq.com");

        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("https://api.multiavatar.com/%s.png",CommunityUtil.generateUUID().substring(0,5)));
        user.setCreateTime(new Date());

        int i = userDao.insert(user);
        System.out.println(i);
        System.out.println(user.getId());
    }

    @Test
    public void testLoginTicket(){
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("TEST");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));

        loginTicketDao.insert(loginTicket);
        LoginTicket ticket = loginTicketDao.getByTicket("TEST");
        System.out.println(ticket.getUserId());
        System.out.println(ticket.getId());
    }

    @Test
    public void UpdateLoginTicket(){
        loginTicketDao.updateStatus("TEST",1);
    }

    @Test
    public void testSelectMessage(){
        List<Message> messageList1 = messageDao.listConversation(111, 0, 20);
        for (Message message:messageList1){
            System.out.println(message);
        }

        System.out.println(messageDao.countConversation(111));

        List<Message> messageList2 = messageDao.listLetter("111_112", 0, 10);
        for (Message message:messageList2){
            System.out.println(message);
        }

        System.out.println(messageDao.countLetter("111_112"));

        System.out.println(messageDao.countLetterUnread(131, "111_131"));
    }

}
