package asia.daijizai.community.controller.interceptor;

import asia.daijizai.community.entity.User;
import asia.daijizai.community.service.MessageService;
import asia.daijizai.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/17 14:59
 * @description
 */

@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            int unreadLetterCount = messageService.countUnreadLetter(user.getId(), null);
            int unreadNoticeCount = messageService.countUnreadNotice(user.getId(), null);
            modelAndView.addObject("allUnreadCount", unreadLetterCount + unreadNoticeCount);
        }
    }
}
