package br.com.fiap.orderservice.service;

import br.com.fiap.orderservice.config.RabbitMQConfig;
import br.com.fiap.orderservice.dto.ReviewRequest;
import br.com.fiap.orderservice.model.Dish;
import br.com.fiap.orderservice.repository.DishRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ReviewService {

    private final DishRepository dishRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public ReviewService(
            DishRepository dishRepository,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper
    ) {
        this.dishRepository = dishRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishReview(ReviewRequest request) {

        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new InvalidRatingException();
        }

        Dish dish = dishRepository.findById(request.getDishId())
                .orElseThrow(DishNotFoundException::new);

        try {
            String message = objectMapper.writeValueAsString(
                    Map.of(
                            "dishId", dish.getId(),
                            "dishName", dish.getName(),
                            "rating", request.getRating(),
                            "comment", request.getComment()
                    )
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY,
                    message
            );

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize review", e);
        }
    }

    public static class InvalidRatingException extends RuntimeException {
    }

    public static class DishNotFoundException extends RuntimeException {
    }
}