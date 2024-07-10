package org.example.bot.bot;

import com.pengrad.telegrambot.model.request.*;
import lombok.RequiredArgsConstructor;
import org.example.bot.entity.Category;
import org.example.bot.entity.Product;
import org.example.bot.repo.CategoryRepository;
import org.example.bot.repo.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class BotUtils {
    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepository;

    public Keyboard generateCategoryButtons() {
        List<Category> categories = categoryRepo.findAll();

        // "Home" tugmasi
        KeyboardButton homeButton = new KeyboardButton("Home");

        // Kategoriyalar uchun tugmalar
        KeyboardButton[] categoryButtons = categories.stream()
                .map(category -> new KeyboardButton(category.getName()))
                .toArray(KeyboardButton[]::new);

        // Klaviaturani yaratish
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(
                new KeyboardButton[]{homeButton},  // Birinchi qatorda "Home" tugmasi
                categoryButtons                   // Keyingi qatorda kategoriyalar
        );

        // Klaviatura parametrlarini sozlash
        keyboard.resizeKeyboard(true)
                .oneTimeKeyboard(true);

        return keyboard;
    }

    public Keyboard generateProductButtons(Integer categoryId) {
        KeyboardButton homeButton = new KeyboardButton("Category");

        List<Product> products = productRepository.findByCategoryId(categoryId);
        KeyboardButton[] buttons = products.stream()
                .map(product -> new KeyboardButton(product.getName()))
                .toArray(KeyboardButton[]::new);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(
                new KeyboardButton[]{homeButton},  // Birinchi qatorda "Home" tugmasi
                buttons                  // Keyingi qatorda kategoriyalar
        );

        keyboard.resizeKeyboard(true)
                .oneTimeKeyboard(true);

        return keyboard;


    }

    public InlineKeyboardMarkup generateProductInlineButton(Integer productId, int currentAmount) {
        InlineKeyboardButton btnMinus = new InlineKeyboardButton("-").callbackData("decrease_" + productId + "_" + currentAmount);
        InlineKeyboardButton btnQuantity = new InlineKeyboardButton(String.valueOf(currentAmount)).callbackData("quantity_" + productId);
        InlineKeyboardButton btnPlus = new InlineKeyboardButton("+").callbackData("increase_" + productId + "_" + currentAmount);
        InlineKeyboardButton btnAddToCart = new InlineKeyboardButton("🛒 add to basket").callbackData("addToCart_" + productId + "_" + currentAmount);

        return new InlineKeyboardMarkup(
                new InlineKeyboardButton[][]{
                        {btnMinus, btnQuantity, btnPlus},
                        {btnAddToCart}
                }
        );
    }

    public Keyboard generateStartButtons() {
        KeyboardButton categoryButton = new KeyboardButton("Category");
        KeyboardButton orderButton = new KeyboardButton("Order");

        KeyboardButton[][] keyboardButtons = new KeyboardButton[][] {
                { categoryButton, orderButton }
        };


        return new ReplyKeyboardMarkup(keyboardButtons)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true);
    }

    public Keyboard generateOrderButtons() {
        KeyboardButton categoryButton = new KeyboardButton("Current Order");
        KeyboardButton orderButton = new KeyboardButton("Old Orders");
        KeyboardButton home = new KeyboardButton("Home");

        KeyboardButton[][] keyboardButtons = new KeyboardButton[][] {
                { categoryButton, orderButton ,home }
        };



        return new ReplyKeyboardMarkup(keyboardButtons)
                .resizeKeyboard(true);
    }
}