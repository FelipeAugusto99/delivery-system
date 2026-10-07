package br.com.fiap.orderservice.config;

import br.com.fiap.orderservice.model.Dish;
import br.com.fiap.orderservice.repository.DishRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataLoader implements CommandLineRunner {

    private final DishRepository dishRepository;

    public DataLoader(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    @Override
    public void run(String... args) {
        dishRepository.save(new Dish(
                "House Burger",
                "Brioche bun",
                new BigDecimal("39.90"),
                10
        ));

        dishRepository.save(new Dish(
                "Margherita Pizza",
                "Tomato, mozzarella and basil",
                new BigDecimal("42.90"),
                20
        ));

        dishRepository.save(new Dish(
                "Caesar Salad",
                "Lettuce, chicken and parmesan",
                new BigDecimal("29.90"),
                20
        ));

        dishRepository.save(new Dish(
                "Pasta Bolognese",
                "Pasta with bolognese sauce",
                new BigDecimal("34.90"),
                20
        ));

        dishRepository.save(new Dish(
                "Chocolate Cake",
                "Chocolate cake slice",
                new BigDecimal("18.90"),
                20
        ));
    }
}