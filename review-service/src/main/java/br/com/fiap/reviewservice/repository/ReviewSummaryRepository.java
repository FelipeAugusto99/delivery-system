package br.com.fiap.reviewservice.repository;

import br.com.fiap.reviewservice.model.ReviewSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewSummaryRepository extends JpaRepository<ReviewSummary, Long> {
}