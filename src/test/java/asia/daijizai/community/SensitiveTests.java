package asia.daijizai.community;

import asia.daijizai.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/7 23:55
 * @description
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class SensitiveTests {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter(){
        String text="你真是个s b，你真是个n@1@t哈哈！！！";
        System.out.println(sensitiveFilter.filter(text));
    }

}
