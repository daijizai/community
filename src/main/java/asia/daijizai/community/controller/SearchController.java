package asia.daijizai.community.controller;

import asia.daijizai.community.entity.DiscussPost;
import asia.daijizai.community.entity.Page;
import asia.daijizai.community.entity.ViewObject;
import asia.daijizai.community.service.ElasticsearchService;
import asia.daijizai.community.service.LikeService;
import asia.daijizai.community.service.UserService;
import asia.daijizai.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/19 16:08
 * @description
 */

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/search",method = RequestMethod.GET)
    public String search(String keyword, Page page,Model model){
        //搜索帖子
        org.springframework.data.domain.Page<DiscussPost> searchResult =
                elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

        //聚合数据
        List<ViewObject> searchResultVOs=new ArrayList<>();
        if (searchResult!=null){
            for (DiscussPost post:searchResult){
                ViewObject vo=new ViewObject();
                //帖子
                vo.put("post",post);
                //作者
                vo.put("user",userService.getUser(post.getUserId()));
                //点赞数量
                vo.put("likeCount",likeService.countLike(ENTITY_TYPE_POST,post.getId()));

                searchResultVOs.add(vo);
            }
        }

        model.addAttribute("searchResultVOs",searchResultVOs);
        model.addAttribute("keyWord",keyword);

        //分页信息
        page.setPath("/search?keyword="+keyword);
        page.setRows(searchResult==null?0:(int)searchResult.getTotalElements());

        return "site/search";
    }
}
