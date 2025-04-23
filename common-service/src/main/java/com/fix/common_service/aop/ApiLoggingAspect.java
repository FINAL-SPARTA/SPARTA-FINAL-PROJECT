package com.fix.common_service.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ApiLoggingAspect {

    private final ObjectMapper objectMapper;
    private static final String TRACE_ID = "traceId";

    @Around("@annotation(com.fix.common_service.aop.ApiLogging)")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String traceId = MDC.get(TRACE_ID);
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getMethod().getName();

        HttpServletRequest request =
            ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();

        String userId = request.getHeader("x-user-id");
        String userRole = request.getHeader("x-user-role");

        if (userId == null) userId = "UNKNOWN_USER";
        if (userRole == null) userRole = "UNKNOWN_ROLE";

        String paramsJson = "";
        try {
            paramsJson = paramsToString(joinPoint.getArgs(), signature.getParameterNames());
        } catch (Exception e) {
            log.warn("[{}] API 요청 파라미터 JSON 변환 오류: {}", traceId, e.getMessage());
        }

        log.info("[{}] API START: userId={}, userRole={}, {} {} | Method={}.{} | Params={}",
            traceId, userId, userRole, httpMethod, requestURI, className, methodName, paramsJson);

        Object result;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable throwable) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.error("[{}] API ERROR: userId={}, userRole={}, {} {} | Method={}.{} | Duration={}ms | Error={}",
                traceId, userId, userRole, httpMethod, requestURI, className, methodName, duration, throwable.getMessage(), throwable);
            throw throwable; // 예외를 다시 던져서 호출자에게 전달
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.info("[{}] API END: userId={}, userRole={}, {} {} | Method={}.{} | Duration={}ms",
                traceId, userId, userRole, httpMethod, requestURI, className, methodName, duration);
        }
    }

    // 메서드 파라미터를 안전하게 문자열(JSON)으로 변환하는 메서드
    private String paramsToString(Object[] params, String[] paramNames) {
        if (params == null || params.length == 0) {
            return "NONE";
        }
        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 0; i < params.length; i++) {
            String name = paramNames[i];
            Object value = params[i];

            // 민감 정보, HttpServletRequest/Response, BindingResult 등 로깅 제외/마스킹
            if (name.toLowerCase().contains("password") ||
                value instanceof jakarta.servlet.ServletRequest ||
                value instanceof jakarta.servlet.ServletResponse ||
                value instanceof org.springframework.web.multipart.MultipartFile ||
                value instanceof org.springframework.ui.Model ||
                value instanceof org.springframework.validation.BindingResult) {
                paramMap.put(name, "*** MASKED / EXCLUDED ***");
            } else {
                paramMap.put(name, value);
            }
        }
        try {
            // 너무 큰 요청 로깅 방지 (2000자 제한)
            String json = objectMapper.writeValueAsString(paramMap);
            if (json.length() > 2000) {
                return json.substring(0, 2000) + "...(truncated)";
            }
            return json;
        } catch (Exception e) {
            return "Params serialization failed: " + e.getMessage();
        }
    }
}
