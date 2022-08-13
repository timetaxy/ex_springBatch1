package com.example.exambatch1.part2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class SharedConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job shareJob() {
        return jobBuilderFactory.get("shareJob")
                .incrementer(new RunIdIncrementer())
                .start(this.shareStep())
                .next(this.shareStep2())
                .build();
    }

    @Bean
    public Step shareStep() {
        return stepBuilderFactory.get("shareStep")
                .tasklet((contribution, chunkContext) -> {
                    //왜 contributil 바로 아래 객체로 step 인가?
                    StepExecution stepExecution = contribution.getStepExecution();
                    ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
                    stepExecutionContext.putString("stepK", "step exe ctx");
//                    contribution.getStepExecution().getExecutionContext().putString("stepK", "step exe ctx");

//                    JobExecution jobExecution = contribution.getStepExecution().getJobExecution();
                    JobExecution jobExecution = stepExecution.getJobExecution();
                    JobInstance jobInstance = jobExecution.getJobInstance();
                    ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();
                    jobExecutionContext.putString("jobK", "job exe ctx");
                    JobParameters jobParameters = jobExecution.getJobParameters();

//                    contribution.getStepExecution().getJobExecution().getExecutionContext().putString("jobK","job exe ctx");

                    log.info("jobName:{}, stepName:{}, parameter:{}", jobInstance.getJobName(), stepExecution.getStepName(), jobParameters.getLong("run.id"));
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step shareStep2() {
        return stepBuilderFactory.get("shareStep2")
                .tasklet(((contribution, chunkContext) -> {
                    ExecutionContext stepExecutionContext = contribution.getStepExecution().getExecutionContext();
                    ExecutionContext jobExecutionContext = contribution.getStepExecution().getJobExecution().getExecutionContext();

                    log.info("jK:{}, stepK:{}", jobExecutionContext.getString("jobKey", "emptyJK"), stepExecutionContext.getString("stepK", "emptyStepKey"));
                    return RepeatStatus.FINISHED;

                })).build();
    }




}
