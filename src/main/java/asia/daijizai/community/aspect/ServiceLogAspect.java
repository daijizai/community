package asia.daijizai.community.aspect;

import asia.daijizai.community.controller.UserController;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/12 18:30
 * @description
 */

@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    //声明切点
    @Pointcut("execution(* asia.daijizai.community.service.*.*(..))")
    public void pointcut() {

    }

    //前置通知，在开始时织入
    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        // 用户[1.2.3.4],在[xxx],访问了[asia.daijizai.community.service.xxx()].
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes==null){
            //为什么要这么处理？
            //切点在service，只要调用的了service方法就会执行before()方法。如果是controller层调用service，attributes肯定不会为空
            //但是EventConsumer调用service时，attributes就为空了。
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
            logger.info(String.format("系统内部调用,在[%s],访问了[%s].", now, target));
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));
    }

}
