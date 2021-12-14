package com.example.otlojkaBot.service;

import com.example.otlojkaBot.domain.Record;
import com.example.otlojkaBot.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${schedule.postingInterval}")
    private long postingInterval;

    @Scheduled(fixedDelayString = "60000")
    public void run() {
        Optional<Record> recordOptional = recordRepository.getFirstRecordInQueue();
        if (recordOptional.isPresent()) {
            Optional<Record> lastPostedRecordOptional = recordRepository.getLastPostedRecord();
            if (lastPostedRecordOptional.isPresent()) {
                Record lastPostedRecord = lastPostedRecordOptional.get();
                Duration duration = Duration.between(lastPostedRecord.getPostDateTime(), LocalDateTime.now());
                if (duration.toMinutes() >= postingInterval) {
                    Record record = recordOptional.get();
                    botHandler.doPost(record);
                }
            } else {
                Record record = recordOptional.get();
                botHandler.doPost(record);
            }
        }
    }
}
