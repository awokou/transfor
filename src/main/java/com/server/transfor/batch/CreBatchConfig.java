package com.server.transfor.batch;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreBatchConfig {

    @Bean
    public CreCsvTasklet creCsvTasklet(){
        return new CreCsvTasklet();
    }
}
