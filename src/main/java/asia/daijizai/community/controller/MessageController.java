package asia.daijizai.community.controller;

import asia.daijizai.community.entity.Message;
import asia.daijizai.community.entity.Page;
import asia.daijizai.community.entity.User;
import asia.daijizai.community.entity.ViewObject;
import asia.daijizai.community.service.MessageService;
import asia.daijizai.community.service.UserService;
import asia.daijizai.community.util.CommunityConstant;
import asia.daijizai.community.util.CommunityUtil;
import asia.daijizai.community.util.HostHolder;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/11 22:14
 * @description
 */

@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    //会话列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();

        //分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.countConversation(user.getId()));
        //会话列表查询
        List<Message> conversationList = messageService.listConversation(user.getId(), page.getOffset(), page.getLimit());

        List<ViewObject> conversationVOs = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                ViewObject conversationVO = new ViewObject();
                conversationVO.put("conversation", message);
                conversationVO.put("letterCount", messageService.countLetter(message.getConversationId()));
                conversationVO.put("unreadCount", messageService.countUnreadLetter(user.getId(), message.getConversationId()));

                //如果当前用户是to，目标用户就是from；如果当前用户是from，目标用户就是to
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                conversationVO.put("target", userService.getUser(targetId));

                conversationVOs.add(conversationVO);
            }
        }

        model.addAttribute("conversationVOs", conversationVOs);

        //未读私信数量
        int letterUnreadCount = messageService.countUnreadLetter(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        //未读通知数量
        int noticeUnreadCount = messageService.countUnreadNotice(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/letter";
    }

    //会话详情
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.countLetter(conversationId));

        //一个会话中的信息列表
        List<Message> letterList = messageService.listLetter(conversationId, page.getOffset(), page.getLimit());

        List<ViewObject> letterVOs = new ArrayList<>();
        if (letterList != null) {
            for (Message letter : letterList) {
                ViewObject letterVO = new ViewObject();

                letterVO.put("letter", letter);
                letterVO.put("fromUser", userService.getUser(letter.getFromId()));
                letterVOs.add(letterVO);
            }
        }

        model.addAttribute("letterVOs", letterVOs);

        //私信的目标
        model.addAttribute("target", getLetterTarget(conversationId));

        //设置已读
        List<Integer> unreadIds = getUnreadIds(letterList);
        if (!unreadIds.isEmpty()) {
            messageService.readMessage(unreadIds);
        }

        return "/site/letter-detail";
    }

    private List<Integer> getUnreadIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        return hostHolder.getUser().getId() == id0 ? userService.getUser(id1) : userService.getUser(id0);
    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        User target = userService.getUserByUsername(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在！");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        message.setConversationId(Math.min(message.getFromId(), message.getToId()) + "_" + Math.max(message.getFromId(), message.getToId()));
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());

        messageService.insertMessage(message);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        //查询评论通知
        Message message = messageService.getLastNotice(user.getId(), TOPIC_COMMENT);
        ViewObject messageVO = new ViewObject();
        if (message != null) {
            messageVO.put("message", message);
            Map<String, Object> data = JSONObject.parseObject(message.getContent(), HashMap.class);

            messageVO.put("user", userService.getUser((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.countNotice(user.getId(), TOPIC_COMMENT);
            messageVO.put("count", count);

            int unread = messageService.countUnreadNotice(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread", unread);
        }
        model.addAttribute("commentNotice", messageVO);

        //查询点赞通知
        message = messageService.getLastNotice(user.getId(), TOPIC_LIKE);
        messageVO = new ViewObject();
        if (message != null) {
            messageVO.put("message", message);
            Map<String, Object> data = JSONObject.parseObject(message.getContent(), HashMap.class);

            messageVO.put("user", userService.getUser((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.countNotice(user.getId(), TOPIC_LIKE);
            messageVO.put("count", count);

            int unread = messageService.countUnreadNotice(user.getId(), TOPIC_LIKE);
            messageVO.put("unread", unread);
        }
        model.addAttribute("likeNotice", messageVO);

        //查询关注通知
        message = messageService.getLastNotice(user.getId(), TOPIC_FOLLOW);
        messageVO = new ViewObject();
        if (message != null) {
            messageVO.put("message", message);
            Map<String, Object> data = JSONObject.parseObject(message.getContent(), HashMap.class);

            messageVO.put("user", userService.getUser((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));

            int count = messageService.countNotice(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count", count);

            int unread = messageService.countUnreadNotice(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread", unread);
        }
        model.addAttribute("followNotice", messageVO);

        //未读私信数量
        int letterUnreadCount = messageService.countUnreadLetter(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        //未读通知数量
        int noticeUnreadCount = messageService.countUnreadNotice(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();

        page.setLimit(5);//一页显示5条数据
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.countNotice(user.getId(), topic));//一共多少条数据

        List<Message> noticeList = messageService.listNotice(user.getId(), topic, page.getOffset(), page.getLimit());

        List<ViewObject> noticeVOs = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                ViewObject noticeVO = new ViewObject();
                // 通知
                noticeVO.put("notice", notice);
                // 内容

                Map<String, Object> data = JSONObject.parseObject(notice.getContent(), HashMap.class);
                noticeVO.put("user", userService.getUser((Integer) data.get("userId")));
                noticeVO.put("entityType", data.get("entityType"));
                noticeVO.put("entityId", data.get("entityId"));
                noticeVO.put("postId", data.get("postId"));
                // 通知作者
                noticeVO.put("fromUser", userService.getUser(notice.getFromId()));

                noticeVOs.add(noticeVO);
            }
        }
        model.addAttribute("notices", noticeVOs);

        // 设置已读
        List<Integer> ids = getUnreadIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }
}
