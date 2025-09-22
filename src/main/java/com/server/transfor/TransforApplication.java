package com.server.transfor;

import com.server.transfor.service.CreService;
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
            // Charger depuis le classpath
            URL resourceUrl = getClass().getClassLoader().getResource("input/CA_PAIEMENT_20250123093141197.csv");
            if (resourceUrl == null) {
                throw new FileNotFoundException("Fichier CSV introuvable dans les ressources.");
            }
            Path input = Paths.get(resourceUrl.toURI());
            Path output = Paths.get("src/main/resources/output/CRE_FICHIER.txt");
            creService.processCAFile(input, output);
        };
    }
}
