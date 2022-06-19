package asia.daijizai.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/7 11:10
 * @description
 */

@Data
public class LoginTicket {
    private int id;
    private int userId;
    private String ticket;
    private int status;
    private Date expired;
}
