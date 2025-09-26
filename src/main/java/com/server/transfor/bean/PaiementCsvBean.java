package com.server.transfor.bean;

import com.server.transfor.annotations.MaxLength;
import com.server.transfor.annotations.Required;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PaiementCsvBean {

    @Required(message = "Le nom est obligatoire")
    @MaxLength(value = 5)
    private String typeDocument;// "AC" ou "AD"

    @MaxLength(value = 3)
    private String devise;

    @MaxLength(value = 5)
    private String codeAdresse;

    @MaxLength(value = 5)
    private String codeSite;

    @MaxLength(value = 15)
    private String axeAnalytique1Crc;

    private String codeArticle;

    private String designationArticle1;

    private String designationArticle2;

    private String axeAnalytique2Cgb;

    private String codeEntiteTGENTITE;

    private LocalDate dateDebutPeriode;

    private LocalDate dateFinPeriode;

    private String periodeFacturee;

    private BigDecimal prixUnitaireHT;

    private BigDecimal quantite;

    private String numeroFactureSicof;

    private String numeroCommandeFournisseur;

    private String commentaire1;

    private String commentaire2;

    public PaiementCsvBean(String typeDocument, String devise, String codeAdresse, String codeSite, String axeAnalytique1Crc, String codeArticle, String designationArticle1, String designationArticle2, String axeAnalytique2Cgb, String codeEntiteTGENTITE, LocalDate dateDebutPeriode, LocalDate dateFinPeriode, String periodeFacturee, BigDecimal prixUnitaireHT, BigDecimal quantite, String numeroFactureSicof, String numeroCommandeFournisseur, String commentaire1, String commentaire2) {
        this.typeDocument = typeDocument;
        this.devise = devise;
        this.codeAdresse = codeAdresse;
        this.codeSite = codeSite;
        this.axeAnalytique1Crc = axeAnalytique1Crc;
        this.codeArticle = codeArticle;
        this.designationArticle1 = designationArticle1;
        this.designationArticle2 = designationArticle2;
        this.axeAnalytique2Cgb = axeAnalytique2Cgb;
        this.codeEntiteTGENTITE = codeEntiteTGENTITE;
        this.dateDebutPeriode = dateDebutPeriode;
        this.dateFinPeriode = dateFinPeriode;
        this.periodeFacturee = periodeFacturee;
        this.prixUnitaireHT = prixUnitaireHT;
        this.quantite = quantite;
        this.numeroFactureSicof = numeroFactureSicof;
        this.numeroCommandeFournisseur = numeroCommandeFournisseur;
        this.commentaire1 = commentaire1;
        this.commentaire2 = commentaire2;
    }
}
