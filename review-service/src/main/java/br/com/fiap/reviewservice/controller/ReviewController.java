package br.com.fiap.reviewservice.controller;

import br.com.fiap.reviewservice.model.ReviewSummary;
import br.com.fiap.reviewservice.repository.ReviewSummaryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewSummaryRepository repository;

    public ReviewController(ReviewSummaryRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/ranking")
    public List<RankingResponse> getRanking() {
        return repository.findAll().stream()
                .sorted(Comparator.comparingDouble(
                        ReviewSummary::getAverage
                ).reversed())
                .map(summary -> new RankingResponse(
                        summary.getDishId(),
                        summary.getDishName(),
                        summary.getAverage(),
                        summary.getCount()
                ))
                .toList();
    }

    public record RankingResponse(
            Long dishId,
            String dishName,
            double average,
            Long count
    ) {
    }
}