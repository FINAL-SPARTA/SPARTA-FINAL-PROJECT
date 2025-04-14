package com.fix.alarm_service.domain.model;

public enum AlarmType {

    //예매 완료 알림 - 사용자가 티켓을 정상적으로 예매했을때 , KAFKA 통해 주문 or 결제 도메인에서 전달받은 이벤트 기반
    RESERVATION_COMPLETE("예매가 완료되었습니다."),

    //예매 취소 알림 - 사용자가 티켓 예매를 취소했을때 ,KAFKA EVENT 수신 후 생성
    RESERVATION_CANCELED("예매가 취소되었습니다."),

    //경기 하루 전 알림 - 스케줄러를 통해 자동으로 예약 발송
    MATCH_REMINDER("내일 예매한 경기가 열립니다.");

    private final String description;

    AlarmType(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}
