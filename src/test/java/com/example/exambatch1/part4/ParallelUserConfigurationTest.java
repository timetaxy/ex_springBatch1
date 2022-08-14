package com.example.exambatch1.part4;


import com.example.exambatch1.TestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

@SpringBatchTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {UserConfiguration.class, TestConfiguration.class}) //테스트 대상과 테스트 config
public class ParallelUserConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void test() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        int size = userRepository.findAllByUpdatedDate(LocalDate.now()).size();
//이슈 : 배치 시간이 다음날 까지 걸치게 될 경우
        Assertions.assertThat(jobExecution.getStepExecutions().stream()
                        .filter(x -> x.getStepName().equals("userLevelUpStep"))
                        .mapToInt(StepExecution::getWriteCount)
                        .sum())
                .isEqualTo(size)
                .isEqualTo(300);

        Assertions.assertThat(userRepository.count())
                .isEqualTo(400);
    }
}