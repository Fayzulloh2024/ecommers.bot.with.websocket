package org.example.bot.bot;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.example.bot.entity.*;
import org.example.bot.entity.status.OrderStatus;
import org.example.bot.repo.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.bot.bot.MyBot.telegramBot;


@RequiredArgsConstructor
@Service
public class BotService {
private final BotUtils botUtils;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public  void handleUpdate(Update update){
        if (update.message() != null) {
            Message message = update.message();
            User user = getUser(message.chat().id());



            if (message.text() != null) {
                String text = message.text();
                if (text.equals("/start")) {
                 SendMessage sendMessage=new SendMessage(user.getChatId(),"Choose!");
                   sendMessage.replyMarkup(botUtils.generateStartButtons());
                   telegramBot.execute(sendMessage);
                }
                if (text.equals("Home")) {
                 SendMessage sendMessage=new SendMessage(user.getChatId(),"Choose!");
                   sendMessage.replyMarkup(botUtils.generateStartButtons());
                   telegramBot.execute(sendMessage);
                }


                else if ("Category".equals(text)) {
                    SendMessage sendMessage = new SendMessage(
                            user.getChatId(),
                            "Kategoriyalarni tanlang!"
                    );
                    sendMessage.replyMarkup(botUtils.generateCategoryButtons());
                    telegramBot.execute(sendMessage);
                }

                else if ("Order".equals(text)) {
                    SendMessage sendMessage=new SendMessage(user.getChatId(),"Choose!");
                    sendMessage.replyMarkup(botUtils.generateOrderButtons());
                    telegramBot.execute(sendMessage);
                }

                else if ("Current Order".equals(text)) {
                    List<OrderProduct> orderProducts =orderProductRepository.findAllByUserAndOrderIsNull(user); // Bu metod sizning buyurtma mahsulotlarini olish uchun kerak (o'zingizning implementatsiyangiz bo'lishi mumkin)

                    if (orderProducts.isEmpty()) {
                        SendMessage messages = new SendMessage(user.getChatId(), "Sizda buyurtmalar mavjud emas.");
                        telegramBot.execute(messages);
                    } else {
                        StringBuilder messageBuilder = new StringBuilder();
                        Integer total=0;
                        for (int i = 0; i < orderProducts.size(); i++) {
                            String info = (i+1)+") "+"Name: "+orderProducts.get(i).getProduct().getName()+"    Price: "+orderProducts.get(i).getProduct().getPrice()+"     Amount: "+orderProducts.get(i).getAmount()+"\n";
                            messageBuilder.append(info);
                            total+=orderProducts.get(i).getAmount()*orderProducts.get(i).getProduct().getPrice();
                        }
                        messageBuilder.append("Total: ").append(total).append("\n");
                        SendMessage sendMessage = new SendMessage(user.getChatId(), messageBuilder.toString());
                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(new InlineKeyboardButton("do Order").callbackData("order"));
                        sendMessage.replyMarkup(inlineKeyboardMarkup);
                        telegramBot.execute(sendMessage);
                    }
                }

                else if ("Old Orders".equals(text)) {
                    List<Order> all = orderRepository.findAllByUser(user);
                    if (all.isEmpty()) {
                        SendMessage sendMessage = new SendMessage(user.getChatId(), "Sizda orderlar yo'q");
                        sendMessage.replyMarkup(botUtils.generateOrderButtons());
                        telegramBot.execute(sendMessage);
                    }else {
                        for (Order order : all) {
                            List<OrderProduct> allByOrderAndUser = orderProductRepository.findAllByOrderAndUser(order, user);
                            StringBuilder messageBuilder = new StringBuilder();
                            Integer total = 0;
                            for (int i = 0; i < allByOrderAndUser.size(); i++) {
                                String info = (i+1)+") "+"Name: "+allByOrderAndUser.get(i).getProduct().getName()+"    Price: "+allByOrderAndUser.get(i).getProduct().getPrice()+"     Amount: "+allByOrderAndUser.get(i).getAmount()+"\n";
                                messageBuilder.append(info);
                                total += allByOrderAndUser.get(i).getAmount()*allByOrderAndUser.get(i).getProduct().getPrice();
                            }
                            messageBuilder.append("Status: ").append(order.getOrderStatus());
                            messageBuilder.append("\nTotal Price: ").append(total);
                            SendMessage sendMessage = new SendMessage(user.getChatId(), messageBuilder.toString());
                            telegramBot.execute(sendMessage);
                        }
                    }


                }

                 else if(IsItInCategoryName(text)) {
                    Integer categoryId=getCategoryId(text);
                    SendMessage sendMessage = new SendMessage(
                            user.getChatId(),
                            "Product tanlang!"
                    );
                    sendMessage.replyMarkup(botUtils.generateProductButtons(categoryId));
                    telegramBot.execute(sendMessage);
                }



                else if(IsItInProductName(text)) {
                    Product product = getProduct(text);

                    String caption = String.format("%s\nЦена: %d сум", product.getName(), product.getPrice());

                    // Inlayn klaviatura
                    InlineKeyboardMarkup inlineKeyboard = botUtils.generateProductInlineButton(product.getId(), user.getAmountProduct());

                    // Oddiy tugma klaviaturasi

                    // Inlayn klaviatura bilan rasm yuborish
                    SendPhoto sendPhotoRequest = new SendPhoto(user.getChatId(), product.getImage())
                            .caption(caption)
                            .replyMarkup(inlineKeyboard);
                    telegramBot.execute(sendPhotoRequest);

                    // Keyin oddiy tugma klaviaturasi bilan xabar yuborish


                }




            }
        } else if (update.callbackQuery() != null) {
         handleCallbackQuery(update);
        }
    }

