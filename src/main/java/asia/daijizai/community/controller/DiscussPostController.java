package asia.daijizai.community.controller;

import asia.daijizai.community.entity.*;
import asia.daijizai.community.event.EventProducer;
import asia.daijizai.community.service.CommentService;
import asia.daijizai.community.service.DiscussPostService;
import asia.daijizai.community.service.LikeService;
import asia.daijizai.community.service.UserService;
import asia.daijizai.community.util.CommunityConstant;
import asia.daijizai.community.util.CommunityUtil;
import asia.daijizai.community.util.HostHolder;
import asia.daijizai.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/8 8:53
 * @description
 */

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录！");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.insertDiscussPost(post);

        //触发发帖事件
        Event event=new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        //报错情况，将来统一处理

        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        //帖子
        DiscussPost discussPost = discussPostService.getDiscussPost(discussPostId);
        sensitiveFilter.filter(discussPost);
        model.addAttribute("post", discussPost);

        //作者
        User user = userService.getUser(discussPost.getUserId());
        model.addAttribute("user", user);

        //帖子的点赞数量
        long likeCount = likeService.countLike(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);

        //登陆用户对帖子的点赞状态
        int likeStatus = 0;
        if (hostHolder.getUser()!=null){
            likeStatus=likeService.getLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        }
        model.addAttribute("likeStatus", likeStatus);

        //评论的分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(discussPost.getCommentCount());
        model.addAttribute("page", page);

        List<Comment> commentList = commentService.ListCommentByEntity(ENTITY_TYPE_POST, discussPostId, page.getOffset(), page.getLimit());

        List<ViewObject> commentVOs = new ArrayList<>();
        ;

        if (commentList != null) {
            for (Comment comment : commentList) {
                sensitiveFilter.filter(comment);

                ViewObject commentVO = new ViewObject();
                commentVO.put("comment", comment);
                commentVO.put("user", userService.getUser(comment.getUserId()));

                //评论的点赞数量
                likeCount = likeService.countLike(ENTITY_TYPE_COMMENT, comment.getId());
                commentVO.put("likeCount", likeCount);

                //登陆用户对评论的点赞状态
                likeStatus = 0;
                if (hostHolder.getUser()!=null){
                    likeStatus=likeService.getLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                }
                commentVO.put("likeStatus", likeStatus);

                List<Comment> replyList = commentService.ListCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<ViewObject> replyVOs = new ArrayList<>();


                if (replyList != null) {
                    for (Comment reply : replyList) {
                        sensitiveFilter.filter(reply);

                        ViewObject replyVO = new ViewObject();
                        replyVO.put("reply", reply);
                        replyVO.put("user", userService.getUser(reply.getUserId()));
                        User target = reply.getTargetId() == 0 ? null : userService.getUser(reply.getTargetId());
                        replyVO.put("target", target);

                        //回复的点赞数量
                        likeCount = likeService.countLike(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVO.put("likeCount", likeCount);

                        //登陆用户对回复的点赞状态
                        likeStatus = 0;
                        if (hostHolder.getUser()!=null){
                            likeStatus=likeService.getLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        }
                        replyVO.put("likeStatus", likeStatus);


                        replyVOs.add(replyVO);
                    }
                }
                commentVO.put("replyVOs", replyVOs);

                int replyCount = commentService.CountCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVO.put("replyCount", replyCount);

                commentVOs.add(commentVO);
            }
        }
        model.addAttribute("commentVOs", commentVOs);


        return "/site/discuss-detail";
    }


}
