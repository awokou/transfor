package com.server.transfor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreLine {
    private String cdapp;            // "GV"
    private String lntypcre;         // "GV001" ou "GV002"
    private String typeDocument;          // "AC" ou "AD"
    private String codeEntiteTGENTITE;     // entité selon règle (pour ligne SOMME ou ligne normale)
    private LocalDate dateDebutPeriode;
    private LocalDate dateFinPeriode;
    private BigDecimal sommeHT;
    private String devise; // "EUR"
    private String cdtypSol; // "A"
    private String cdTvaUG;
    private String cdCHMTVA;
    private String typDom;// "F"
}
