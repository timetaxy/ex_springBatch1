package com.example.exambatch1.part1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class HelloConfiguration {

    //이미 빈으로 생성되어 있음
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

//    public HelloConfiguration(JobBuilderFactory jobBuilderFactory,
//                              StepBuilderFactory stepBuilderFactory){
//        this.jobBuilderFactory = jobBuilderFactory;
//        this.stepBuilderFactory = stepBuilderFactory;
//    }

    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("helloJob")
                .incrementer(new RunIdIncrementer())
                .start(this.helloStep())
                .build();
    }
    //스텝은 잡의 실행 단위, 하나의 잡은 한개 이상의 스텝을 가질 수 있음
    //스텝도 잡처럼 빈으로 만들어야 함

    @Bean
    public Step helloStep() {
        return stepBuilderFactory.get("helloStep")
                .tasklet(((stepContribution, chunkContext) -> {
                    log.info("hello log");
                    return RepeatStatus.FINISHED;
                })).build();
    }
}