    public void handleCallbackQuery(Update update) {
        if (update.callbackQuery().data().equals("order")) {
            doOrder(update.callbackQuery().from().id(),update);
        }
        if (update.callbackQuery().data().startsWith("back_")){
            String data = update.callbackQuery().data();
            String substring = data.substring(5);
            Integer productId = Integer.parseInt(substring);
            Product product = productRepository.findById(productId).get();
            Keyboard keyboard = botUtils.generateProductButtons(product.getCategory().getId());
            SendMessage sendMessage=new SendMessage(update.callbackQuery(),"choose");
            sendMessage.replyMarkup(keyboard);
          //  telegramBot.execute(sendMessage);
        }
        User user = getUser(update.callbackQuery().from().id());
        String callbackData = update.callbackQuery().data();
        System.out.println("Callback query received: " + callbackData);

        String[] parts = callbackData.split("_");
        if (parts.length < 2) {
            System.out.println("Invalid callback data format: " + callbackData);
            return; // Exit early if the format is invalid
        }

        String action = parts[0];
        Integer productId;
        Integer currentAmount = 1; // Default value

        try {
            productId = Integer.parseInt(parts[1]);
            if (parts.length > 2) {
                currentAmount = Integer.parseInt(parts[2]);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format in callback data: " + callbackData);
            e.printStackTrace();
            return; // Exit early if parsing fails
        }

        switch (action) {
            case "decrease":
                if (currentAmount > 1) {
                    currentAmount--;
                    user.setAmountProduct(currentAmount);
                    userRepository.save(user);
                    updateProductAmount(productId, currentAmount, update);
                }
                break;
            case "increase":
                currentAmount++;
                user.setAmountProduct(currentAmount);
                userRepository.save(user);
                updateProductAmount(productId, currentAmount, update);
                break;
            case "addToCart":
                addToCart(productId, user);

                // Xabar ID va chat ID-ni oling
                long chatId = update.callbackQuery().message().chat().id();
                int messageId = update.callbackQuery().message().messageId();

                // Inline tugmalarni o'chirish
                removeInlineKeyboard( chatId, messageId);

                // Xabarni yangilash yoki yangi xabar jo'natish
                String confirmationMessage = String.format(productRepository.findById(productId).get().getName()+" added to basket with amount: %d", currentAmount);
                SendMessage sendMessage = new SendMessage(chatId, confirmationMessage);
                telegramBot.execute(sendMessage);
                break;
            default:
                System.out.println("Unknown action: " + action);
                break;
        }
    }

    private void doOrder(Long id, Update update) {
        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.CREATE);
        order.setUser(getUser(id));
        List<OrderProduct> all = orderProductRepository.findAllByUserAndOrderIsNull(getUser(id));
        int total=0;
        order.setTotalPrice(total);
        orderRepository.save(order);
        for (OrderProduct orderProduct : all) {
            orderProduct.setOrder(order);
            orderProductRepository.save(orderProduct);
            total+=orderProduct.getProduct().getPrice()*orderProduct.getAmount();
        }
        order.setTotalPrice(total);
        orderRepository.save(order);
        messagingTemplate.convertAndSend("/topic/orders", order);
        removeInlineKeyboard(id,update.callbackQuery().message().messageId());
        SendMessage sendMessage=new SendMessage(id,"an order has been created successfully !");
        telegramBot.execute(sendMessage);
    }


    private void removeInlineKeyboard( long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage(chatId, messageId);
        telegramBot.execute(deleteMessage);
    }
    private void updateProductAmount(Integer productId, int newAmount, Update update) {
        InlineKeyboardMarkup inlineKeyboard = botUtils.generateProductInlineButton(productId, newAmount);
        Keyboard keyboard = botUtils.generateProductButtons(productRepository.findById(productId).get().getCategory().getId());

        Object chatId = update.callbackQuery().message().chat().id();
        int messageId = update.callbackQuery().message().messageId();

        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup(chatId, messageId)
                .replyMarkup(inlineKeyboard);
            telegramBot.execute(editMarkup);





    }

    private void addToCart(Integer productId, User user) {
        OrderProduct orderProduct = OrderProduct.builder()
                .amount(user.getAmountProduct())
                .user(user)
                .product(productRepository.findById(productId).get())
                .build();

        orderProductRepository.save(orderProduct);
        user.setAmountProduct(0);
        userRepository.save(user);


    }

    private Product getProduct(String text) {
        List<Product> all = productRepository.findAll();
        for (Product product : all) {
            if (text.equals(product.getName())) {
                return product;
            }
        }
     return null;
    }

    private boolean IsItInProductName(String text) {
        List<Product> all = productRepository.findAll();
        for (Product product : all) {
            if (text.equals(product.getName())) {
                return true;
            }
        }
        return false;
    }

    private Integer getCategoryId(String text) {
        List<Category> all = categoryRepository.findAll();
        for (Category category : all) {
            if (category.getName().equals(text)) {
                return category.getId();
            }
        }
      return null;
    }

    private boolean IsItInCategoryName(String text) {
        List<Category> all = categoryRepository.findAll();
        for (Category category : all) {
            if (category.getName().equals(text)) {
                return true;
            }
        }
   return false;
    }

    private  User getUser(Long id) {
        List<User> all = userRepository.findAll();
        for (User user : all) {
            if (user.getChatId().equals(id)) {
                return user;
            }
        }
        User user = new User();
        user.setAmountProduct(0);
        user.setChatId(id);
        userRepository.save(user);
        return user;
    }
}
