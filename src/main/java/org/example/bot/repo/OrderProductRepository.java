package org.example.bot.repo;

import org.example.bot.entity.Order;
import org.example.bot.entity.OrderProduct;
import org.example.bot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    List<OrderProduct> findAllByUserAndOrderIsNull(User user);
    List<OrderProduct> findAllByOrderAndUser(Order order, User user);
    List<OrderProduct> findAllByOrder(Order order);
}
