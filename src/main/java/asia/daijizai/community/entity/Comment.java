package asia.daijizai.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/10 21:20
 * @description
 */

@Data
public class Comment {
    private int id;
    private int userId;
    private int entityType;
    private int entityId;
    private int targetId;
    private String content;
    private int status;
    private Date createTime;
}
