## 𝑻𝒆𝒆𝒑𝒊𝒄𝒌 - 𝑭𝑰𝑿
### ⚾ 𝑻𝒆𝒆𝒑𝒊𝒄𝒌 : 티켓 예매, 이벤트, 알림, 채팅까지! 야구 팬을 위한 올인원 서비스 플랫폼 ⚾
![](https://velog.velcdn.com/images/azuressu/post/1973361b-56a5-4938-a5dc-9d5c68c8fcec/image.png)

<br>

## 𝑭𝑰𝑿 팀원 소개
|정민수|이수연|권길남|이종원|
|:----:|:----:|:----:|:----:|
|[@dbp-jack](https://github.com/dbp-jack)|[@azuressu](https://github.com/azuressu)|[@nimpa3201](https://github.com/nimpa3201)|[@zapzookj](https://github.com/zapzookj)|
|주문, 결제 도메인, 배포|경기, 채팅 도메인|이벤트, 티켓 도메인, 배포|알람, 경기장 도메인|

<details>
<summary>담당 역할 자세히 보기</summary>
<div markdown="1">  

  - 정민수
  
    - 프로젝트 기획 및 관리
      - 프로젝트 전반의 일정 수립 및 관리
      - 주간 회의 주최 및 진행, 진행 상황 점검
      - 팀원들의 다양한 의견 수렴 및 피드백 반영
      - JIRA 셋팅과 백로그/스프린트 관리를 주도하고, Slack 연동을 통해 커밋·PR 알림을 실시간 공유하며 개발 관리를 효율화

    - 주문 도메인
      - 주문 비즈니스 로직 설계 및 생성 처리 로직 구현
      - Kafka 기반 이벤트 흐름 설계(주문 상태 변경 처리)
      - 주문 취소 처리 로직 구현
      - 주문 생성 실패에 대한 보상 트랜잭션 처리
      - JPA 기반 주문 CRUD 구현
      - 주문-결제 연계 처리 설계 및 구현

    - 결제 도메인
      - 결제 비즈니스 로직 설계 및 결제 승인 처리 로직 구현
      - Kafka 기반 이벤트 연동    
      - 결제 상태 검증 및 중복 방지 처리
      - 결제 실패 처리 보상 트랜잭션 구현
      - 결제 실패에 따른 주문 보상 트랜잭션 처리
      - 결제 취소에 따른 주문 연계 처리 및 로직 구현 
      - Toss Payment API 연동
      - Kafka 성능 및 부하 테스트용 모킹 구현

    - 배포
      - AWS Cloud 활용해 인프라 배포환경 구축
      - VPC Network 환경 구성
      - ECR Repository 구성
      - ECS(Fargate)기반 클러스터 및 서비스 구축
      - Dockerfile 작성 및 Image Build

    - CI / CD 파이프라인 구축
      - GitHub Actions를 사용하여 코드 푸시 및 PR 생성 시 자동으로 빌드, 테스트 수행.
    
  
  
- 이수연
  - 프로젝트 일정 관리
    - 전체적인 프로젝트 일정을 파악하여 프로젝트 진행 및 관리에 기여
    - 매일 데일리 스크럼을 기록하여 팀원 간 작업 현황과 계획 공유 및 프로젝트 진행 상황 관리
    - 기능 설계 및 시스템 흐름의 문서화를 통해 팀 내 기술 공유 및 협업 생산성 향상에 기여

  - 경기 도메인 구현
    - 경기 정보 기본 CRUD API 구현
    - 경기 생성 시, 각각 Alarm과 Chat 서비스로 이벤트 전파
        - 기존 Feign 기반 동기 방식에서 Kafka 기반 이벤트 전파 처리로 변경하여 평균 응답 시간 약 28% 감소
        - 또한, 메시지 유실 방지를 위해 outbox 테이블에 저장하고, 별도 스케줄러를 통하여 이벤트 전파 후 이벤트 상태 전환 처리

  - 대기열 시스템
    - Redis Sorted Set 기반의 대기열 기능 구현
        - 사용자 입장 순서대로 토큰 발급 후 대기열에 저장하여 순서 관리
     - WebSocket 기반 대기 번호 전송
        - 대기하고 있는 사용자를 대상으로 WebSocket 연결을 통해 연결 상태를 감지하고, 변화하는 대기 번호를 전송
  
  - 실시간 채팅 기능 구현
      - WebSocket 기반 채팅 기능 구축
          - 실시간 채팅 기능 구현을 위한 WebSocket 도입
          - WebSocketHandler 커스터마이징을 통해 사용자 메시지를 수신하며, 경기별로 채팅방을 분리하여 메시지 라우팅 구현
      - Kafka 기반 메시지 브로커 연동
        - 채팅 메시지를 Kafka Producer로 발행, Kafka Consumer가 메시지를 다시 브로드캐스트
        - 분산된 환경에서도 안정적으로 메시지를 전달하고, 확장성을 확보
     - 사용자 닉네임 기반 표시
        - 로그인 한 사용자의 닉네임이 표시되도록 WebSocket Intercepter 구현
    - Gateway WebSocket Filter 적용

  - JMeter 기반 성능 테스트 수행
    - 티켓 예매 로직의 처리량 비교 테스트
       - Feign → Redis → Kafka 로 점진적으로 로직을 개선하는 과정마다 테스트 수행
       - 이를 통해 처리량은  약 81% 증가하였으며, 평균 응답 시간은 약 83% 감소하였음을 확인
    - 채팅 메시지 브로커 성능 테스트
       - Redis Pub/Sub 방식과 Kafka 방식의 성능 테스트를 통해 수치를 비교하고, 비교적 안정적인 Kafka 방식을 채택
  
  
  
- 권길남
  - 경기장 도메인 구현
    - 도메인 중심 설계(Domain-Driven Design) 적용
      - Stadium, Seat를 도메인 모델로 명확히 분리하여 책임 기반의 객체 설계 수행.
      - Stadium, Seat은 각각의 독립적인 도메인 개체로서 도메인 로직을 내장하고, 엔티티 간 연관관계는 Stadium → Seat 단방향 Aggregate Root 형태로 설계

    - 복합 인덱스를 통한 구역별 좌석 조회 성능 최적화
      - @Table(indexes = { @Index(name = "idx_seat_stadium_section", columnList = "stadium_id, section") })을 통해 좌석 테이블에 복합 인덱스 구성.
      - stadiumId + section 조건으로 좌석을 조회할 경우 인덱스 Range Scan이 작동하여 조회 성능을 대폭 향상시킴.

    - 캐시를 활용한 정적 데이터 응답 최적화
      - 좌석 구역 정보(SeatSection.name()) 및 팀명 기반 경기장 정보(StadiumName.fromTeamName(teamName)) 조회 시 각각 @Cacheable("seatSectionsCache"), @Cacheable("stadiumInfoCache") 캐시 적용.
      - Redis 기반 캐시 적용을 통해 반복 요청에 대한 DB I/O 비용을 줄이고 응답 속도 개선.

  - 알림 도메인 구현
      - Kafka 기반 메시지 소비 구조 설계
        - 도메인 간 결합도를 최소화하기 위해 알림 발송 트리거는 Kafka 이벤트 기반으로 설계.
  	  - RedisIdempotencyChecker를 적용하여 메시지 중복 수신 방지
    - 멀티 이벤트 소비자 구조 적용
  	  - 경기 하루 전 알림: AlarmService가 매일 자정 실행되는 Scheduler에서 Kafka 이벤트 발행 → Order 서비스에서 userId 목록을 응답
      - 예매 완료 알림: Order 서비스에서 Kafka 이벤트 발행 → 알림 서비스가 소비 후 SNS 전송.
      - 이벤트 당첨자 알림: Event 서비스에서 당첨자 ID 리스트 포함 Kafka 이벤트 발행 → 수신 후 당첨 축하 메시지 개별 발송.
    - 유저 정보 조회를 위한 동기 통신 구조 구성
      - 알림 발송 시 수신자의 전화번호가 필요하므로, user-service와의 연동을 위해 @FeignClient를 활용한 비동기 HTTP API 호출 구현.

  - 성능 및 부하 테스트 (nGrinder 기반)
      - nGrinder를 이용한 API 부하 테스트 수행
        - 예매 요청, 알림 발송 등 주요 API의 초당 요청 처리량(RPS) 측정
        - 예매 요청, 알림 발송 등 성능 병목이 우려되는 주요 API를 대상으로 부하 테스트를 수행
        - Groovy 기반 nGrinder 스크립트를 작성하여 테스트 흐름 구성
        - 사용자 인증 → 티켓 예매 요청 → 응답 코드/시간 검증까지 포함된 시나리오 구성
        - TestRunner.groovy 내부에 Think Time, Loop, Assertion 설정을 통해 실제 사용자 행위에 근접하게 시뮬레이션
      - 테스트 환경은 로컬 환경 및 컨테이너 기반으로 분리
        - nGrinder Controller/Agent를 도커 및 로컬 양쪽에 배포하여 테스트 다각화.
      - 테스트 수행 항목
        - 사용자 수 증가에 따른 TPS(RPS), 응답 시간, 에러율 측정
        - 50명, 100명, 200명 등 가상의 동시 사용자 수 조절을 통해 최대 처리 임계점 파악
        - Throughput(처리량), Error Rate, 평균/최대 응답 시간 지표 수집
 
- 이종원
  - 이벤트 도메인 구현
    - 포인트 시스템과 연동되는 이벤트 생성 및 조회, 사용자의 포인트 기반 응모 처리(이벤트 기반 분산 트랜잭션 포함), 당첨자 선정 등 이벤트 라이프 사이클 전반의 기능 구현
    - Quartz(클러스터링, 동적 트리거)를 활용한 스케줄러
       - 분산 환경에서도 이벤트 시작/종료 시점에 맞춰 상태를 정확하고 안정적으로 자동 변경하는 스케줄링 기능 구현

  - 티켓 도메인
    - 티켓 예매 시 락 적용으로 데이터 일관성 유지
      - Redisson MultiLock을 적용하여 여러 좌석의 동시성 티켓 예매 시 레이스 컨디션 방지
	
    - Kafka기반 비동기 이벤트 전파
      - 상태 변경 시 이벤트 발행 및 다른 도메인의 이벤트 및 보상 트랜잭션 이벤트를 수신하여 타 서비스와의 느슨하고 안정적인 결합 구현  
    - 티켓 예매 API 요청과 비동기 작업 큐 분리로 처리량 극대화
      - REST API 응답은 즉시 완료, 실제 비즈니스 로직은 Kafka Consumer에서 비동기 처리
      - 파티션 + Concurrency 병렬 처리로 대용량 트래픽 환경에서도 안정적 처리 보장
   - Redis Keyspace Notifications를 활용한 미결제 티켓 자동 정리
     - Redis TTL 만료 이벤트를 구현하여 자동으로 미결제 티켓 정리
     - 별도 스케줄러 없이 실시간 데이터 정리 구현

  - ELK를 활용한 로깅 및 모니터링
    - 로그 수집 정책 수립
      - 로그 레벨별(INFO/WARN/ERROR) 수집 기준 정의 및 JSON 포맷 표준화
    - 애플리케이션 코드 레벨 로그 구성
      - @Slfj4를 사용해 도메인 별 핵심 기능의 로그 작성
      - Logback + p6spy 설정으로 애플리케이션 로그 표준화
      - MDC를 활용해 분산 트랜잭션 추적성 확보
    - ELK 스택 및 Metricbeat 기반 파이프라인 구축
      - FluentD로 로그, Metricbeat로 매트릭 데이터 수집 및 Elasticsearch 저장
      - Logstash를 활용한 로그 필터링 및 가공
      - Kibana 대시보드를 통해 실시간 로그 검색 및 모니터링 시각화
  
</div>
</details>


#### ⇒ [팀 노션 바로가기](https://www.notion.so/teamsparta/9-FIX-1c82dc3ef51481bdaaf6ecf9f501164c)
#### ⇒ [프로젝트 노션 바로가기](https://www.notion.so/9-1e42dc3ef5148051a3e8ee1fbdcff070?pvs=25)
#### ⇒ [지라 프로젝트 바로가기](https://jira.external-share.com/issue/3debbd45-19d2-4f6a-b865-47bb218c1d37)

<br>

## 𝑻𝒆𝒆𝒑𝒊𝒄𝒌 기능
<details>
<summary>게이트웨이 (Gateway)</summary>
<div markdown="1">

- JWT 검증
  - 사용자의 요청이 들어오면, Header에 담긴 JWT를 검증하는 역할
  - Access Token의 검증을 통해 사용자의 인증 여부를 확인하고, 
각 서비스에서는 사용자 서비스 호출 없이 검증이 통과된 정보를 통해
사용자 정보 사용 가능

</div>
</details>


<details>
<summary>티켓 (Ticket)</summary>
<div markdown="1">

- 비동기 티켓 예매 처리
  - **Kafka 기반 요청 분리** : 사용자의 예매 요청 접수 시, 실제 처리 로직을 즉시 실행하는 대신 Kafka의 토픽으로 이벤트를 발행한 뒤 빠르게 응답을 반환, 예매 처리는 별도의 Worker를 통해 처리
  - **파티셔닝 및 병렬 처리** : 경기 Id와 좌석 Id를 파티션 키로 사용하여 특정 좌석에 대한 요청은 순서를 보장하며 서로 다른 좌석에 대한 요청은 Kafka의 여러 파티션으로 분산, 다중 Consumer 스레드가 각 파티션의 메시지를 병렬로 처리하여 시스템의 처리량 극대화
  
- 원자적 예매 보장 및 동시성 제어
  - **분산 락 (Redisson MultiLock)** : 비동기 처리 환경에서도 여러 좌석을 한 번에 예매하는 요청의 원자성을 보장하기 위해, MultiLock 사용.
  - **다중 중복 체크** : 락 획득 후, 캐시 조회와 DB 조회를 통해 해당 좌석의 예매/판매 여부를 이중으로 검증하여 중복 예매를 철저히 방지

- 예약 후 미결제 티켓 자동 정리
  - **Redis Keyspace Notification 활용** : 티켓 예매 시 생성되는 캐시 데이터의 TTL을 3분으로 설정
  - **만료 이벤트 기반 삭제** : Redis 키가 TTL 만료로 삭제될 때 발생하는 이벤트를 KeyExpiredListener가 감지
  - **DB 정리** : 만료된 ticketId에 해당하는 티켓을 정확한 시점에 삭제.

- 예약 후 미결제 티켓 자동 정리
  - **이벤트 발행** : 티켓 상태 변경 시 관련 이벤트를 Kafka 토픽으로 발행하여 다른 서비스와 정보를 동기화
    - TICKET_RESERVED : 최종 예매 성공 시 Order Service로 발행 (주문 생성 요청)
    - TICKET_SOLD : 결제 완료 후 Game Service로 발행 (잔여 좌석 수 차감)
    - TICKET_CANCELLED : 주문 취소 시 Game Service로 발행 (잔여 좌석 수 복구)
    - TICKET_RESERVATION_SUCCEEDED / FAILED : 비동기 예매 처리 최종 결과 발행
  - **이벤트 구독 및 처리** : 다른 서비스에서 발행된 이벤트를 구독하여 티켓 상태 업데이트 및 보상 트랜잭션 수행
    - ORDER_COMPLETED : 결제 완료 시 티켓 상태를 SOLD로 변경
    - ORDER_CANCELLED : 주문 취소 시 티켓 상태를 CANCELLED로 변경
    - ORDER_CREATION_FAILED / ORDER_COMPLETIONFAILED : Saga 패턴 실패 시 티켓 삭제 등 보상 트랜잭션 처리
  
</div>
</details>


<details>
<summary>주문 (Order)</summary>
<div markdown="1">

- 비동기 주문 생성 및 처리
  - **Kafka 기반 주문 요청 분리**
    - 사용자의 주문 요청(Order Create API) 수신 시, 즉시 주문 데이터를 저장하고 Kafka `order-created-topic`으로 이벤트 발행.
    - 결제 서비스 등 후속 처리는 Kafka 이벤트를 통해 비동기로 연계.
    - 빠른 사용자 응답 반환과 비즈니스 로직 분산 처리 동시 달성.
  - **이벤트 기반 주문 생성 흐름**
    - `TICKET_RESERVED` 수신 → 주문 생성
    - 주문 생성 완료 후 `ORDER_CREATED` 이벤트 발행하여 결제 프로세스 트리거.
- 주문 상태 관리 및 중복 이벤트 처리 방지
  - **기반 전이(Order Status Transition)**
    - 주문은 명시적 상태(`CREATED`,`COMPLETED`,`CANCELLED`)를 가진다.
    - Kafka 이벤트 수신 시 상태 전이를 철저히 제어하여 중복 처리 및 비정상 전이를 방지.
  - **상태별 트랜잭션 보호**
    - 이미 완료된(COMPLETED) 주문에 대해서는 중복 이벤트 처리 방지.
    - 상태 변경은 트랜잭션 내에서 수행하여 원자성 확보.
- 금액 계산 및 일관성 보장
  - **ticket-reserved 이벤트 기반 총 금액(totalPrice) 계산**
    - ticket-service에서 발행하는 `ticketId`, `price` 정보를 기반으로 주문의 총합 계산.
    - 외부 API 호출(FeignClient) 없이 Kafka Payload만으로 금액 일관성 확보.
  - **주문 생성 시 필수 데이터 검증**
    - ticketIds, totalPrice 등이 유효한 경우에만 주문 진행.
- SAGA기반 주문/결제 완료 플로우
  - **결제 서비스 연동 (Kafka Saga)**
    - `ORDER_CREATED` 발행 → payment-service가 수신 후 결제 시도.
    - 결제 성공 시 `PAYMENT_COMPLETED` 이벤트 수신 → 주문 상태를 `COMPLETED`로 변경.
    - 결제 실패 시 `PAYMENT_COMPLETION_FAILED` 수신 → 주문 상태를 `FAILED`로 변경 및 보상 로직 트리거.
  - **이벤트 기반 동기화 및 상태 반영**
    - 결제 결과에 따라 주문 상태를 실시간으로 반영하고, 알림 연동을 위한 추가 이벤트 발행.
- 보상 트랜잭션 및 실패 복구
  - **보상 트랜잭션 처리**
    - 결제 실패, 주문 생성 실패 등 다양한 장애 상황 발생 시 Kafka 보상 이벤트(`ORDER_CREATION_FAILED`, `ORDER_COMPLETION_FAILED`) 발행.
    - 티켓 반환, 주문 취소, 잔여 좌석 복구 등 후속 보상 트랜잭션을 자동 수행.
  - **이벤트 실패 대비 DLT 준비 계획**
    - 향후 Kafka Dead Letter Topic(DLT) 적용하여 실패 이벤트 이관 및 재처리 체계 구축 예정.

</div>
</details>



<details>
<summary>결제 (Payment)</summary>
<div markdown="1">

- 비동기 결제 요청 및 처리
  - **Kafka 기반 비동기 결제 요청**
    - order-service가 발행한 `ORDER_CREATED` 이벤트를 구독하여 결제 시도 시작
    - 결제 로직을 비동기로 수행하고, 빠른 응답성과 확장성 확보
  - **Mock 결제 흐름 (백엔드용)**
    - 테스트 및 내부 백엔드 흐름에서는 실제 결제 없이 Mock 결제 처리
    - `PaymentEventProcessor`를 통해 주문 금액, 티켓 수량 기반 모킹 성공/실패 처리
  
- Toss Payment API 연동 (프론트 연동용)
  - **Toss API 실결제 흐름 연동**
    - 프론트단 Checkout 결제 성공 후, 백엔드 `PaymentConfirmController`를 통해 Toss API에 최종 결제 승인(confirm) 요청.
    - 승인 성공 시 결제 완료 처리, 실패 시 적절한 오류 처리 및 복구.
  - **프론트-백엔드 연동 구조**
    - 프론트 Success URL → 백엔드 Confirm API → Toss 승인 → 결제 성공 이벤트 발행
  - **TossPayment 엔티티 저장**
    - Toss 결제 성공/실패 데이터는 TossPayment 테이블에 저장하여 기록 관리
  - 프론트 흐름과 백엔드 흐름이 완벽히 독립/병행 운영
  
- 결제 상태 관리 및 일관성 확보
  - **Kafka 이벤트 기반 결제 완료 처리**
    - 결제 성공 시 `PAYMENT_COMPLETED` 이벤트 발행
    - 결제 실패 시 `PAYMENT_COMPLETION_FAILED` 이벤트 발행하여 주문 서비스에 상태 반영 요청
  
- 보상 트랜잭션 처리
  - **결제 실패 보상 흐름 구축**
  	- Mock 결제 실패, Toss API 승인 실패 등 다양한 실패 케이스 대응
	- 결제 실패 시 Kafka를 통해 주문 서비스에 주문 취소/보상 트랜잭션 트리거
	- ticket-service에도 필요한 경우 좌석 반환 요청

- Kafka 이벤트 발행 및 구독  
  - **Kafka 발행**
    - `payment-completed-topic` : 결제 성공 시 발행
    - `payment-completion-failed-topic` : 결제 실패 시 발행
    - `payment-cancelled-topic` : 결제 취소 시 발행
  - **Kafka 구독**
    - `order-created-topic` : 주문 생성 수신 후 결제 시도
    - `order-cancelled-topic` : 주문 취소 시 결제 취소 처리
  - **Kafka 멱등성 처리**
    - Redis 기반 멱등성 체크 적용하여 중복 메시지 소비 방지.
  
- 결제 취소 기능
  - **사용자 결제 취소 처리**
    - 결제 완료 이후에도 사용자가 요청하면 결제 취소 가능.
    - Toss API의 결제 취소(cancel) API 호출 및 TossPayment 업데이트.
  - **PaymentCancelController 구성**
    - 결제 취소 요청 API 제공 → Toss 결제 취소 → Kafka로 `PAYMENT_CANCELLED` 이벤트 발행.
  결제 완료 이후에도 유연한 주문 취소/환불 가능.
  
</div>
</details>

<details>
<summary>경기장 (Stadium)</summary>
<div markdown="1">

- 좌석 및 경기장 정보 조회 최적화
  - 좌석 섹션 목록 조회
    - Enum 타입 `SeatSection`을 기반으로 좌석 섹션명 리스트를 반환  
    	→ 프론트엔드에서 동적 폼 구성 시 사용 가능     
  	- 불변 데이터 특성상, `@Cacheable("seatSectionsCache")`를 통해 캐싱 처리  
    	→ 애플리케이션 레벨 캐시로 DB 접근 불필요, 응답 속도 향상 
  - 팀명 기반 경기장 조회
	- 다른 도메인(예: 경기 도메인)에서 팀명을 기반으로 경기장 정보 요청 가능
	- `StadiumName.fromTeamName(teamName)` 호출을 통해 `StadiumName` Enum 생성
	    → 도메인 모델 내부 일관성 유지
	- 조회 결과는 `StadiumFeignResponse`로 반환하여 외부 도메인에 전달
	- `@Cacheable("stadiumInfoCache")` 적용
	    → 동일 팀명 요청 시, DB 접근 없이 응답 가능
  - 구역별 좌석 목록 조회 성능 최적화
	- `Seat` 테이블에 복합 인덱스 `idx_seat_stadium_section` 설정
	    → `stadium_id`, `section` 컬럼을 조건으로 하는 조회 쿼리에서 **Index Range Scan** 활용 가능
    - 좌석 단건 조회가 아닌 **리스트 기반 조회**의 성능 병목을 해결함
  
  
- FeignClient를 활용한 서비스 간 통신
  - 경기장 정보 제공 (Stadium → 경기/티켓 도메인)
	- 외부 도메인(경기, 티켓 등)에서 팀명으로 경기장 정보를 요청할 수 있도록 공개된 API 제공
	- 내부적으로 `StadiumName.fromTeamName(teamName)`을 통해 도메인 객체 생성
  - **구역별 좌석 정보 제공 (Stadium → 티켓 도메인)**
	- 티켓 도메인에서, 특정 경기장의 구역별 좌석 정보를 요청
	- 요청 시 전달받은 `stadiumId`와 `section`을 기반으로 좌석 리스트 조회

- 경기장 등록 및 좌석 구성
  - 사용자는 경기장 이름과 좌석 목록을 포함한 정보를 등록 가능
  - 각 좌석은 `Seat.createSeat`를 통해 생성되며, `stadium.addSeat`로 연관 설정
  
- 경기장 정보 조회
  - 특정 경기장 ID 기반으로 상세 조회 가능
  - 전체 경기장 목록을 페이징 처리하여 조회 (커스텀 `PageResponseDto` 사용)
  - 경기장 이름 기준 검색 기능 제공 (`StadiumQueryRepository` 사용)

</div>
</details>

<details>
<summary>경기 (Game)</summary>
<div markdown="1">

- 경기장 정보 검증
  - **FeignClient** 및 **Redis Caching** 을 활용하여 경기장 정보를 검증
    - 초반 Feign 요청 이후 저장되는 Cache 정보를 통해 검증 속도 개선
  - 홈 팀에 맞는 경기장의 정보를 검증하고 가져와 경기 정보 생성

- Kafka를 활용한 이벤트 발행
  - 경기가 생성되고 나면, 경기에 대한 정보를 각각 Alarm, Chat으로 Kafka 이벤트를 통해 발행
    - 초반 Feign 기반 통신에서 Kafka로 전환하였고,
    이후 메시지 유실 방지를 위한 Outbox 테이블 도입 
  
- 예매 대기열 관리
  - Redis
    - Redis Sorted Set 구조를 활용하여 유저의 대기 순서를 정렬하고 관리
    - 사용자 요청 기반으로 대기열 진입 처리
  - WebSocket을 통한 실시간 대기 번호 전송
    - 입장 대기 중인 사용자에게 WebSocket으로 실시간 대기 번호 전송
    - 입장 가능 상태가 되면 통지하여 페이지 전환 유도
  
</div>
</details>

<details>
<summary>이벤트 (Event)</summary>
<div markdown="1">

- 포인트 기반 이벤트 응모 및 관리
  - 사용자는 보유 포인트를 사용하여 특정 이벤트에 응모 가능
  - 이벤트의 검색 및 응모 내역 조회
- Kafka 기반 이벤트 발행
  - 사용자의 이벤트 응모 시, 포인트 차감 이벤트를 발행
  - 포인트 차감 결과에 따라 발생하는 이벤트를 구독하여 보상 트랜잭션 처리
- Quartz를 통한 이벤트 시작/종료 처리
  - DB JobStore 및 동적 트리거를 활용하여 이벤트 기간에 맞춰 이벤트의 시작 및 종료 처리
  - Quartz의 클러스터링 모드를 활용하여 서버의 스케일 아웃에도 안정적인 스케줄 기능
  - 이벤트의 생성 및 업데이트 시 동적으로 Job을 등록하여 각 이벤트에 맞는 동적인 스케줄링 처리
  
</div>
</details>

<details>
<summary>알림 (Alarm)</summary>
<div markdown="1">

- AWS SNS 기반 SMS 발송
  - 알림 전송은 AWS SNS(`software.amazon.awssdk.services.sns.SnsClient`) 기반으로 처리
  - `sendSns(String rawPhoneNumber, String message)` 메서드에서 `PublishRequest`를 구성
  - 발신 성공 시 `messageId` 로깅, 실패 시 `SnsException` 로깅 및 예외 발생 처리
  - 전화번호는 `+82` 형식으로 표준화 (`formatPhoneNumber` 메서드에서 변환 처리)

- 예매 완료 시 알림 전송
  - `order-service`에서 예매 완료 이벤트 발생 시, Kafka 통해 알림 이벤트 수신
        → 알림 서비스는 해당 이벤트를 소비하여 **유저에게 알림 메시지 발송**

  - 수신된 userId에 대해 `UserClient.getPhoneNumber(userId)` 호출로 전화번호 조회
        → `@FeignClient`를 사용한 user-service 연동 구조

  - 전화번호 조회 후 `sendSns()`를 통해 메시지 발송

- 경기 하루 전 알림 발송
  - `GameAlarmSchedule` 테이블에서 **내일 경기면서 아직 전송되지 않은 알림 정보만 조회**
  - 알림 대상 경기 ID마다 Kafka를 통해 알림 요청 이벤트 발행
  - 예약 스케줄링은 Spring Scheduler 사용

- 이벤트 당첨자 알림 전송 
  - 알람 서비스는 `EventWinnerAnnouncedConsumer`를 통해 해당 이벤트를 구독
  - 수신된 메시지에서 `winnerIds` 리스트를 순회하며 FeignClient를 통해 `user-service`에서 각 사용자 전화번호 조회 후  AWS SNS를 통해 당첨 축하 메시지 전송
  
</div>
</details>


<details>
<summary>채팅 (Chat)</summary>
<div markdown="1">

- WebSocket 기반 실시간 채팅 서버 구현
  - WebSocket 실시간 채팅 서버
    - Spring WebSocketHandler를 상속하여 사용자 간 채팅을 실시간으로 처리
    - 채팅방은 경기(gameId) 기준으로 나뉘며, 각 경기별로 분리된 채팅 세션 유지

  - Kafka 기반 메시지 브로커 도입
    - 기존의 WebSocket 내 직접 처리 구조에서 Kafka로 메시지 송수신 구조 변경
    - 메시지를 Kafka에 발행하고, Consumer를 통해 브로드캐스트 처리

  - 사용자 식별 및 닉네임 처리
    - 사용자 ID 대신 닉네임 기반으로 채팅 메시지를 표시
    - 서버 메시지는 별도 타입으로 처리하여 구분 가능

</div>
</details>

<br>

## 기능 로직
<details>
<summary>경기 생성 로직</summary>
<div markdown="1">

![image (8)](https://github.com/user-attachments/assets/db58ee88-7190-4174-b563-a6ada2752b88)

</div>
</details>

<details>
<summary>티켓 예매 로직</summary>
<div markdown="1">

![image_(6)](https://github.com/user-attachments/assets/0176dc13-0bba-4d32-a224-b0fed2d3b5e8)

</div>
</details>


<details>
<summary>이벤트 참여 로직</summary>
<div markdown="1">
	
![image_(7)](https://github.com/user-attachments/assets/dc93e99d-6c68-484a-ae39-4cfd6b8e03ef)

</div>
</details>

### 



<br>


## 적용 기술과 기술적 의사결정

◻️ MSA
> - 유지 보수, 분업 효율, 확장성 경험을 위해 MSA 구조를 선택했습니다

◻️ QueryDSL
> - 정렬, 검색어 등에 따른 동적 쿼리 작성을 위하여 QueryDSL를 도입하여 활용했습니다.

◻️ Quartz
> - 정해진 시간이나 주기마다 작업을 안정적으로 실행하기 위해 Quartz 라이브러리를 도입했습니다.

◻️ AWS SNS
> - 클라우드 네이티브 아키텍처와의 자연스러운 통합
>  - AWS SNS는 다른 AWS 서비스 (Lambda, SQS, EventBridge 등)와의 통합성이 뛰어나 MSA 구조에 적합하며, 비동기 이벤트 기반 메시징을 유연하게 처리 가능.
> - 복수 채널(이메일, SMS, HTTP 등) 지원으로 확장 용이
>  - AWS SNS는 단순 SMS뿐만 아니라 Email, HTTPS 엔드포인트, Lambda 등 다양한 전송 채널을 지원하므로,
>추후 카카오톡 알림톡, 앱 푸시, 이메일 통지 등 다양한 다중 채널 알림 시스템으로의 확장이 쉬움.
>  - 전 세계 인프라를 활용한 안정성 및 확장성
>   - AWS는 99.999% 이상의 고가용성을 보장하는 글로벌 인프라를 통해 안정적인 메시지 발송 보장.
> 트래픽 증가 시에도 Auto Scaling 및 다중 리전 활용이 가능하므로, 트래픽 폭증 시에도 지연 없이 대응 가능.

◻️ Kafka
> - 마이크로서비스 간 비동기 메시지 처리와 안정적인 이벤트 라우팅을 위해 Kafka를 활용했습니다.

◻️ PostgreSQL
> - 관계형 데이터의 정합성과 복잡한 쿼리 처리를 위해 PostgreSQL을 도입했습니다.

◻️ Redis
> - 연속된 요청으로 인한 DB병목을 해소하고 RefreshToken 등 소멸기간이 존재하는 데이터의 TimeToLive 관리와 분산 락, 캐싱을 용이하게 할 수 있도록 Redis를 도입하였습니다.

◻️ MongoDB
> - 유연한 스키마와 대용량 비정형 데이터 처리를 위해 MongoDB를 도입했습니다.


<br>

## 개발 환경

`Java 17` `Spring Boot 3.4.3` `QueryDSL 5.0.0` `Spring Cloud 2024.0.0`

<br>

## 트러블 슈팅

#### 주문 상태 전이 및 금액 계산 구조 문제 [→ WIKI 보기](https://github.com/FINAL-SPARTA/SPARTA-FINAL-PROJECT/wiki/%5BTrouble-Shooting%5D-%EC%A3%BC%EB%AC%B8-%EC%83%81%ED%83%9C-%EC%A0%84%EC%9D%B4-%EB%B0%8F-%EA%B8%88%EC%95%A1-%EA%B3%84%EC%82%B0-%EA%B5%AC%EC%A1%B0-%EB%AC%B8%EC%A0%9C)
#### 티켓 예매 동시성 문제 [→ WIKI 보기](https://github.com/FINAL-SPARTA/SPARTA-FINAL-PROJECT/wiki/%5BTrouble-Shooting%5D-%ED%8B%B0%EC%BC%93-%EC%98%88%EB%A7%A4-%EB%8F%99%EC%8B%9C%EC%84%B1-%EB%AC%B8%EC%A0%9C)
#### Payment TossPay 연결 [→ WIKI 보기](https://github.com/FINAL-SPARTA/SPARTA-FINAL-PROJECT/wiki/%5BTrouble-Shooting%5D-Payment-TossPay-%EC%97%B0%EA%B2%B0)
#### Kafka 직렬화 / 역직렬화 문제 [→ WIKI 보기](https://github.com/FINAL-SPARTA/SPARTA-FINAL-PROJECT/wiki/%5BTrouble-Shooting%5D-Kafka-%EC%A7%81%EB%A0%AC%ED%99%94---%EC%97%AD%EC%A7%81%EB%A0%AC%ED%99%94-%EB%AC%B8%EC%A0%9C)
#### 티켓 예매 로직 개선 [→ WIKI 보기](https://github.com/FINAL-SPARTA/SPARTA-FINAL-PROJECT/wiki/%5BTrouble-Shooting%5D-%ED%8B%B0%EC%BC%93-%EC%98%88%EB%A7%A4-%EB%A1%9C%EC%A7%81-%EA%B0%9C%EC%84%A0)
#### Saga Choreography Pattern 구현 [→ WIKI 보기](https://github.com/FINAL-SPARTA/SPARTA-FINAL-PROJECT/wiki/%5BTrouble-Shooting%5D-Saga-Choreography-Pattern-%EA%B5%AC%ED%98%84)
#### 스케줄러 기반 만료 티켓 정리의 비효율성 개선 [→ WIKI 보기](https://github.com/FINAL-SPARTA/SPARTA-FINAL-PROJECT/wiki/%5BTrouble-Shooting%5D-%EC%8A%A4%EC%BC%80%EC%A4%84%EB%9F%AC-%EA%B8%B0%EB%B0%98-%EB%A7%8C%EB%A3%8C-%ED%8B%B0%EC%BC%93-%EC%A0%95%EB%A6%AC%EC%9D%98-%EB%B9%84%ED%9A%A8%EC%9C%A8%EC%84%B1-%EA%B0%9C%EC%84%A0)
#### 분산 환경에서의 이벤트 상태 변경 스케줄링 정확성 및 안정성 확보 [→ WIKI 보기](https://github.com/FINAL-SPARTA/SPARTA-FINAL-PROJECT/wiki/%5BTrouble-Shooting%5D-%EB%B6%84%EC%82%B0-%ED%99%98%EA%B2%BD%EC%97%90%EC%84%9C%EC%9D%98-%EC%9D%B4%EB%B2%A4%ED%8A%B8-%EC%83%81%ED%83%9C-%EB%B3%80%EA%B2%BD-%EC%8A%A4%EC%BC%80%EC%A4%84%EB%A7%81-%EC%A0%95%ED%99%95%EC%84%B1-%EB%B0%8F-%EC%95%88%EC%A0%95%EC%84%B1-%ED%99%95%EB%B3%B4)
#### WebSocket을 활용한 채팅 기능 개선 [→ WIKI 보기](https://github.com/FINAL-SPARTA/SPARTA-FINAL-PROJECT/wiki/%5BTrouble-Shooting%5D-WebSocket%EC%9D%84-%ED%99%9C%EC%9A%A9%ED%95%9C-%EC%B1%84%ED%8C%85-%EA%B8%B0%EB%8A%A5-%EA%B0%9C%EC%84%A0)
#### 경기 생성 로직 수정 [→ WIKI 보기](https://github.com/FINAL-SPARTA/SPARTA-FINAL-PROJECT/wiki/%5BTrouble-Shooting%5D-%EA%B2%BD%EA%B8%B0-%EC%83%9D%EC%84%B1-%EB%A1%9C%EC%A7%81-%EC%88%98%EC%A0%95)
#### Kafka 이벤트 요청-응답 설계 고민: Alarm → Order 서비스 [→ WIKI 보기](https://github.com/FINAL-SPARTA/SPARTA-FINAL-PROJECT/wiki/%5BTrouble-Shooting%5D-Kafka-%EC%9D%B4%EB%B2%A4%ED%8A%B8-%EC%9A%94%EC%B2%AD%E2%80%90%EC%9D%91%EB%8B%B5-%EC%84%A4%EA%B3%84-%EA%B3%A0%EB%AF%BC:-Alarm-%E2%86%92-Order-%EC%84%9C%EB%B9%84%EC%8A%A4)
#### saveAll() 사용 시 타입 소거로 인한 DIP 설계 원칙 충돌 해결 [→ WIKI 보기](https://github.com/FINAL-SPARTA/SPARTA-FINAL-PROJECT/wiki/%5BTrouble-Shooting%5D-saveAll()-%EC%82%AC%EC%9A%A9-%EC%8B%9C-%ED%83%80%EC%9E%85-%EC%86%8C%EA%B1%B0%EB%A1%9C-%EC%9D%B8%ED%95%9C-DIP-%EC%84%A4%EA%B3%84-%EC%9B%90%EC%B9%99-%EC%B6%A9%EB%8F%8C-%ED%95%B4%EA%B2%B0)
#### 티켓 예매 기능의 처리량 개선 [→ WIKI 보기](https://github.com/FINAL-SPARTA/SPARTA-FINAL-PROJECT/wiki/%5BTrouble-Shooting%5D-%ED%8B%B0%EC%BC%93-%EC%98%88%EB%A7%A4-%EA%B8%B0%EB%8A%A5%EC%9D%98-%EC%B2%98%EB%A6%AC%EB%9F%89-%EA%B0%9C%EC%84%A0)



<br>

## 아키텍처 설계도

### 시스템 아키텍처 설계

![](https://velog.velcdn.com/images/azuressu/post/73b640c4-51fa-4b2e-aa6c-282418588243/image.png)

### MSA 설계

![](https://velog.velcdn.com/images/azuressu/post/b0f39e4b-5613-4210-90bb-86821212641d/image.png)



<br>

## API 명세서

[⇒ API 명세서](https://teamsparta.notion.site/API-1e52dc3ef5148083ab23da78bec2f14a)

<br>

## ERD 다이어그램 & 테이블 명세서
![](https://velog.velcdn.com/images/azuressu/post/0d7b1f26-f221-4aaf-95f2-c1354b4b2fd0/image.png)

[⇒ 테이블 명세서](https://teamsparta.notion.site/1e52dc3ef51480f5968fe81fc25d078b)

<br>

## 기술과 도구

<div style="display: flex; justify-content: center;">
  <img src="https://img.shields.io/badge/Java-007396?&style=flat&logo=java&logoColor=white" style="margin-right: 10px;">
  <img src="https://img.shields.io/badge/Spring Boot-6DB33F?&style=flat&logo=springboot&logoColor=white" style="margin-right: 10px;">
 <img src="https://img.shields.io/badge/Spring Security-6DB33F?&style=flat&logo=springsecurity&logoColor=white" style="margin-right: 10px;">
    <img src="https://img.shields.io/badge/ApachetTomcat-F8DC75?style=flat&logo=apachetomcat&logoColor=white"style="margin-right: 10px;"/>
    <img src="https://img.shields.io/badge/Json Web Tokens-000000?style=flat&logo=jsonwebtokens&logoColor=white"style="margin-right: 10px;"/>
</div>
  
<div style="display: flex; justify-content: center;">
    <img src="https://img.shields.io/badge/Gradle-02303A?style=flat&logo=gradle&logoColor=white" style="margin-right: 10px;"/>
    <img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=flat&logo=Postgresql&logoColor=white" style="margin-right: 10px;"/>
    <img src="https://img.shields.io/badge/Redis-FF4438?style=flat&logo=redis&logoColor=white" style="margin-right: 10px;"/>
    <img src="https://img.shields.io/badge/MongoDB-47A248?style=flat&logo=mongodb&logoColor=white" style="margin-right: 10px;"/>
    <img src="https://img.shields.io/badge/Apache%20Kafka-231F20?style=flat&logo=apachekafka&logoColor=white" style="margin-right: 10px;"/>
</div>


<div style="display: flex; justify-content: center;">
    <img src="https://img.shields.io/badge/Elasticsearch-005571?style=flat&logo=elasticsearch&logoColor=white" style="margin-right: 10px;"/>
    <img src="https://img.shields.io/badge/Logstash-005571?style=flat&logo=logstash&logoColor=white" style="margin-right: 10px;"/>
    <img src="https://img.shields.io/badge/Kibana-005571?style=flat&logo=kibana&logoColor=white" style="margin-right: 10px;"/>
    <img src="https://img.shields.io/badge/Prometheus-E6522C?style=flat&logo=prometheus&logoColor=white" style="margin-right: 10px;"/>
    <img src="https://img.shields.io/badge/Fluentd-0E83C8?style=flat&logo=fluentd&logoColor=white" style="margin-right: 10px;"/>
</div>
  
  
  
<div style="display: flex; justify-content: center;"> 
    <img src="https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white" style="margin-right: 10px;">
    <img src="https://img.shields.io/badge/GibHub%20Actions-2088FF?style=flat&logo=githubactions&logoColor=white" style="margin-right: 10px;">
    <img src="https://img.shields.io/badge/Amazon%20EC2-FF9900?style=flat&logo=amazonec2&logoColor=white" style="margin-right: 10px;">
    <img src="https://img.shields.io/badge/Amazon%20ECS-FF9900?style=flat&logo=amazonecs&logoColor=white" style="margin-right: 10px;">
    <img src="https://img.shields.io/badge/Amazon%20RDS-527FFF?style=flat&logo=amazonrds&logoColor=white" style="margin-right: 10px;">
    <img src="https://img.shields.io/badge/Git-F05032?style=flat&logo=git&logoColor=white" style="margin-right: 10px;">
    <img src="https://img.shields.io/badge/Github-181717?style=flat&logo=github&logoColor=white" style="margin-right: 10px;">
    
</div>
  
  
<div style="display: flex; justify-content: center;">  
  <img src="https://img.shields.io/badge/IntelliJ Idea-000000?style=flat&logo=intellijidea&logoColor=white" style="margin-right: 10px;">
  <img src="https://img.shields.io/badge/Postman-FF6C37?style=flat&logo=postman&logoColor=white" style="margin-right: 10px;">
  <img src="https://img.shields.io/badge/Jira Software-0052CC?style=flat&logo=jirasoftware&logoColor=white" style="margin-right: 10px;">
  <img src="https://img.shields.io/badge/Notion-000000?style=flat&logo=notion&logoColor=white" style="margin-right: 10px;">
  <img src="https://img.shields.io/badge/Slack-4A154B?style=flat&logo=slack&logoColor=white" style="margin-right: 10px;">

</div>

<br>
