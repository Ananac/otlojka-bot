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
import org.telegram.telegrambots.meta.api.objects.Message;
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
        Long userId = update.getMessage().getFrom().getId();
        if (adminId.contains(userId)) {
            processMessage(update.getMessage());
        } else {
            reply(userId, "Permission denied");
        }
    }

    private void processMessage(Message message) {
        Record record = new Record();
        Long chatId = message.getChatId();
        if (message.getPhoto() != null && !message.getPhoto().isEmpty()) {
            String fileId = getLargestFileId(message);
            record.setFileId(fileId);
            record.setComment(message.getCaption());
            record.setDataType("PHOTO");
        } else if (message.getVideo() != null) {
            String fileId = message.getVideo().getFileId();
            record.setFileId(fileId);
            record.setComment(message.getCaption());
            record.setDataType("VIDEO");
        } else if (message.getAnimation() != null) {
            String fileId = message.getAnimation().getFileId();
            record.setFileId(fileId);
            record.setComment(message.getCaption());
            record.setDataType("ANIMATION");
        } else if (message.getDocument() != null) {
            String fileId = message.getDocument().getFileId();
            record.setFileId(fileId);
            record.setComment(message.getCaption());
            record.setDataType("DOCUMENT");
        } else if (message.getText() != null) {
            switch (message.getText()) {
                case "/info": {
                    reply(chatId, "Количество постов в отложке: " + recordRepository.getNumberOfScheduledPosts());
                    return;
                }
                case "/clear": {
                    reply(chatId, "Чтобы очистить напиши /delete");
                    return;
                }
                case "/delete": {
                    recordRepository.clear();
                    reply(chatId, "Очищено. Количество постов в отложке: " + recordRepository.getNumberOfScheduledPosts());
                    return;
                }
            }
            reply(chatId, "Посты с текстом не поддерживаются");
            return;
        } else {
            reply(chatId, "Я такое постить не буду");
            return;
        }
        record.setId(message.getMessageId());
        record.setCreateDateTime(LocalDateTime.now());
        record.setAuthor(message.getFrom().getUserName());
        recordRepository.save(record);
        reply(message.getChatId(), "Добавлено. Количество постов в отложке: " + recordRepository.getNumberOfScheduledPosts());
    }

    private String getLargestFileId(Message message) {
        return message.getPhoto().stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .orElse(null)
                .getFileId();
    }

    private void reply(Long chatId, String text) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText(text);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendAnimation(Record record) {
        try {
            SendAnimation sendAnimation = new SendAnimation();
            sendAnimation.setChatId(chatId);
            sendAnimation.setAnimation(new InputFile(record.getFileId()));
            execute(sendAnimation);
            afterPost(record);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendDocument(Record record) {
        try {
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId);
            sendDocument.setDocument(new InputFile(record.getFileId()));
            execute(sendDocument);
            afterPost(record);
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
            afterPost(record);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendPhoto(Record record) {
        try {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chatId);
            sendPhoto.setPhoto(new InputFile(record.getFileId()));
            execute(sendPhoto);
            afterPost(record);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendVideo(Record record) {
        try {
            SendVideo sendVideo = new SendVideo();
            sendVideo.setChatId(chatId);
            sendVideo.setVideo(new InputFile(record.getFileId()));
            execute(sendVideo);
            afterPost(record);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void afterPost(Record record) {
        record.setPostDateTime(LocalDateTime.now());
        recordRepository.save(record);
    }

}


