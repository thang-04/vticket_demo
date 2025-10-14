package com.vticket.vticket.service;

import com.vticket.vticket.domain.mysql.entity.Booking;
import com.vticket.vticket.dto.request.MomoCreationRequest;
import com.vticket.vticket.dto.response.MomoCreationResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Service
public class MomoService {

    private static final Logger logger = LogManager.getLogger(MomoService.class);

    @Value("${momo.endpoint}")
    private String MOMO_ENDPOINT;
    @Value("${momo.accessKey}")
    private String ACCESS_KEY;
    @Value("${momo.secretKey}")
    private String SECRET_KEY;
    @Value("${momo.partnerCode}")
    private String PARTNER_CODE;
    @Value("${momo.redirectUrl}")
    private String REDIRECT_URL;
    @Value("${momo.requestType}")
    private String REQUEST_TYPE;
    @Value("${momo.ipnUrl}")
    private String IPN_URL;

    private final WebClient webClient = WebClient.builder().build();

    public MomoCreationResponse createQR(Booking booking) {
        long start = System.currentTimeMillis();
        logger.info("Creating MoMo QR for booking: {}", booking);

        try {
            String requestId = booking.getBookingCode();
            String orderId = String.valueOf(booking.getId());
            String amount = String.valueOf(booking.getTotalAmount());
            String extraData = (booking.getDiscountCode() != null && !booking.getDiscountCode().isEmpty())
                    ? booking.getDiscountCode()
                    : "";
            String parnerCode = PARTNER_CODE+System.currentTimeMillis();

            String orderInfo = "Thanh toan don hang Vticket";

            String rawSignature = "accessKey=" + ACCESS_KEY +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + IPN_URL +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + parnerCode +
                    "&redirectUrl=" + REDIRECT_URL +
                    "&requestId=" + requestId +
                    "&requestType=" + REQUEST_TYPE;

            logger.info("Raw signature: {}", rawSignature);
            String signature = hmacSHA256(rawSignature, SECRET_KEY);

            MomoCreationRequest momoRequest = MomoCreationRequest.builder()
                    .partnerCode(parnerCode)
                    .redirectUrl(REDIRECT_URL)
                    .ipnUrl(IPN_URL)
                    .requestType(REQUEST_TYPE)
                    .amount(amount)
                    .extraData(extraData)
                    .orderId(orderId)
                    .orderInfo(orderInfo)
                    .requestId(requestId)
                    .lang("vi")
                    .signature(signature)
                    .build();

            logger.info("Sending MoMo request: {}", momoRequest);

            // call MoMo API
            MomoCreationResponse response = webClient.post()
                    .uri(MOMO_ENDPOINT)
                    .header("Content-Type", "application/json")
                    .bodyValue(momoRequest)
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .doOnNext(body -> logger.error("Error response from MoMo: {}", body))
                                    .flatMap(body -> Mono.error(new RuntimeException("MoMo error: " + body)))
                    )
                    .bodyToMono(MomoCreationResponse.class)
                    .block();

            logger.info("Received MoMo response: {} in {} ms", response, (System.currentTimeMillis() - start));
            return response;
        } catch (Exception e) {
            logger.error("Error while creating MoMo QR: {}", e.getMessage(), e);
            return null;
        }
    }

    private static String hmacSHA256(String data, String secretKey) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return HexFormat.of().formatHex(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}
