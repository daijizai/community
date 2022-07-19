package asia.daijizai.community.service;

import asia.daijizai.community.util.RedisKeyUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/27 23:06
 * @description
 */

public interface DataService {

    void recordUV(String ip) ;

    long calculateUV(Date start, Date end) ;

    void recordDAU(int userId) ;

    long calculateDAU(Date start, Date end);
}
