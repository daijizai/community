package asia.daijizai.community.dao;

import asia.daijizai.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/7 11:15
 * @description
 */

@Mapper
@Deprecated
public interface LoginTicketDao {


    @Insert("insert into login_ticket (user_id, ticket, status, expired) " +
            "values(#{loginTicket.userId}, #{loginTicket.ticket}, #{loginTicket.status}, #{loginTicket.expired})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(@Param("loginTicket") LoginTicket loginTicket);

    @Select("select id, user_id, ticket, status, expired " +
            "from login_ticket where ticket = #{ticket}")
    LoginTicket getByTicket(@Param("ticket") String ticket);

    @Update("update login_ticket set status = #{status} where ticket = #{ticket}")
    int updateStatus(@Param("ticket") String ticket,@Param("status")int status);
}
