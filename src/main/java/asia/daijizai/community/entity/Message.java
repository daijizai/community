package asia.daijizai.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/11 19:47
 * @description
 */

@Data
public class Message {
    private int id;
    private int fromId;
    private int toId;
    private String conversationId;
    private String content;
    private int status;
    private Date createTime;
}
