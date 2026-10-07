package br.com.fiap.paymentservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final Random random = new Random();

    @Value("${server.port}")
    private int serverPort;

    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody Map<String, BigDecimal> request) {

        if (random.nextBoolean()) {
            System.out.println("Payment failed - instance: " + serverPort);
            return ResponseEntity.internalServerError().build();
        }

        System.out.println("Payment approved - instance: " + serverPort);

        return ResponseEntity.ok(
                Map.of(
                        "status", "APPROVED",
                        "instance", serverPort
                )
        );
    }
}