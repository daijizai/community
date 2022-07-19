package asia.daijizai.community.controller;

import asia.daijizai.community.entity.DiscussPost;
import asia.daijizai.community.entity.Page;
import asia.daijizai.community.entity.User;
import asia.daijizai.community.entity.ViewObject;
import asia.daijizai.community.service.DiscussPostService;
import asia.daijizai.community.service.LikeService;
import asia.daijizai.community.service.UserService;
import asia.daijizai.community.util.CommunityConstant;
import asia.daijizai.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/5 23:32
 * @description
 */

@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {

        page.setRows(discussPostService.countDiscussPost(0));
        page.setPath("/index?orderMode="+orderMode);//注意下这里的路径

        List<DiscussPost> discussPostList = discussPostService.listDiscussPost(0, page.getOffset(), page.getLimit(), orderMode);
        List<ViewObject> discussPostVOs = new ArrayList<>();

        if (discussPostList != null) {
            for (DiscussPost discussPost : discussPostList) {
                sensitiveFilter.filter(discussPost);

                ViewObject discussPostVO = new ViewObject();
                discussPostVO.put("post", discussPost);

                User user = userService.getUser(discussPost.getUserId());
                discussPostVO.put("user", user);

                long likeCount = likeService.countLike(ENTITY_TYPE_POST, discussPost.getId());
                discussPostVO.put("likeCount", likeCount);

                discussPostVOs.add(discussPostVO);
            }
        }

        model.addAttribute("discussPostVOs", discussPostVOs);
        model.addAttribute("page", page);
        model.addAttribute("orderMode", orderMode);
        return "/index";
    }

    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }

}
