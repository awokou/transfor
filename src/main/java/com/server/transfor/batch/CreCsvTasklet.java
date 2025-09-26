package com.server.transfor.batch;

import com.server.transfor.bean.PaiementCsvBean;
import com.server.transfor.bean.CreCsvBean;
import com.server.transfor.utils.WriterCsvBatch;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CreCsvTasklet implements Tasklet, StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(CreCsvTasklet.class);

    private static final String EXCLUDED = "00018";
    private static final String GV = "GV";
    private static final String GV001 = "GV001";
    private static final String GV002 = "GV002";
    private static final String AC = "AC";
    private static final String AD = "AD";
    private static final String A = "A";

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("Executing Tasklet: Processing data...");
        Path input = Paths.get("");
        Path output = Paths.get("");
        processCAFile(input,output);
        return RepeatStatus.FINISHED;
    }


    /**
     * Processus principal pour lire le fichier CSV d'entrée, calculer les sommes,
     * générer les lignes CRE et écrire le fichier de sortie.
     *
     * @param inputFile
     * @param outputFile
     * @throws IOException
     */
    private void processCAFile(Path inputFile, Path outputFile) throws IOException {
        List<PaiementCsvBean> lines = readCsv(inputFile);

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Le fichier d'entrée est vide ou invalide.");
        }

        // Calcul Somme AC - AD
        BigDecimal sommeAC = sumByType(lines, AC);
        BigDecimal sommeAD = sumByType(lines, AD);
        BigDecimal somme = sommeAC.subtract(sommeAD);

        //Determiner le sens
        boolean isCredit = somme.compareTo(BigDecimal.ZERO) >= 0;
        if (isCredit) {

            // Exclusion de l'entité 00018
            List<PaiementCsvBean> filtered = filterByEntity(lines, EXCLUDED, false);
            BigDecimal ac2 = sumByType(filtered, AC);
            BigDecimal ad2 = sumByType(filtered, AD);
            somme = ac2.subtract(ad2);

            // somme peut être positif ou négatif ici selon les données, conserver le signe
        } else {

            // sens Débit : rendre somme positive
            somme = somme.abs();
        }

        // Générer les lignes CRE
        List<CreCsvBean> creCsvBeans = new ArrayList<>();

        // lignes normales
        for (PaiementCsvBean ligne : lines) {
            if (isCredit && EXCLUDED.equals(ligne.getCodeEntiteTGENTITE())) {
                continue;
            }
            CreCsvBean cre = getLine(ligne);
            creCsvBeans.add(cre);
        }

        // trouver une ligne de l'entité 00018 pour récupérer les dates
        Optional<PaiementCsvBean> ref = lines.stream()
                .filter(l -> EXCLUDED.equals(l.getCodeEntiteTGENTITE()))
                .findFirst();

        LocalDate debutRef = ref.map(PaiementCsvBean::getDateDebutPeriode).orElse(LocalDate.now());
        LocalDate finRef = ref.map(PaiementCsvBean::getDateFinPeriode).orElse(LocalDate.now());
        CreCsvBean sommeLine = new CreCsvBean(GV, GV002, isCredit ? AC : AD, isCredit ? "99999" : "14000", debutRef, finRef, somme, "EUR", A, "0", "H", "F");
        creCsvBeans.add(sommeLine);

        // Générer le fichier CRE format fixe
        WriterCsvBatch.writeCsvLine(outputFile, creCsvBeans);
    }

    /**
     * Conversion d'une ligne PaiementCsvBean à CreCsvBean
     *
     * @param ligne
     * @return
     */
    private CreCsvBean getLine(PaiementCsvBean ligne) {
        CreCsvBean cre = new CreCsvBean();
        cre.setCdapp(GV);
        cre.setLntypcre(GV001);
        cre.setTypeDocument(ligne.getTypeDocument());
        cre.setCodeEntiteTGENTITE(ligne.getCodeEntiteTGENTITE());
        cre.setDateDebutPeriode(ligne.getDateDebutPeriode());
        cre.setDateFinPeriode(ligne.getDateFinPeriode());
        cre.setSommeHT(ligne.getPrixUnitaireHT());
        cre.setDevise(ligne.getDevise());
        cre.setCdtypSol(A);
        cre.setCdTvaUG("0");
        cre.setCdCHMTVA("H");
        cre.setTypDom("F");
        return cre;
    }

    /**
     * Lecture du fichier CSV source
     *
     * @param path
     * @return
     * @throws IOException
     */
    private List<PaiementCsvBean> readCsv(Path path) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        try (Reader reader = Files.newBufferedReader(path);
             CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withDelimiter(';'))) {
            List<PaiementCsvBean> lines = new ArrayList<>();
            int expectedSize = 19;
            for (CSVRecord chp : parser) {
                if (chp.size() < expectedSize) {
                    continue;
                }
                lines.add(new PaiementCsvBean(
                        chp.get(0), // typeDocument
                        chp.get(1), // devise
                        chp.get(2), // codeAdresse
                        chp.get(3), // codeSite
                        chp.get(4), // axeAnalytique1Crc
                        chp.get(5), // codeArticle
                        chp.get(6), // designationArticle1
                        chp.get(7), // designationArticle2
                        chp.get(8), // axeAnalytique2Cgb
                        chp.get(9), // codeEntiteTGENTITE
                        LocalDate.parse(chp.get(10), formatter), // dateDebutPeriode
                        LocalDate.parse(chp.get(11), formatter), // dateFinPeriode
                        chp.get(12), // periodeFacturee
                        parseBigDecimalSafely(chp.get(13)), // prixUnitaireHT
                        parseBigDecimalSafely(chp.get(14)), // quantite
                        chp.get(15), // numeroFactureSicof
                        chp.get(16), // numeroCommandeFournisseur
                        chp.get(17), // commentaire1
                        chp.get(18)  // commentaire2
                ));
            }

            return lines;
        }
    }

    /**
     * Conversion sécurisée de String à BigDecimal
     *
     * @param value
     * @return
     */
    private BigDecimal parseBigDecimalSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO; // ou null si tu veux
        }
        return new BigDecimal(value.trim());
    }


    /**
     * Calcul de la somme des montants HT pour un type de document donné(AC ou AD)
     *
     * @param lines
     * @param type
     * @return
     */
    private BigDecimal sumByType(List<PaiementCsvBean> lines, String type) {
        return lines.stream()
                .filter(l -> type.equals(l.getTypeDocument()))
                .map(PaiementCsvBean::getPrixUnitaireHT)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Filtrage des lignes par entité, garder ou exclure selon le paramètre keep
     *
     * @param lines
     * @param entity
     * @param keep
     * @return
     */
    private List<PaiementCsvBean> filterByEntity(List<PaiementCsvBean> lines, String entity, boolean keep) {
        if (keep) {
            return lines.stream()
                    .filter(l -> entity.equals(l.getCodeEntiteTGENTITE()))
                    .toList();
        } else {
            return lines.stream()
                    .filter(l -> !entity.equals(l.getCodeEntiteTGENTITE()))
                    .toList();
        }
    }
}
