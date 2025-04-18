package com.fix.game_service.infrastructure.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

	@Bean(name = "queueExecutor")
	public Executor queueExecutor() {
		// ThreadPoolExecutor를 쉽게 사용할 수 있도록 해주는 일종의 Wrapper 클래스
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		// 기본 스레드 개수를 5개로 설정 - 미리 생성해둠
		executor.setCorePoolSize(5);
		// 최대 10개로 설정 - 동시 처리 작업이 더 많아지는 경우 스레드를 10까지 늘림
		executor.setMaxPoolSize(10);
		// 대기 큐 100개 - 스레드가 모두 일하고 있을 때 (corepool) 새 작업이 들어오면 100개까지 대기열에 쌓아둠
		// 이 대기 개수를 넘어가면 maxPoolSize 10까지 스레드를 늘림
		executor.setQueueCapacity(100);
		// 생성되는 스레드 이름 앞에 QueueExecutor1, QueueExecutor2 처럼 이름 붙임 (어떤 스레드가 일하는지 확인 가능)
		executor.setThreadNamePrefix("QueueExecutor-");
		// 스레드 풀 초기화 - 실제 스레드 풀이 동작하도록 설정 마무리
		executor.initialize();
		// 반환
		return executor;
	}

	/**
	 * 비동기 에외 처리를 위한 핸들러
	 * @return : 예외 처리
	 */
	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return (throwable, method, params) -> {
			log.error("비동기 예외 발생 - 메서드: {} 파라미터: {}", method.getName(), params, throwable);
			// 해당 오류 로그 저장 및 모니터링에 추가 가능
		};
	}

}
