package br.com.fiap.reviewservice.service;

import br.com.fiap.reviewservice.consumer.ReviewConsumer;
import br.com.fiap.reviewservice.model.ReviewSummary;
import br.com.fiap.reviewservice.repository.ReviewSummaryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

@Service
public class ReviewBatchService {

    private final ReviewConsumer reviewConsumer;
    private final ReviewSummaryRepository repository;
    private final TransactionTemplate transactionTemplate;

    public ReviewBatchService(
            ReviewConsumer reviewConsumer,
            ReviewSummaryRepository repository,
            TransactionTemplate transactionTemplate
    ) {
        this.reviewConsumer = reviewConsumer;
        this.repository = repository;
        this.transactionTemplate = transactionTemplate;
    }

    @Scheduled(fixedRate = 5000)
    public void processReviews() {

        Map<Long, ReviewConsumer.ReviewBuffer> batch =
                reviewConsumer.drainBuffer();

        if (batch.isEmpty()) {
            return;
        }

        try {
            transactionTemplate.executeWithoutResult(status -> {
                batch.forEach((dishId, data) -> {

                    ReviewSummary summary = repository.findById(dishId)
                            .orElseGet(() -> new ReviewSummary(
                                    dishId,
                                    data.dishName(),
                                    0L,
                                    0L
                            ));

                    summary.setCount(
                            summary.getCount() + data.count()
                    );

                    summary.setSumRatings(
                            summary.getSumRatings() + data.sumRatings()
                    );

                    repository.save(summary);
                });
            });

        } catch (RuntimeException e) {
            reviewConsumer.restoreBuffer(batch);
            throw e;
        }
    }
}