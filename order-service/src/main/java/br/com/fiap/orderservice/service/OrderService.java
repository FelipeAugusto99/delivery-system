package br.com.fiap.orderservice.service;

import br.com.fiap.orderservice.client.PaymentClient;
import br.com.fiap.orderservice.model.CustomerOrder;
import br.com.fiap.orderservice.model.Dish;
import br.com.fiap.orderservice.repository.CustomerOrderRepository;
import br.com.fiap.orderservice.repository.DishRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderService {

    private final DishRepository dishRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final PaymentClient paymentClient;

    public OrderService(
            DishRepository dishRepository,
            CustomerOrderRepository customerOrderRepository,
            PaymentClient paymentClient
    ) {
        this.dishRepository = dishRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.paymentClient = paymentClient;
    }

    @Transactional
    public CustomerOrder createOrder(Long dishId, int quantity) {

        if (quantity < 1) {
            throw new InvalidQuantityException();
        }

        Dish dish = dishRepository.findByIdWithLock(dishId)
                .orElseThrow(DishNotFoundException::new);

        if (dish.getStock() < quantity) {
            throw new InsufficientStockException();
        }

        BigDecimal totalPrice = dish.getPrice()
                .multiply(BigDecimal.valueOf(quantity));

        try {
            paymentClient.processPayment(totalPrice);
        } catch (Exception exception) {
            throw new PaymentFailedException();
        }

        dish.setStock(dish.getStock() - quantity);
        dishRepository.save(dish);

        CustomerOrder order = new CustomerOrder(
                dishId,
                quantity,
                totalPrice,
                "CONFIRMED",
                LocalDateTime.now()
        );

        return customerOrderRepository.save(order);
    }

    public static class DishNotFoundException extends RuntimeException {
    }

    public static class InsufficientStockException extends RuntimeException {
    }

    public static class InvalidQuantityException extends RuntimeException {
    }

    public static class PaymentFailedException extends RuntimeException {
    }
}