package com.fix.common_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({"status", "code", "message", "data"}) // JSON 필드 순서 지정
public class CommonResponse<T> {

    private static final String SUCCESS_CODE = "S000";
    private static final String FAIL_CODE = "F000";
    private static final String CREATED_CODE = "C000";

    private final int status;  // HTTP 상태 코드 추가
    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL) // null 값은 JSON에서 제외
    private final T data;

    public static <T> CommonResponse<T> success(T data, String message) {
        return CommonResponse.<T>builder()
            .status(200)  // 성공 상태 코드 포함
            .code(SUCCESS_CODE)
            .data(data)
            .message(message)
            .build();
    }

    public static <T> CommonResponse<T> fail(String errorCode, String message, int status) {
        return CommonResponse.<T>builder()
            .status(status)  // HTTP 상태 코드 적용
            .code(errorCode != null ? errorCode : FAIL_CODE)
            .message(message != null ? message : "요청 처리 중 오류가 발생했습니다.")
            .build();
    }

    public static <T> CommonResponse<T> created(T data, String message) {
        return CommonResponse.<T>builder()
            .code(CREATED_CODE)
            .data(data)
            .message(message)
            .build();
    }
}
