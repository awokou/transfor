package com.server.transfor.utils;

import com.server.transfor.model.CreLine;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class FixedWidthWriter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private FixedWidthWriter() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void write(Path path, List<CreLine> lines) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (CreLine line : lines) {

                String licreug = line.getTypeDocument() + "-" + String.format("%05d", Integer.parseInt(line.getCodeEntiteTGENTITE())) + "-" + line.getDateDebutPeriode().format(DATE_FMT) + "-" + line.getDateFinPeriode().format(DATE_FMT);

                // Date calculée : dernier jour du mois suivant la date de fin de période
                LocalDate dateReglement = line.getDateFinPeriode()
                        .plusMonths(1)
                        .withDayOfMonth(line.getDateFinPeriode().plusMonths(1).lengthOfMonth());
                String dateApp = formatDate(dateReglement);

                String  idcre = "IDCRE" + "-" +dateApp;

                // RFDOSSIER : "TVA-MM-AAAA" basé sur date début
                String mois = line.getDateDebutPeriode().format(DateTimeFormatter.ofPattern("MM"));
                String annee = line.getDateDebutPeriode().format(DateTimeFormatter.ofPattern("yyyy"));
                String rfdossier = "TVA-" + mois + "-" + annee;

                //Positions – tu dois ajuster selon ta spécification complète
                // CDAPP : 2 caractères
                writer.write(padRight(line.getCdapp(), 2));
                // FILLER_03 : 3 blancs
                writer.write(padRight("", 3));
                // LNTYPENREG : 5 caractères : on laisse vide
                writer.write(padRight("", 5));
                // LNTYPCRE : 5 caractères
                writer.write(padRight(line.getLntypcre(), 5));
                // DAAREGLE : 8 caractères : date de fin période + 1 mois = dernier jour du mois suivant
                writer.write(padRight(dateApp, 8));
                // CDANN : 1 caractère
                writer.write(padRight("", 1));
                // FILLER_01 : 1 caractère
                writer.write(padRight("", 1));
                // IDCRE : 17 caractères
                writer.write(padRight(idcre, 17));
                // CDINSTANCE : 34 caractères
                writer.write(padRight("", 34));
                // DADTO_A : 8 caractères same as DAAREGLE
                 writer.write(padRight(dateApp, 8));
                // DADOP_A : 8 caractères same as DAAREGLE
                writer.write(padRight(dateApp, 8));
                // ZTECH001 : 3 blancs
                writer.write(padRight("", 3));
                // FILLER_27A : 27 blancs
                writer.write(padRight("", 27));
                // CDCGB6 : 6 blancs
                writer.write(padRight("", 6));
                // CDSOCUG : 5 caractères, exemple "00001"
                writer.write(padRight("00001", 5));
                // FILLER_10A : 10 blancs
                writer.write(padRight("", 10));
                // ZBCRIT : 50 blancs
                writer.write(padRight("", 50));
                // LICREUG : concat : TypeDoc + "-" + CodeEntité + "-" + DateDebut + "-" + DateFin
                writer.write(padRight(licreug, 52));
                // DACOMPTA : 8 blancs
                writer.write(padRight("", 8));
                // DAVAL_A : 8 blancs
                writer.write(padRight("", 8));
                // ZTECH002 : 1 blanc
                writer.write(padRight("", 1));
                // FILLER_03A : 3 blancs
                writer.write(padRight("", 3));
                // NOCRPA : 11 blancs
                writer.write(padRight("", 11));
                // CDEMETUG : 6 caractères, ex "INV"
                writer.write(padRight("INV", 6));
                // CDPART : 5 caractères = code entité
                writer.write(padRight(String.format("%05d", Integer.parseInt(line.getCodeEntiteTGENTITE())), 5));
                // FILLER_20A : 20 blancs
                writer.write(padRight("", 20));
                // CDGRDJ : 2 blancs
                writer.write(padRight("", 2));
                // CDNCRE : 1 blanc
                writer.write(padRight("", 1));
                // CDCRC : 12 caractères, ex "CRC06"
                writer.write(padRight("06", 12));
                // CDPRVN : 2 blancs
                writer.write(padRight("", 2));
                // ZTECH003 : 1 blanc
                writer.write(padRight("", 1));
                // FILLER_17 : 17 blancs
                writer.write(padRight("", 17));
                // CDPOSTBUDG : 8 blancs
                writer.write(padRight("", 8));
                // RFLETTRAGE : 20 blancs
                writer.write(padRight("", 20));
                // RFDOSSIER : 20 caractères "TVA-MM-AAAA" basé sur date début
                writer.write(padRight(rfdossier, 20));
                // FILLER_60A blancs (ou autant que spécifié)
                writer.write(padRight("", 529));
                // MTCREDEV
                writer.write(padRight(encodeMontantCRE(line.getSommeHT()), 18));
                // CDDEVISE
                writer.write(padRight(line.getDevise(), 3));
                // CDTYTXCONV
                writer.write(padRight("", 1));
                // DATAUXCONV
                writer.write(padRight("", 8));
                // TXCONV
                writer.write(padRight("", 18));
                // CDRGB
                writer.write(padRight("", 6));
                // RFRAPBQE
                writer.write(padRight("", 20));
                // CDTYPSOL
                writer.write(padRight(line.getCdtypSol(), 1));
                // CDTVAUG
                writer.write(padRight(line.getCdTvaUG(), 1));
                // CDCHMTVA
                writer.write(padRight(line.getCdCHMTVA(), 1));
                // MTCREHT
                writer.write(padRight("", 18));
                // MTCRETVA
                writer.write(padRight("", 18));
                // NOCOMPTE
                writer.write(padRight("", 12));
                if("AD".equals(line.getTypeDocument())) {
                    // CDNATCRE pour AD
                    writer.write(padRight("D", 1));
                } else if("AC".equals(line.getTypeDocument())) {
                    // CDNATCRE pour AC
                    writer.write(padRight("C", 1));
                } else {
                    writer.write(padRight("", 1));
                }
                writer.write(" ");
                writer.write(" ");
                // TYPDOM(TYPE DE DOMAINE)
                writer.write(padRight(line.getTypDom(), 1));
                // Écrire ligne
                writer.newLine();
            }
        }
    }

    private static String padRight(String input, int length) {
        String s = input == null ? "" : input;
        if (s.length() > length) {
            return s.substring(0, length);
        } else {
            StringBuilder sb = new StringBuilder(s);
            while (sb.length() < length) {
                sb.append(' ');
            }
            return sb.toString();
        }
    }

    private static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FMT);
    }

    private static final Map<Integer, Character> POSITIVE_CODES = Map.of(
            0, '{', 1, 'A', 2, 'B', 3, 'C', 4, 'D',
            5, 'E', 6, 'F', 7, 'G', 8, 'H', 9, 'I'
    );
    private static final Map<Integer, Character> NEGATIVE_CODES = Map.of(
            0, '}', 1, 'J', 2, 'K', 3, 'L', 4, 'M',
            5, 'N', 6, 'O', 7, 'P', 8, 'Q', 9, 'R'
    );

    private static String encodeMontantCRE(BigDecimal montant) {
        if (montant == null) {
            montant = BigDecimal.ZERO;
        }
        BigDecimal centimes = montant.setScale(2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        boolean isNegative = centimes.signum() < 0;
        long absValue = centimes.abs().longValue();

        String base = String.format("%016d", absValue); // 16 chiffres sans le caractère de fin
        int lastDigit = (int) (absValue % 10);
        char signChar = isNegative ? NEGATIVE_CODES.get(lastDigit) : POSITIVE_CODES.get(lastDigit);

        return base + signChar; // 17 caractères
    }
}
