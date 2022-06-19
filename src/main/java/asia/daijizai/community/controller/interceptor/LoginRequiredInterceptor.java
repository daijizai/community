package asia.daijizai.community.controller.interceptor;

import asia.daijizai.community.annotation.LoginRequired;
import asia.daijizai.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/7 22:01
 * @description
 */

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){
            HandlerMethod handlerMethod=(HandlerMethod) handler;
            Method method=handlerMethod.getMethod();
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            if (loginRequired!=null&&hostHolder.getUser()==null){
                //有LoginRequired注解且没有登录
                response.sendRedirect(request.getContextPath()+"/login");//重定向到登录页面
                return false;
            }
        }

        return true;
    }
}
