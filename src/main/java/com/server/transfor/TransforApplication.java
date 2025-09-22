package com.server.transfor;

import com.server.transfor.service.CreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class TransforApplication {

    @Value("${file.input}")
    private String inputCsv;

    @Value("${file.output}")
    private String outputTxt;

    private final CreService creService;

    public TransforApplication(CreService creService) {
        this.creService = creService;
    }

    public static void main(String[] args) {
        SpringApplication.run(TransforApplication.class, args);
	}

    @Bean
    CommandLineRunner run() {
        return args -> {
            URL resourceUrl = getClass().getClassLoader().getResource(inputCsv);
            if (resourceUrl == null) {
                throw new FileNotFoundException("File non trouvé dans le classpath: " + inputCsv);
            }
            Path input = Paths.get(resourceUrl.toURI());
            Path output = Paths.get(outputTxt);
            creService.processCAFile(input, output);
        };
    }
}
