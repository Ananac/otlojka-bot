package com.example.otlojkaBot.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "record")
@Data
@RequiredArgsConstructor
public class Record {

    @Id
    private long id;
    private String fileId;
    private String comment;
    private String dataType;
    private LocalDateTime createDateTime;
    private LocalDateTime postDateTime;

}
