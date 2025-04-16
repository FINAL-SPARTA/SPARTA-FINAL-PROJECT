package com.fix.order_service.application.aop;

import com.fix.order_service.application.exception.OrderException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

import static com.fix.order_service.application.exception.OrderException.OrderErrorType.ORDER_ROLE_HEADER_MISSING;
import static com.fix.order_service.application.exception.OrderException.OrderErrorType.ORDER_ROLE_UNAUTHORIZED;

@Aspect
@Component
@Slf4j
public class ValidateUserAspect {

    @Around("@annotation(validateUser)")
    public Object validateUserHeader(ProceedingJoinPoint joinPoint, ValidateUser validateUser) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs.getRequest();

        String userId = request.getHeader("x-user-id");
        String userRole = request.getHeader("x-user-role");

        if (userId == null || userRole == null) {
            log.warn("Missing headers - userId: {}, userRole: {}", userId, userRole);
            throw new OrderException(ORDER_ROLE_HEADER_MISSING);
        }

        String[] allowedRoles = validateUser.roles();
        boolean authorized = Arrays.stream(allowedRoles)
                .anyMatch(role -> role.equalsIgnoreCase(userRole));

        if (!authorized) {
            log.warn("Access denied. userId: {}, role: {}, allowedRoles: {}", userId, userRole, Arrays.toString(allowedRoles));
            throw new OrderException(ORDER_ROLE_UNAUTHORIZED);
        }

        return joinPoint.proceed();
    }
}
