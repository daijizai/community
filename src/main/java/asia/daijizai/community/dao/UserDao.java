package asia.daijizai.community.dao;

import asia.daijizai.community.entity.User;
import org.apache.ibatis.annotations.*;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/5 23:18
 * @description
 */

@Mapper
public interface UserDao {

    String selectFields = "id, username, password, salt, email, type, status, activation_code, header_url, create_time";
    String insertFields = "username, password, salt, email, type, status, activation_code, header_url, create_time";

    @Select("select " + selectFields + " from user where id = #{id}")
    User getUser(@Param("id") int id);

    @Select("select " + selectFields + " from user where username = #{username}")
    User getByUsername(@Param("username") String username);

    @Select("select " + selectFields + " from user where email = #{email}")
    User getByEmail(@Param("email") String email);

    @Insert("insert into user (" + insertFields + ") values (#{user.username}, #{user.password}, #{user.salt}, #{user.email}, #{user.type}, #{user.status}, #{user.activationCode}, #{user.headerUrl}, #{user.createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(@Param("user") User user);

    @Update("update user set status = #{status} where id = #{id}")
    int updateStatus(@Param("id") int id, @Param("status") int status);

    @Update("update user set header_url = #{headerUrl} where id = #{id}")
    int updateHeaderUrl(@Param("id") int id, @Param("headerUrl") String headerUrl);

}
