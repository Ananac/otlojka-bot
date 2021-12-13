package com.example.otlojkaBot.service;

import com.example.otlojkaBot.domain.Record;
import com.example.otlojkaBot.repository.RecordRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.Comparator;

@Component
@Getter
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TelegramBotHandler extends TelegramLongPollingBot {
    private final RecordRepository recordRepository;

    @Value("${telegram.name}")
    private String name;

    @Value("${telegram.token}")
    private String token;

    @Value("${telegram.chatId}")
    private String chatId;

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
        if (update.hasMessage()) {
            Record record = new Record();
            String f_id = update.getMessage().getPhoto().stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(null)
                    .getFileId();
            record.setId(update.getMessage().getMessageId());
            record.setFileId(f_id);
            record.setComment(update.getMessage().getText());
            record.setDataType("PHOTO");
            record.setCreateDateTime(LocalDateTime.now());
            recordRepository.save(record);
        }
    }

    public void sendPhoto(Record record) {
        try {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chatId);
            sendPhoto.setPhoto(new InputFile(record.getFileId()));
            execute(sendPhoto);
            record.setPostDateTime(LocalDateTime.now());
            recordRepository.save(record);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}


