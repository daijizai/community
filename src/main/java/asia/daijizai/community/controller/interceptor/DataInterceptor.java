package asia.daijizai.community.controller.interceptor;

import asia.daijizai.community.entity.User;
import asia.daijizai.community.service.DataService;
import asia.daijizai.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/27 23:08
 * @description
 */

@Component
public class DataInterceptor implements HandlerInterceptor {

    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录UV
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);

        // 记录DAU
        User user = hostHolder.getUser();
        if (user != null) {
            dataService.recordDAU(user.getId());
        }

        return true;
    }
}
