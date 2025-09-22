package com.server.transfor.service;

import com.server.transfor.model.CaPaiementLine;
import com.server.transfor.model.CreLine;
import com.server.transfor.utils.FixedWidthWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CreService {

    private static final String EXCLUDED = "00018";
    private static final String GV = "GV";
    private static final String GV001 = "GV001";
    private static final String GV002 = "GV002";
    private static final String AC = "AC";
    private static final String AD = "AD";
    private static final String A = "A";
    private final JavaMailSender mailSender;

    public CreService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Traitement principal
    public void processCAFile(Path inputFile, Path outputFile) throws IOException {
        List<CaPaiementLine> lines = readCsv(inputFile);

        // Calcul SOMME
        BigDecimal sommeAC = sumByType(lines, AC);
        BigDecimal sommeAD = sumByType(lines, AD);
        BigDecimal somme = sommeAC.subtract(sommeAD);

        //Determiner le sens
        boolean isCredit = somme.compareTo(BigDecimal.ZERO) >= 0;

        if (isCredit) {
            // Exclusion de l'entité 00018
            List<CaPaiementLine> filtered = filterByEntity(lines, EXCLUDED, false);
            BigDecimal ac2 = sumByType(filtered, AC);
            BigDecimal ad2 = sumByType(filtered, AD);
            somme = ac2.subtract(ad2);
            // somme peut être positif ou négatif ici selon les données, conserver le signe
        } else {
            // sens Débit : rendre somme positive
            somme = somme.abs();
        }

        // Générer les lignes CRE
        List<CreLine> creLines = new ArrayList<>();
        // lignes normales
        for (CaPaiementLine ligne : lines) {
            if (isCredit && EXCLUDED.equals(ligne.getCodeEntiteTGENTITE())) {
                continue;
            }
            CreLine cre = getCreLine(ligne);
            creLines.add(cre);
        }

        // trouver une ligne de l'entité 00018 pour récupérer les dates
        Optional<CaPaiementLine> ref = lines.stream()
                .filter(l -> EXCLUDED.equals(l.getCodeEntiteTGENTITE()))
                .findFirst();

        LocalDate debutRef = ref.map(CaPaiementLine::getDateDebutPeriode).orElse(LocalDate.now());
        LocalDate finRef = ref.map(CaPaiementLine::getDateFinPeriode).orElse(LocalDate.now());

        CreLine sommeLine = new CreLine(GV, GV002,
                isCredit ? AC : AD,
                isCredit ? "99999" : "14000",
                debutRef,
                finRef,
                somme,
                "EUR",
                A,
                "0",
                "H",
                "F");
        creLines.add(sommeLine);
        // Générer le fichier CRE format fixe
        FixedWidthWriter.write(outputFile, creLines);
    }

    private static CreLine getCreLine(CaPaiementLine ligne) {
        CreLine cre = new CreLine();
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

    // Lecture du fichier CSV source
    private List<CaPaiementLine> readCsv(Path path) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        try (Reader reader = Files.newBufferedReader(path);
             CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withDelimiter(';'))) {

            List<CaPaiementLine> lines = new ArrayList<>();
            int expectedSize = 19;

            for (CSVRecord chp : parser) {
                if (chp.size() < expectedSize) {
                    continue;
                }

                lines.add(new CaPaiementLine(
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
                        LocalDate.parse(chp.get(10),formatter), // dateDebutPeriode
                        LocalDate.parse(chp.get(11),formatter), // dateFinPeriode
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

    private BigDecimal parseBigDecimalSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO; // ou null si tu veux
        }
        return new BigDecimal(value.trim());
    }

    // Calcul de la somme des montants HT pour un type de document donné(AC ou AD)
    private BigDecimal sumByType(List<CaPaiementLine> lines, String type) {
        return lines.stream()
                .filter(l -> type.equals(l.getTypeDocument()))
                .map(CaPaiementLine::getPrixUnitaireHT)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Filtrer les lignes par code entité, keep=true pour garder, false pour exclure
    private List<CaPaiementLine> filterByEntity(List<CaPaiementLine> lines, String entity, boolean keep) {
        if (keep) {
            //True
            return lines.stream()
                    .filter(l -> entity.equals(l.getCodeEntiteTGENTITE()))
                    .toList();
        } else {
            //False
            return lines.stream()
                    .filter(l -> !entity.equals(l.getCodeEntiteTGENTITE()))
                    .toList();
        }
    }

    public void sendReportByEmail(String[] toEmail, int nbEntree, int nbSortie, List<String> erreurs, boolean ok) {
        StringBuilder content = new StringBuilder();

        content.append("Date de début de traitement: ").append(java.time.LocalDateTime.now()).append("\n");
        content.append("Nombre de lignes en entrée: ").append(nbEntree).append("\n");
        content.append("Nombre de lignes en sortie: ").append(nbSortie).append("\n");
        content.append("Etat du traitement: ").append(ok ? "OK" : "KO").append("\n");

        if (!erreurs.isEmpty()) {
            content.append("Erreurs:\n");
            for (String err : erreurs) {
                content.append("- ").append(err).append("\n");
            }
        } else {
            content.append("Aucune erreur détectée.\n");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom("");
        message.setSubject("Rapport de traitement CA_PAIEMENT");
        message.setText(content.toString());

        mailSender.send(message);
    }
}
