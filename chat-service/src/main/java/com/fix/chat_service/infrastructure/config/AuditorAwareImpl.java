package com.fix.chat_service.infrastructure.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<Long> {

	@Override
	public Optional<Long> getCurrentAuditor() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

		if (attributes == null) return Optional.empty();

		HttpServletRequest request = attributes.getRequest();
		String userIdHeader = request.getHeader("X-User-Id");

		try {
			return Optional.ofNullable(userIdHeader).map(Long::valueOf);
		} catch (NumberFormatException e) {
			return Optional.empty(); // 유효하지 않으면 null
		}
	}
}

