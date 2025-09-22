package com.server.transfor.model;

import com.server.transfor.validators.MaxLength;
import com.server.transfor.validators.Required;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaPaiementLine {

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
}
