package com.example.otlojkaBot.repository;


import com.example.otlojkaBot.domain.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {

    @Query("select r from Record r where r.createDateTime = (select min(r1.createDateTime) from Record r1 where r1.postDateTime = null)")
    Optional<Record> getFirstRecordInQueue();

    @Query("select r from Record r where r.postDateTime = (select max(r1.postDateTime) from Record r1)")
    Optional<Record> getLastPostedRecord();

    @Query("select count(*) from Record r where r.postDateTime = null")
    long getNumberOfScheduledPosts();

}