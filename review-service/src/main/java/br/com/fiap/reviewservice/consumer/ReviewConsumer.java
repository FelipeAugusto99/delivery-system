package br.com.fiap.reviewservice.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReviewConsumer {

    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<Long, ReviewBuffer> buffer =
            new ConcurrentHashMap<>();

    public ReviewConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "reviews.queue")
    public void receiveReview(String message) throws Exception {

        JsonNode review = objectMapper.readTree(message);

        Long dishId = review.get("dishId").asLong();
        String dishName = review.get("dishName").asText();
        int rating = review.get("rating").asInt();

        buffer.compute(dishId, (id, current) -> {
            if (current == null) {
                return new ReviewBuffer(dishName, rating, 1);
            }

            return new ReviewBuffer(
                    dishName,
                    current.sumRatings() + rating,
                    current.count() + 1
            );
        });
    }

    public ConcurrentHashMap<Long, ReviewBuffer> getBuffer() {
        return buffer;
    }

    public record ReviewBuffer(
            String dishName,
            long sumRatings,
            long count
    ) {
    }
}