package asia.daijizai.community;

import java.io.IOException;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/29 18:23
 * @description
 */
public class WkTests {

    public static void main(String[] args) {
//        String cmd = "D:/Program Files/wkhtmltopdf/bin/wkhtmltoimage --quality 75 https://www.baidu.com d:/work/data/wk-images/1.png";
        String cmd = "D:/Program Files/wkhtmltopdf/bin/wkhtmltoimage --quality 75  https://www.nowcoder.com d:/work/data/wk-images/3.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
