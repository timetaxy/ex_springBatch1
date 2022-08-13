package com.example.exambatch1;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class ExamBatch1Application {

    public static void main(String[] args) {
        SpringApplication.run(ExamBatch1Application.class, args);
    }

}
