package br.com.fiap.reviewservice.service;

import br.com.fiap.reviewservice.consumer.ReviewConsumer;
import br.com.fiap.reviewservice.model.ReviewSummary;
import br.com.fiap.reviewservice.repository.ReviewSummaryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReviewBatchService {

    private final ReviewConsumer reviewConsumer;
    private final ReviewSummaryRepository repository;

    public ReviewBatchService(
            ReviewConsumer reviewConsumer,
            ReviewSummaryRepository repository
    ) {
        this.reviewConsumer = reviewConsumer;
        this.repository = repository;
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processReviews() {

        Map<Long, ReviewConsumer.ReviewBuffer> batch = new HashMap<>();

        reviewConsumer.getBuffer().forEach((dishId, ignored) -> {
            ReviewConsumer.ReviewBuffer removed =
                    reviewConsumer.getBuffer().remove(dishId);

            if (removed != null) {
                batch.put(dishId, removed);
            }
        });

        batch.forEach((dishId, data) -> {

            ReviewSummary summary = repository.findById(dishId)
                    .orElseGet(() -> new ReviewSummary(
                            dishId,
                            data.dishName(),
                            0L,
                            0L
                    ));

            summary.setCount(summary.getCount() + data.count());
            summary.setSumRatings(
                    summary.getSumRatings() + data.sumRatings()
            );

            repository.save(summary);
        });
    }
}