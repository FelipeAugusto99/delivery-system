package br.com.fiap.orderservice.client;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class PaymentClient {

    private final RestTemplate restTemplate;

    public PaymentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable(
            maxAttemptsExpression = "${payment.retry.max-retries}",
            backoff = @Backoff(
                    delayExpression = "${payment.retry.delay}",
                    multiplierExpression = "${payment.retry.multiplier}",
                    maxDelayExpression = "${payment.retry.max-delay}",
                    random = true
            )
    )
    public void processPayment(BigDecimal amount) {
        restTemplate.postForEntity(
                "http://PAYMENT-SERVICE/payments",
                Map.of("amount", amount),
                Map.class
        );
    }
}