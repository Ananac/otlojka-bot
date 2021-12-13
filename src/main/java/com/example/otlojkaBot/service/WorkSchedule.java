package com.example.otlojkaBot.service;

import com.example.otlojkaBot.domain.Record;
import com.example.otlojkaBot.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkSchedule {
    private final RecordRepository recordRepository;
    private final TelegramBotHandler botHandler;

    @Scheduled(fixedDelayString = "10000")
    public void run() {
        Optional<Record> recordOptional = recordRepository.getFirstRecordInQueue();
        if (recordOptional.isPresent()) {
            if (recordRepository.checkIfTableIsEmpty() != 0) {
                Record lastPostedRecord = recordRepository.getLastPostedRecord().get();
                Duration duration = Duration.between(lastPostedRecord.getPostDateTime(), LocalDateTime.now());
                if (duration.toMinutes() >= 2) {
                    Record record = recordOptional.get();
                    botHandler.sendPhoto(record);
                }
            } else {
                Record record = recordOptional.get();
                botHandler.sendPhoto(record);
            }
        }
    }
}
