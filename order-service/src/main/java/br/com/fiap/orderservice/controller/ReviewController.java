package br.com.fiap.orderservice.controller;

import br.com.fiap.orderservice.dto.ReviewRequest;
import br.com.fiap.orderservice.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request) {
        try {
            reviewService.publishReview(request);

            return ResponseEntity.accepted().build();

        } catch (ReviewService.InvalidRatingException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid rating"));

        } catch (ReviewService.DishNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Dish not found"));
        }
    }
}