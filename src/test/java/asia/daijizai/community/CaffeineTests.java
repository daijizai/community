package asia.daijizai.community;

import asia.daijizai.community.entity.DiscussPost;
import asia.daijizai.community.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTests {

    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void initDataForTest() {
        for (int i = 0; i < 500000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("压力测试");
            post.setContent("50万之一");
            post.setCreateTime(new Date());
            post.setScore(Math.random() * 2000);
            discussPostService.insertDiscussPost(post);
        }
    }

    @Test
    public void testCache() {
        System.out.println(discussPostService.listDiscussPost(0, 0, 10, 1));
        System.out.println(discussPostService.listDiscussPost(0, 0, 10, 1));
        System.out.println(discussPostService.listDiscussPost(0, 0, 10, 1));
        System.out.println(discussPostService.listDiscussPost(0, 0, 10, 0));
    }

}
