package com.example.exambatch1.part3;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ChunkProcessingConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

//    public ChunkProcessingConfiguration(JobBuilderFactory jobBuilderFactory,
//                                        StepBuilderFactory stepBuilderFactory) {
//        this.jobBuilderFactory = jobBuilderFactory;
//        this.stepBuilderFactory = stepBuilderFactory;
//    }

    @Bean
    public Job chunkProcessingJob() {
        return jobBuilderFactory.get("chunkProcessingJob")
                .incrementer(new RunIdIncrementer())
                .start(this.taskBaseStep())
                .next(this.chunkBaseStep(null))
//                null ?????? @JobScope ?????? ?????? ????????????
                .build();
    }

//    @Bean
//    public Step chunkBaseStep() {
//        return stepBuilderFactory.get("chunkBaseStep")
//                .<String, String>chunk(10)
////                input, output format
//                .reader(itemReader())
//                .processor(itemProcessor())
//                .writer(itemWriter())
//                .build();
//    }

//    ????????????
//    2 ????????? ??????
    @Bean
    @JobScope
    public Step chunkBaseStep(@Value("#{jobParameters[chunkSize]}") String chunkSize) {
        return stepBuilderFactory.get("chunkBaseStep")
//                ?????? ?????????
                .<String, String>chunk(StringUtils.isNotEmpty(chunkSize) ? Integer.parseInt(chunkSize) : 10)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    private ItemWriter<String> itemWriter() {
//        return items -> log.info("chunk item size : {}", items.size());
        return items -> items.forEach(log::info);
    }

    private ItemProcessor<String, String> itemProcessor() {
        return item -> item + ", Spring Batch";
    }

    private ItemReader<String> itemReader() {
//        ?????? ????????? ????????? ??????
        return new ListItemReader<>(getItems());
    }

    @Bean
    public Step taskBaseStep() {
        return stepBuilderFactory.get("taskBaseStep")
                .tasklet(this.tasklet(null))
                .build();
    }

    @Bean
    @StepScope
    public Tasklet tasklet(@Value("${jobParameters[chunkSize]}") String value) {
//    public Tasklet tasklet() {
//        return (contribution, chunkContext) -> {
//            List<String> items = getItems();
//            log.info("task item size : {}", items.size());
//            return RepeatStatus.FINISHED;
//        };


//        ??????????????? ????????? ????????? ???????????? ?????? ?????????
        List<String> items = getItems();
        return (contribution, chunkContext) -> {
//            int chunkSize = 10;

////            ????????????
////            1.jobParameters ?????? ??????
//            StepExecution stepExecution = contribution.getStepExecution();
//            JobParameters jobParameters = stepExecution.getJobParameters();
//
//            String value = jobParameters.getString("chunkSize", "10");
//            int chunkSize = StringUtils.isNotEmpty(value) ? Integer.parseInt(value) : 10;
//            int fromIdx = stepExecution.getReadCount();
//            int toIdx = fromIdx + chunkSize;
//            if (fromIdx >= items.size()) {
//                return RepeatStatus.FINISHED;
//            }
//            List<String> subList = items.subList(fromIdx, toIdx);
//            log.info("task item size : {}", subList.size());
//            stepExecution.setReadCount(toIdx);
//            return RepeatStatus.CONTINUABLE;

            //            ????????????
//            2.scope ??? ????????? ???????????????
            StepExecution stepExecution = contribution.getStepExecution();
//            JobParameters jobParameters = stepExecution.getJobParameters();

//            String value = jobParameters.getString("chunkSize", "10");
            int chunkSize = StringUtils.isNotEmpty(value) ? Integer.parseInt(value) : 10;
            int fromIdx = stepExecution.getReadCount();
            int toIdx = fromIdx + chunkSize;
            if (fromIdx >= items.size()) {
                return RepeatStatus.FINISHED;
            }
            List<String> subList = items.subList(fromIdx, toIdx);
            log.info("task item size : {}", subList.size());
            stepExecution.setReadCount(toIdx);
            return RepeatStatus.CONTINUABLE;

        };
    }

    private List<String> getItems() {
        List<String> items = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            items.add(i + " Hello");
        }

        return items;
    }


//    @Bean
//    @JobScope
//    public Step chunkBaseStep(@Value("#{jobParameters[chunkSize]}") String chunkSize) {
//
//        return stepBuilderFactory.get("chunkBaseStep")
//                .<String, String>chunk(StringUtils.isNotEmpty(chunkSize) ? Integer.parseInt(chunkSize) : 10)
//                .reader(itemReader())
//                .processor(itemProcessor())
//                .writer(itemWriter())
//                .build();
//    }

}