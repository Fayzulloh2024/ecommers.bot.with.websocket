package org.example.bot.bot;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyBot implements CommandLineRunner {

    public static final TelegramBot telegramBot = new TelegramBot("bot token");
    private final BotService botService;


    @Override
    public void run(String... args) throws Exception {
        telegramBot.setUpdatesListener(updates->{
            for (Update update : updates) {
                botService.handleUpdate(update);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
