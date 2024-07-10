package org.example.bot.component;

import lombok.RequiredArgsConstructor;
import org.example.bot.entity.Category;
import org.example.bot.entity.Product;
import org.example.bot.repo.CategoryRepository;
import org.example.bot.repo.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static javax.print.DocFlavor.URL.JPEG;

@Component
@RequiredArgsConstructor
public class Runner implements CommandLineRunner {

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddl;

    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (ddl.equals("create")) {
            Category category1 = new Category(1, "Clothes");
            Category category2 = new Category(2, "Food");
            Category category3 = new Category(3, "Sports");
            categoryRepo.save(category1);
            categoryRepo.save(category2);
            categoryRepo.save(category3);

            productRepository.save(Product.builder()
                    .category(category1)
                    .price(100)
                    .name("T-Shirt")
                    .build());
            productRepository.save(Product.builder()
                    .category(category1)
                    .price(100)
                    .name("Shirt")
                    .build());
            productRepository.save(Product.builder()
                    .category(category1)
                    .price(100)
                    .name("Bag")
                    .build());
        }
    }


}