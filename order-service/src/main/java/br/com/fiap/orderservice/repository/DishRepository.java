package br.com.fiap.orderservice.repository;

import br.com.fiap.orderservice.model.Dish;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DishRepository extends JpaRepository<Dish, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Dish d WHERE d.id = :id")
    Optional<Dish> findByIdWithLock(@Param("id") Long id);
}