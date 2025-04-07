package com.fix.game_service.infrastructure.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Nullable;

@Configuration
public class PageableConfig implements WebMvcConfigurer {

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new CustomPageableHandlerMethodArgumentResolver());
	}

	public static class CustomPageableHandlerMethodArgumentResolver extends PageableHandlerMethodArgumentResolver {

		private static final List<Integer> ALLOWED_SIZES = List.of(10, 30, 50);

		@Override
		public Pageable resolveArgument(
			MethodParameter methodParameter,
			@Nullable ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest,
			@Nullable WebDataBinderFactory binderFactory) {

			Pageable pageable = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

			int page = pageable.getPageNumber();
			int size = pageable.getPageSize();
			Sort sort = pageable.getSort();

			if (!ALLOWED_SIZES.contains(size)) {
				size = 10; // 기본값
			}

			return PageRequest.of(page, size, sort.isSorted() ? sort : Sort.by(Sort.Direction.DESC, "createdAt"));
		}
	}
}
