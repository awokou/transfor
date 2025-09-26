package com.server.transfor.bean;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CreCsvBean {
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

    public CreCsvBean(String cdapp, String lntypcre, String typeDocument, String codeEntiteTGENTITE, LocalDate dateDebutPeriode, LocalDate dateFinPeriode, BigDecimal sommeHT, String devise, String cdtypSol, String cdTvaUG, String cdCHMTVA, String typDom) {
        this.cdapp = cdapp;
        this.lntypcre = lntypcre;
        this.typeDocument = typeDocument;
        this.codeEntiteTGENTITE = codeEntiteTGENTITE;
        this.dateDebutPeriode = dateDebutPeriode;
        this.dateFinPeriode = dateFinPeriode;
        this.sommeHT = sommeHT;
        this.devise = devise;
        this.cdtypSol = cdtypSol;
        this.cdTvaUG = cdTvaUG;
        this.cdCHMTVA = cdCHMTVA;
        this.typDom = typDom;
    }
}
