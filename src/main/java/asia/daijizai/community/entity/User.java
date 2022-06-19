package asia.daijizai.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/5 23:18
 * @description
 */

@Data
public class User {
    private int id;
    private String username;
    private String password;
    private String salt;
    private String email;
    private int type;
    private int status;
    private String activationCode;
    private String headerUrl;
    private Date createTime;
}
