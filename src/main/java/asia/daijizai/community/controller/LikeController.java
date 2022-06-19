package asia.daijizai.community.controller;

import asia.daijizai.community.entity.Event;
import asia.daijizai.community.entity.User;
import asia.daijizai.community.entity.ViewObject;
import asia.daijizai.community.event.EventProducer;
import asia.daijizai.community.service.LikeService;
import asia.daijizai.community.util.CommunityConstant;
import asia.daijizai.community.util.CommunityUtil;
import asia.daijizai.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/12 20:51
 * @description
 */

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId,int postId) {
        User user = hostHolder.getUser();

        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);

        // 数量
        long likeCount = likeService.countLike(entityType, entityId);
        // 状态
        int likeStatus = likeService.getLikeStatus(user.getId(), entityType, entityId);
        // 返回的结果
        ViewObject likeVO = new ViewObject();
        likeVO.put("likeCount", likeCount);
        likeVO.put("likeStatus", likeStatus);

        //触发点赞事件
        if(likeStatus==1){
            Event event=new Event()
                    .setTopic(TOPIC_LIKE)//事件的主题
                    .setUserId(user.getId())//事件的触发者
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId",postId);
            eventProducer.fireEvent(event);
        }

        return CommunityUtil.getJSONString(0, null, likeVO);
    }
}
