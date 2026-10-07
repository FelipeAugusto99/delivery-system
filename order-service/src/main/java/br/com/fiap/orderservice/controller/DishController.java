package br.com.fiap.orderservice.controller;

import br.com.fiap.orderservice.model.Dish;
import br.com.fiap.orderservice.repository.DishRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dishes")
public class DishController {

    private final DishRepository dishRepository;

    public DishController(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    @GetMapping
    public List<Dish> findAll() {
        return dishRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dish> findById(@PathVariable Long id) {
        return dishRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}