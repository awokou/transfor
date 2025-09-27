package com.server.transfor.batch;

import com.server.transfor.bean.ErrorCsvBean;
import com.server.transfor.bean.PaiementCsvBean;
import com.server.transfor.bean.CreCsvBean;
import com.server.transfor.exception.SourceFileException;
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
        Path input = Paths.get("");
        Path output = Paths.get("");
        processCAFile(input, output);
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

        // 1. Liste pour stocker les anomalies détectées
        List<ErrorCsvBean> anomalies = new ArrayList<>();

        // 2. Lire le fichier CSV source avec collecte des anomalies
        List<PaiementCsvBean> lines = readCsv(inputFile, anomalies);

        int lignesEntree = lines.size();

        if (lines.isEmpty()) {
            throw new SourceFileException("Le fichier d'entrée est vide ou invalide.");
        }

        // 5. Calcul des sommes AC / AD
        BigDecimal sommeAC = sumByType(lines, AC);
        BigDecimal sommeAD = sumByType(lines, AD);
        BigDecimal somme = sommeAC.subtract(sommeAD);

        // 6. Détermination du sens : Crédit ou Débit
        boolean isCredit = somme.compareTo(BigDecimal.ZERO) >= 0;
        if (isCredit) {

            // Exclusion de l'entité 00018
            List<PaiementCsvBean> filtered = filterByEntity(lines, EXCLUDED, false);
            BigDecimal ac2 = sumByType(filtered, AC);
            BigDecimal ad2 = sumByType(filtered, AD);
            somme = ac2.subtract(ad2);
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

        // Trouver une ligne de l'entité 00018 pour récupérer les dates
        Optional<PaiementCsvBean> ref = lines.stream()
                .filter(l -> EXCLUDED.equals(l.getCodeEntiteTGENTITE()))
                .findFirst();

        LocalDate debutRef = ref.map(PaiementCsvBean::getDateDebutPeriode).orElse(LocalDate.now());
        LocalDate finRef = ref.map(PaiementCsvBean::getDateFinPeriode).orElse(LocalDate.now());
        CreCsvBean sommeLine = new CreCsvBean(GV, GV002, isCredit ? AC : AD, isCredit ? "99999" : "14000", debutRef, finRef, somme, "EUR", A, "0", "H", "F");

        // ligne de somme
        creCsvBeans.add(sommeLine);

        // Générer le fichier CRE format fixe
        WriterCsvBatch.writeCsvLine(outputFile, creCsvBeans);

        log.info("Traitement terminé avec succès. Lignes en entrée : {}, Lignes en sortie : {}, Anomalies : {}",
                lignesEntree,
                creCsvBeans.size(),
                anomalies.size()
        );

        //List<String> messages = anomalies.stream().map(AnomalieCsvBean::toString).toList();
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
    private List<PaiementCsvBean> readCsv(Path path, List<ErrorCsvBean> errorCsvBeans) throws IOException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        try (Reader reader = Files.newBufferedReader(path);
             CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withDelimiter(';'))) {
            List<PaiementCsvBean> lines = new ArrayList<>();

            int expectedSize = 19;
            int lineNumber = 0;

            for (CSVRecord record : parser) {
                lineNumber++;
                // Vérification du nombre de colonnes
                if (record.size() < expectedSize) {
                    errorCsvBeans.add(new ErrorCsvBean(lineNumber, "Nombre de colonnes insuffisant", String.valueOf(record.size())));
                    continue;
                }

                if (record.size() > expectedSize) {
                    errorCsvBeans.add(new ErrorCsvBean(lineNumber, "Nombre de colonnes excessif", String.valueOf(record.size())));
                    continue;
                }
                try {
                    // Parsing des données
                    String typeDocument = record.get(0).trim();
                    String devise = record.get(1).trim();
                    String codeAdresse = record.get(2).trim();
                    String codeSite = record.get(3).trim();
                    String axeAnalytique1Crc = record.get(4).trim();
                    String codeArticle = record.get(5).trim();
                    String designationArticle1 = record.get(6).trim();
                    String designationArticle2 = record.get(7).trim();
                    String axeAnalytique2Cgb = record.get(8).trim();
                    String codeEntiteTGENTITE = record.get(9).trim();

                    LocalDate dateDebutPeriode = LocalDate.parse(record.get(10).trim(), formatter);
                    LocalDate dateFinPeriode = LocalDate.parse(record.get(11).trim(), formatter);

                    String periodeFacturee = record.get(12).trim();
                    BigDecimal prixUnitaireHT = parseBigDecimalSafely(record.get(13));
                    BigDecimal quantite = parseBigDecimalSafely(record.get(14));
                    String numeroFactureSicof = record.get(15).trim();
                    String numeroCommandeFournisseur = record.get(16).trim();
                    String commentaire1 = record.get(17).trim();
                    String commentaire2 = record.get(18).trim();

                    // Vérifications métiers
                    List<ErrorCsvBean> ligneAnomalies = validateLine(lineNumber, typeDocument, devise, prixUnitaireHT, quantite, codeEntiteTGENTITE, dateDebutPeriode, dateFinPeriode);
                    if (!ligneAnomalies.isEmpty()) {
                        errorCsvBeans.addAll(ligneAnomalies);
                        continue;
                    }

                    // Ligne valide
                    PaiementCsvBean paiementCsvBean = new PaiementCsvBean(typeDocument, devise, codeAdresse, codeSite, axeAnalytique1Crc, codeArticle, designationArticle1, designationArticle2, axeAnalytique2Cgb, codeEntiteTGENTITE, dateDebutPeriode, dateFinPeriode, periodeFacturee, prixUnitaireHT, quantite, numeroFactureSicof, numeroCommandeFournisseur, commentaire1, commentaire2);

                    lines.add(paiementCsvBean);

                } catch (Exception e) {
                    errorCsvBeans.add(new ErrorCsvBean(lineNumber, "Format de date invalide", record.get(10) + " ou " + record.get(11)));
                }
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

    /**
     * Validation des règles métiers pour une ligne donnée
     *
     * @param lineNumber
     * @param typeDocument
     * @param devise
     * @param prixUnitaireHT
     * @param quantite
     * @param codeEntiteTGENTITE
     * @param dateDebut
     * @param dateFin
     * @return
     */
    private List<ErrorCsvBean> validateLine(int lineNumber, String typeDocument, String devise, BigDecimal prixUnitaireHT, BigDecimal quantite, String codeEntiteTGENTITE, LocalDate dateDebut, LocalDate dateFin) {
        List<ErrorCsvBean> anomalies = new ArrayList<>();

        if (typeDocument.isBlank()) {
            anomalies.add(new ErrorCsvBean(lineNumber, "Champ obligatoire vide", "Type de document"));
        }

        if (!"EUR".equalsIgnoreCase(devise)) {
            anomalies.add(new ErrorCsvBean(lineNumber, "Devise invalide (attendu: EUR)", devise));
        }

        if (prixUnitaireHT.compareTo(BigDecimal.ZERO) <= 0) {
            anomalies.add(new ErrorCsvBean(lineNumber, "Prix unitaire HT non positif", prixUnitaireHT.toPlainString()));
        }

        if (quantite.compareTo(BigDecimal.ZERO) < 0) {
            anomalies.add(new ErrorCsvBean(lineNumber, "Quantité négative", quantite.toPlainString()));
        }

        if (dateDebut.isAfter(dateFin)) {
            anomalies.add(new ErrorCsvBean(lineNumber, "Date de début après la date de fin", dateDebut + " > " + dateFin));
        }

        return anomalies;
    }
}
