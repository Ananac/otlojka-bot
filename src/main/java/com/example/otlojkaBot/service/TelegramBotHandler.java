package com.example.otlojkaBot.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Data
@Slf4j
public class TelegramBotHandler extends TelegramLongPollingBot {
    @Value("${telegram.name}")
    private String name;

    @Value("${telegram.token}")
    private String token;

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            try {
                SendMessage message = getCommandResponse(text, update.getMessage().getFrom(), String.valueOf(chat_id));
                message.enableHtml(true);
                message.setParseMode(ParseMode.HTML);
                message.setChatId(String.valueOf(chat_id));
                execute(message);
            } catch (TelegramApiException e) {
                log.error("", e);
                SendMessage message = handleNotFoundCommand();
                message.setChatId(String.valueOf(chat_id));
            }
        } else if (update.hasCallbackQuery()) {
            try {
                SendMessage message = getCommandResponse(update.getCallbackQuery().getData(), update.getCallbackQuery().getFrom(), String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
                message.enableHtml(true);
                message.setParseMode(ParseMode.HTML);
                message.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
                execute(message);
            } catch (TelegramApiException e) {
                log.error("", e);
            }
        }
    }

    private SendMessage getCommandResponse(String text, User user, String chatId) throws TelegramApiException {

        return handleNotFoundCommand();
    }

    private SendMessage handleNotFoundCommand() {
        SendMessage message = new SendMessage();
        message.setText("");
        return message;
    }
}
