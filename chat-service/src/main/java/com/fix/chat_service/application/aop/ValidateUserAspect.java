package com.fix.chat_service.application.aop;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.w3c.dom.events.EventException;

import com.fix.chat_service.application.exception.ChatException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

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
			throw new ChatException(ChatException.ChatErrorType.CHATROOM_CANNOT_ACCESS);
		}

		// 역할 확인
		String[] allowedRoles = validateUser.roles();
		boolean authorized = Arrays.stream(allowedRoles)
			.anyMatch(role -> role.equalsIgnoreCase(userRole));

		if (!authorized) {
			log.warn("Access denied. userId: {}, role: {}, allowedRoles: {}", userId, userRole, Arrays.toString(allowedRoles));
			throw new ChatException(ChatException.ChatErrorType.CHATROOM_CANNOT_ACCESS);
		}

		return joinPoint.proceed();
	}
}

