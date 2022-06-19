package asia.daijizai.community;

import asia.daijizai.community.util.MailClient;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/6 17:45
 * @description
 */

@SpringBootTest
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Test
    public void testTextMail(){
        mailClient.sendMail("1162667741@qq.com","TEST","高考加油！");
    }

}
