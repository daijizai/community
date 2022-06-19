package asia.daijizai.community.controller;

import asia.daijizai.community.entity.Comment;
import asia.daijizai.community.entity.DiscussPost;
import asia.daijizai.community.entity.Event;
import asia.daijizai.community.event.EventProducer;
import asia.daijizai.community.service.CommentService;
import asia.daijizai.community.service.DiscussPostService;
import asia.daijizai.community.util.CommunityConstant;
import asia.daijizai.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/11 9:32
 * @description
 */

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @RequestMapping(path = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId")int discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.insertComment(comment);

        // 触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)//事件的主题
                .setUserId(hostHolder.getUser().getId())//事件的触发者
                .setEntityType(comment.getEntityType())//实体的类型
                .setEntityId(comment.getEntityId())//实体的id
                .setData("postId",discussPostId);//所属的帖子id
        if(comment.getEntityType()==ENTITY_TYPE_POST){//如果是评论
            DiscussPost target = discussPostService.getDiscussPost(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }else if (comment.getEntityType()==ENTITY_TYPE_COMMENT){//如果是回复
            Comment target = commentService.getComment(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        if(comment.getEntityType()==ENTITY_TYPE_POST){
            //触发发帖事件
            event=new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);
        }


        return "redirect:/discuss/detail/"+discussPostId;
    }
}
