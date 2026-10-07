package br.com.fiap.orderservice.controller;

import br.com.fiap.orderservice.dto.OrderRequest;
import br.com.fiap.orderservice.model.CustomerOrder;
import br.com.fiap.orderservice.repository.CustomerOrderRepository;
import br.com.fiap.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CustomerOrderRepository customerOrderRepository;

    public OrderController(
            OrderService orderService,
            CustomerOrderRepository customerOrderRepository
    ) {
        this.orderService = orderService;
        this.customerOrderRepository = customerOrderRepository;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        try {
            CustomerOrder order = orderService.createOrder(
                    request.getDishId(),
                    request.getQuantity()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(order);

        } catch (OrderService.InvalidQuantityException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid quantity"));

        } catch (OrderService.DishNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Dish not found"));

        } catch (OrderService.InsufficientStockException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Dish out of stock"));

        } catch (OrderService.PaymentFailedException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Payment failed"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerOrder> findById(@PathVariable Long id) {
        return customerOrderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}