package com.example.otlojkaBot.service;

import com.example.otlojkaBot.domain.Record;
import com.example.otlojkaBot.repository.RecordRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;

@Component
@Getter
@RequiredArgsConstructor
public class TelegramBotHandler extends TelegramLongPollingBot {
    private final RecordRepository recordRepository;

    @Value("${telegram.name}")
    private String name;

    @Value("${telegram.token}")
    private String token;

    @Value("${telegram.chatId}")
    private String chatId;

    @Value("${telegram.adminId}")
    private Set<Long> adminId;

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
        if (adminId.contains(update.getMessage().getFrom().getId())) {
            processMessage(update);
        } else {
            try {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
                sendMessage.setText("Permission denied");
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void processMessage(Update update) {
        if (update.hasMessage()) {
            Record record = new Record();
            if (update.getMessage().getPhoto() != null && !update.getMessage().getPhoto().isEmpty()) {
                String f_id = update.getMessage().getPhoto().stream()
                        .max(Comparator.comparing(PhotoSize::getFileSize))
                        .orElse(null)
                        .getFileId();
                record.setFileId(f_id);
                record.setComment(update.getMessage().getCaption());
                record.setDataType("PHOTO");
            } else if (update.getMessage().getVideo() != null) {
                String f_id = update.getMessage().getVideo().getFileId();
                record.setFileId(f_id);
                record.setComment(update.getMessage().getCaption());
                record.setDataType("VIDEO");
            } else if (update.getMessage().getAnimation() != null) {
                String f_id = update.getMessage().getAnimation().getFileId();
                record.setFileId(f_id);
                record.setComment(update.getMessage().getCaption());
                record.setDataType("ANIMATION");
            } else if (update.getMessage().getDocument() != null) {
                String f_id = update.getMessage().getDocument().getFileId();
                record.setFileId(f_id);
                record.setComment(update.getMessage().getCaption());
                record.setDataType("DOCUMENT");
            } else if (update.getMessage().getText() != null) {
                record.setComment(update.getMessage().getText());
                record.setDataType("TEXT");
            } else {
                return;
            }


            record.setId(update.getMessage().getMessageId());
            record.setCreateDateTime(LocalDateTime.now());
            recordRepository.save(record);
        }
    }

    public void doPost(Record record) {
        switch (record.getDataType()) {
            case "PHOTO": {
                sendPhoto(record);
                break;
            }
            case "VIDEO": {
                sendVideo(record);
                break;
            }
            case "TEXT": {
                sendMessage(record);
                break;
            }
            case "ANIMATION": {
                sendAnimation(record);
                break;
            }
            case "DOCUMENT": {
                sendDocument(record);
                break;
            }
        }
    }

    public void sendAnimation(Record record) {
        try {
            SendAnimation sendAnimation = new SendAnimation();
            sendAnimation.setChatId(chatId);
            sendAnimation.setCaption(record.getComment());
            execute(sendAnimation);
            record.setPostDateTime(LocalDateTime.now());
            recordRepository.save(record);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendDocument(Record record) {
        try {
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId);
            sendDocument.setDocument(new InputFile(record.getFileId()));
            sendDocument.setCaption(record.getComment());
            execute(sendDocument);
            record.setPostDateTime(LocalDateTime.now());
            recordRepository.save(record);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Record record) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(record.getComment());
            execute(sendMessage);
            record.setPostDateTime(LocalDateTime.now());
            recordRepository.save(record);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendPhoto(Record record) {
        try {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chatId);
            sendPhoto.setPhoto(new InputFile(record.getFileId()));
            sendPhoto.setCaption(record.getComment());
            execute(sendPhoto);
            record.setPostDateTime(LocalDateTime.now());
            recordRepository.save(record);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendVideo(Record record) {
        try {
            SendVideo sendVideo = new SendVideo();
            sendVideo.setChatId(chatId);
            sendVideo.setVideo(new InputFile(record.getFileId()));
            sendVideo.setCaption(record.getComment());
            execute(sendVideo);
            record.setPostDateTime(LocalDateTime.now());
            recordRepository.save(record);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}


