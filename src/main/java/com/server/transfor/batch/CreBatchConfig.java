package com.server.transfor.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CreBatchConfig {

    @Bean
    public Job generateCREJob(JobRepository jobRepository, @Qualifier("step1Cre") Step step1, CreJobCompletionNotificationListener listener) {
        return new JobBuilder("generateCreJob",jobRepository)
                .listener(listener)
                .start(step1)
                .build();
    }

    @Bean
    protected Step step1Cre(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step1Cre", jobRepository)
                .tasklet(creCsvTasklet(), transactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    public CreCsvTasklet creCsvTasklet(){
        return new CreCsvTasklet();
    }
}
