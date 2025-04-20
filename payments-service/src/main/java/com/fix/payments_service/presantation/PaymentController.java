package com.fix.payments_service.presantation;

import com.fix.payments_service.application.PaymentEventService;
import com.fix.payments_service.domain.TossPaymentFailure;
import com.fix.payments_service.domain.repository.TossPaymentFailureRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class PaymentController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${toss.secret-key}")
    private String apiSecretKey;
    private final Map<String, String> billingKeyMap = new HashMap<>();

    private final PaymentEventService paymentEventService;
    private final TossPaymentFailureRepository tossPaymentFailureRepository;

    public PaymentController(PaymentEventService paymentEventService,
                             TossPaymentFailureRepository tossPaymentFailureRepository) {
        this.paymentEventService = paymentEventService;
        this.tossPaymentFailureRepository = tossPaymentFailureRepository;
    }

    /**
     * 💳 빌링 결제 확정 메서드
     * Toss billingKey를 통해 결제 승인 요청을 보내고,
     * 성공 시 Kafka로 결제 완료 이벤트 발행,
     * 실패 시 실패 이벤트 발행 및 실패 내역 저장
     */
    @RequestMapping(value = "/confirm-billing")
    public ResponseEntity<JSONObject> confirmBilling(@RequestBody String jsonBody) throws Exception {
        JSONObject requestData = parseRequestData(jsonBody);
        Object customerKeyObj = requestData.get("customerKey");
        if (customerKeyObj == null) {
            logger.warn("❗ customerKey 누락됨");
            JSONObject error = new JSONObject();
            error.put("error", "Missing customerKey");
            return ResponseEntity.badRequest().body(error);
        }

        String customerKey = customerKeyObj.toString();
        String billingKey = billingKeyMap.get(customerKey);
        JSONObject response = sendRequest(requestData, apiSecretKey, "https://api.tosspayments.com/v1/billing/" + billingKey);

        String orderId = (String) requestData.get("orderId");

        if (response.containsKey("error")) {
            String errorMsg = (String) response.get("message");
            paymentEventService.sendPaymentCompletionFailed(UUID.fromString(orderId), errorMsg);

            TossPaymentFailure failure = TossPaymentFailure.builder()
                    .orderId(orderId)
                    .paymentKey(billingKey)
                    .errorCode("BILLING_CONFIRM_FAILED")
                    .errorMessage(errorMsg)
                    .build();
            tossPaymentFailureRepository.save(failure);
        } else {
            paymentEventService.sendPaymentCompleted(UUID.fromString(orderId));
        }

        return ResponseEntity.status(response.containsKey("error") ? 400 : 200).body(response);
    }
    /**
     * 🧾 빌링 키 발급 요청 메서드
     * Toss에 카드 정보를 전달해 billingKey를 발급받고, 메모리 캐시에 저장
     */
    @RequestMapping(value = "/issue-billing-key")
    public ResponseEntity<JSONObject> issueBillingKey(@RequestBody String jsonBody) throws Exception {
        JSONObject requestData = parseRequestData(jsonBody);
        JSONObject response = sendRequest(requestData, apiSecretKey, "https://api.tosspayments.com/v1/billing/authorizations/issue");

        if (!response.containsKey("error")) {
            billingKeyMap.put((String) requestData.get("customerKey"), (String) response.get("billingKey"));
        }

        return ResponseEntity.status(response.containsKey("error") ? 400 : 200).body(response);
    }

    /**
     * 🔐 BrandPay 인증 성공 콜백 메서드
     * 인증 성공 후 code를 전달받아 Toss에서 access-token을 발급받음
     */
    @RequestMapping(value = "/callback-auth", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> callbackAuth(@RequestParam String customerKey, @RequestParam String code) throws Exception {
        JSONObject requestData = new JSONObject();
        requestData.put("grantType", "AuthorizationCode");
        requestData.put("customerKey", customerKey);
        requestData.put("code", code);

        String url = "https://api.tosspayments.com/v1/brandpay/authorizations/access-token";
        JSONObject response = sendRequest(requestData, apiSecretKey, url);

        logger.info("Response Data: {}", response);

        return ResponseEntity.status(response.containsKey("error") ? 400 : 200).body(response);
    }

    /**
     * 💰 BrandPay 결제 확정 메서드
     * Toss BrandPay를 통한 결제 승인 요청을 처리하고,
     * 성공 시 Kafka로 결제 완료 이벤트 발행,
     * 실패 시 실패 이벤트 발행 및 실패 내역 저장
     */
    @RequestMapping(value = "/confirm/brandpay", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<JSONObject> confirmBrandpay(@RequestBody String jsonBody) throws Exception {
        JSONObject requestData = parseRequestData(jsonBody);
        String url = "https://api.tosspayments.com/v1/brandpay/payments/confirm";
        JSONObject response = sendRequest(requestData, apiSecretKey, url);
        String orderId = (String) requestData.get("orderId");
        String paymentKey = (String) requestData.get("paymentKey");

        if (response.containsKey("error")) {
            String errorMsg = (String) response.get("message");
            paymentEventService.sendPaymentCompletionFailed(UUID.fromString(orderId), errorMsg);

            TossPaymentFailure failure = TossPaymentFailure.builder()
                    .orderId(orderId)
                    .paymentKey(paymentKey)
                    .errorCode("BRANDPAY_CONFIRM_FAILED")
                    .errorMessage(errorMsg)
                    .build();
            tossPaymentFailureRepository.save(failure);
        } else {
            paymentEventService.sendPaymentCompleted(UUID.fromString(orderId));
        }

        return ResponseEntity.status(response.containsKey("error") ? 400 : 200).body(response);
    }


//    클라이언트에서 받은 JSON 문자열을 JSONObject로 파싱
    private JSONObject parseRequestData(String jsonBody) {
        try {
            return (JSONObject) new JSONParser().parse(jsonBody);
        } catch (ParseException e) {
            logger.error("JSON Parsing Error", e);
            return new JSONObject();
        }
    }

//    HTTP POST 방식으로 외부 API(Toss 등)에 요청을 보내고 응답을 JSON 형태로 받아 반환
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

//    외부 API 서버와 연결을 설정
    private HttpURLConnection createConnection(String secretKey, String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        return connection;
    }

//  위젯 결제 진입 페이지 렌더링
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "/widget/checkout";
    }

//  ❌ 결제 실패 페이지 렌더링
    @RequestMapping(value = "/fail", method = RequestMethod.GET)
    public String failPayment(HttpServletRequest request, Model model) {
        model.addAttribute("code", request.getParameter("code"));
        model.addAttribute("message", request.getParameter("message"));
        return "/fail";
    }
}
