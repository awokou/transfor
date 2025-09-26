package com.server.transfor.batch;

import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreBatchConfig {

    @Bean
    public CreCsvTasklet creCsvTasklet(){
        return new CreCsvTasklet();
    }

    @Bean
    public PartitionHandler partitionHandler(JobRepository jobRepository) {
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setGridSize(3);
        //taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
        //taskExecutorPartitionHandler.setStep(slaveStep(jobRepository));
        return taskExecutorPartitionHandler;
    }
}
