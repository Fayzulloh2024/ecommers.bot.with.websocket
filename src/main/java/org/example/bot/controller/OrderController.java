package org.example.bot.controller;

import com.pengrad.telegrambot.request.SendMessage;
import org.example.bot.entity.Order;
import org.example.bot.entity.OrderProduct;
import org.example.bot.entity.status.OrderStatus;
import org.example.bot.repo.OrderProductRepository;
import org.example.bot.repo.OrderRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.example.bot.bot.MyBot.telegramBot;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final OrderProductRepository orderProductRepository;

    public OrderController(OrderRepository orderRepository, SimpMessagingTemplate messagingTemplate, OrderProductRepository orderProductRepository) {
        this.orderRepository = orderRepository;
        this.messagingTemplate = messagingTemplate;
        this.orderProductRepository = orderProductRepository;
    }

    @GetMapping
    public List<Order> getOrders() {
        return orderRepository.findAll();
    }

    @PostMapping("/{orderId}/inProgress")
    public Order acceptOrder(@PathVariable Integer orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setOrderStatus(OrderStatus.PROGRESS);
        Order updatedOrder = orderRepository.save(order);
        messagingTemplate.convertAndSend("/topic/orders", updatedOrder);

        List<OrderProduct> allByOrderAndUser = orderProductRepository.findAllByOrder(order);
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < allByOrderAndUser.size(); i++) {
            String info = (i + 1) + ") " + "Name: " + allByOrderAndUser.get(i).getProduct().getName() +
                    "    Price: " + allByOrderAndUser.get(i).getProduct().getPrice() +
                    "     Amount: " + allByOrderAndUser.get(i).getAmount() + "\n";
            messageBuilder.append(info);
        }
        messageBuilder.append("Total Price: ").append(order.getTotalPrice());
        messageBuilder.append("\nStatus: ").append(OrderStatus.CREATE).append("->").append(OrderStatus.PROGRESS);
        SendMessage sendMessage = new SendMessage(order.getUser().getChatId(), messageBuilder.toString());
        telegramBot.execute(sendMessage);


        return updatedOrder;
    }

    @PostMapping("/{orderId}/completed")
    public Order completeOrder(@PathVariable Integer orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setOrderStatus(OrderStatus.COMPLETE);
        Order updatedOrder = orderRepository.save(order);
        messagingTemplate.convertAndSend("/topic/orders", updatedOrder);
        List<OrderProduct> allByOrderAndUser = orderProductRepository.findAllByOrder(order);
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < allByOrderAndUser.size(); i++) {
            String info = (i + 1) + ") " + "Name: " + allByOrderAndUser.get(i).getProduct().getName() +
                    "    Price: " + allByOrderAndUser.get(i).getProduct().getPrice() +
                    "     Amount: " + allByOrderAndUser.get(i).getAmount() + "\n";
            messageBuilder.append(info);
        }
        messageBuilder.append("Total Price: ").append(order.getTotalPrice());
        messageBuilder.append("\nStatus: ").append(OrderStatus.PROGRESS).append("->").append(OrderStatus.COMPLETE);
        SendMessage sendMessage = new SendMessage(order.getUser().getChatId(), messageBuilder.toString());
        telegramBot.execute(sendMessage);

        return updatedOrder;
    }
}