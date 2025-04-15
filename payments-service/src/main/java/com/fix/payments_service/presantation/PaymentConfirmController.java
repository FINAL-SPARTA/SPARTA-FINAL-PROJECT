package com.fix.payments_service.presantation;

import com.fix.payments_service.domain.TossPayment;
import com.fix.payments_service.domain.repository.TossPaymentRepository;
import com.fix.payments_service.infrastructure.client.OrderServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fix.payments_service.domain.TossPaymentMethod;
import com.fix.payments_service.domain.TossPaymentStatus;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Base64;

@RestController
@RequiredArgsConstructor
@RequestMapping("/confirm")
public class PaymentConfirmController {


    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final OrderServiceClient orderServiceClient;
    private final TossPaymentRepository tossPaymentRepository;

    // ✅ application.yml 설정 기반으로 주입받음
    @Value("${toss.widget-secret-key}")
    private String widgetSecretKey;

    @Value("${toss.api-secret-key}")
    private String apiSecretKey;

    // ✅ 기존 PaymentController에서 confirmPayment 메서드 분리
    @PostMapping(value = {"/widget", "/payment"}, consumes = "application/json")
    public ResponseEntity<JSONObject> confirmPayment(HttpServletRequest request, @RequestBody String jsonBody) throws Exception {
        logger.info("토스 결제 승인 요청 시작");

        // ✅ /confirm/payment → API_SECRET_KEY, /confirm/widget → WIDGET_SECRET_KEY
        String secretKey = request.getRequestURI().contains("/confirm/payment") ? apiSecretKey : widgetSecretKey;

        // ✅ 요청 파싱
        JSONObject requestData = parseRequestData(jsonBody);
        // ✅ Toss 승인 요청
        JSONObject response = sendRequest(requestData, secretKey, "https://api.tosspayments.com/v1/payments/confirm");
        int statusCode = response.containsKey("error") ? 400 : 200;

        // ✅ Toss 결제 승인 성공 시 로직
        if (!response.containsKey("error")) {
            String orderId = (String) response.get("orderId");
            TossPaymentMethod method = TossPaymentMethod.valueOf(((String) response.get("method")).toUpperCase());
            TossPaymentStatus status = TossPaymentStatus.valueOf(((String) response.get("status")).toUpperCase());
            // ✅  Toss 결제 내역 저장
            TossPayment payment = TossPayment.builder()
                    .paymentKey((String) response.get("paymentKey"))
                    .orderId(orderId)
                    .amount(Integer.parseInt(String.valueOf(response.get("amount"))))
                    .method(method)
                    .status(status)
                    .approvedAt(ZonedDateTime.parse((String) response.get("approvedAt")))
                    .cardCompany(extract(response, "card", "company"))
                    .cardNumber(extract(response, "card", "number"))
                    .receiptUrl(extract(response, "receipt", "url"))
                    .build();

            tossPaymentRepository.save(payment);

            try {
                orderServiceClient.completeOrder(orderId);
                logger.info("주문 상태 COMPLETED 처리 성공: {}", orderId);
            } catch (Exception e) {
                logger.error("주문 상태 변경 실패: {}", e.getMessage());
            }
        }

        return ResponseEntity.status(statusCode).body(response);
    }

    @SuppressWarnings("unchecked")
    private String extract(JSONObject root, String section, String field) {
        JSONObject nested = (JSONObject) root.get(section);
        return nested != null ? (String) nested.get(field) : null;
    }

    // PaymentController에서 가져온 유틸 메서드
    private JSONObject parseRequestData(String jsonBody) {
        try {
            return (JSONObject) new JSONParser().parse(jsonBody);
        } catch (ParseException e) {
            logger.error("JSON Parsing Error", e);
            return new JSONObject();
        }
    }

    // PaymentController에서 가져온 유틸 메서드
    private JSONObject sendRequest(JSONObject requestData, String secretKey, String urlString) throws IOException {
        HttpURLConnection connection = createConnection(secretKey, urlString);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestData.toString().getBytes(StandardCharsets.UTF_8));
        }

        try (InputStream responseStream = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
             Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
            return (JSONObject) new JSONParser().parse(reader);
        } catch (Exception e) {
            logger.error("Error reading response", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "Error reading response");
            return errorResponse;
        }
    }

    // PaymentController에서 가져온 유틸 메서드
    private HttpURLConnection createConnection(String secretKey, String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        return connection;
    }
}
