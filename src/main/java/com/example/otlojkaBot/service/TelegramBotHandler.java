package com.example.otlojkaBot.service;

import com.example.otlojkaBot.domain.Record;
import com.example.otlojkaBot.repository.RecordRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

    private enum COMMANDS {
        START("/start"),
        INFO("/info");

        private String command;

        COMMANDS(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        if (adminId.contains(userId)) {
            processMessage(update);
        } else {
            reply(userId, "Permission denied");
        }
    }

    private void processMessage(Update update) {
        Record record = new Record();
        if (update.getMessage().getPhoto() != null && !update.getMessage().getPhoto().isEmpty()) {
            String fileId = update.getMessage().getPhoto().stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(null)
                    .getFileId();
            record.setFileId(fileId);
            record.setComment(update.getMessage().getCaption());
            record.setDataType("PHOTO");
        } else if (update.getMessage().getVideo() != null) {
            String fileId = update.getMessage().getVideo().getFileId();
            record.setFileId(fileId);
            record.setComment(update.getMessage().getCaption());
            record.setDataType("VIDEO");
        } else if (update.getMessage().getAnimation() != null) {
            String fileId = update.getMessage().getAnimation().getFileId();
            record.setFileId(fileId);
            record.setComment(update.getMessage().getCaption());
            record.setDataType("ANIMATION");
        } else if (update.getMessage().getDocument() != null) {
            String fileId = update.getMessage().getDocument().getFileId();
            record.setFileId(fileId);
            record.setComment(update.getMessage().getCaption());
            record.setDataType("DOCUMENT");
        } else if (update.getMessage().getText() != null) {
            Long chatId = update.getMessage().getChatId();
            if (update.getMessage().getText().equals("/start")) {
                handleStartCommand(chatId);
                return;
            } else if (update.getMessage().getText().equals("/info")) {
                long numberOfScheduledPosts = recordRepository.getNumberOfScheduledPosts();
                handleInfoCommand(chatId, "Количество постов в отложке: " + numberOfScheduledPosts);
                return;
            }
            record.setComment(update.getMessage().getText());
            record.setDataType("TEXT");
        } else {
            return;
        }
        record.setId(update.getMessage().getMessageId());
        record.setCreateDateTime(LocalDateTime.now());
        record.setAuthor(update.getMessage().getFrom().getUserName());
        recordRepository.save(record);
        long numberOfScheduledPosts = recordRepository.getNumberOfScheduledPosts();
        reply(update.getMessage().getChatId(), "Добавлено. Количество постов в отложке: " + numberOfScheduledPosts);
    }


    private void handleStartCommand(Long chatId) {
        SendMessage message = new SendMessage();
        message.setText("Доступные команды:");
        message.setChatId(String.valueOf(chatId));
        message.setReplyMarkup(getKeyboard());
        message.enableHtml(true);
        message.setParseMode(ParseMode.HTML);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleInfoCommand(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(String.valueOf(chatId));
        message.setReplyMarkup(getKeyboard());
        message.enableHtml(true);
        message.setParseMode(ParseMode.HTML);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup getKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Сколько постов в отложке?");
        inlineKeyboardButton.setCallbackData(COMMANDS.INFO.getCommand());

        List<List<InlineKeyboardButton>> keyboardButtons = new ArrayList<>();

        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton);
        keyboardButtons.add(keyboardButtonsRow1);

        inlineKeyboardMarkup.setKeyboard(keyboardButtons);

        return inlineKeyboardMarkup;
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
            sendAnimation.setCaption(record.getComment());
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
            sendDocument.setCaption(record.getComment());
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
            sendPhoto.setCaption(record.getComment());
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
            sendVideo.setCaption(record.getComment());
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


