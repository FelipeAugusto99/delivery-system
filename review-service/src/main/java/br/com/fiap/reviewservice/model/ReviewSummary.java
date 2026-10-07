package br.com.fiap.reviewservice.model;

import jakarta.persistence.*;

@Entity
public class ReviewSummary {

    @Id
    private Long dishId;

    private String dishName;

    private Long count;

    private Long sumRatings;

    public ReviewSummary() {
    }

    public ReviewSummary(Long dishId, String dishName, Long count, Long sumRatings) {
        this.dishId = dishId;
        this.dishName = dishName;
        this.count = count;
        this.sumRatings = sumRatings;
    }

    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getSumRatings() {
        return sumRatings;
    }

    public void setSumRatings(Long sumRatings) {
        this.sumRatings = sumRatings;
    }

    @Transient
    public double getAverage() {
        return count == null || count == 0 ? 0 : (double) sumRatings / count;
    }
}