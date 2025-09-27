package com.server.transfor.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ErrorCsvBean {

    private  int ligne;
    private  String libelle;
    private  String valeurError;

    public ErrorCsvBean(int ligne, String libelle, String valeurError) {
        this.ligne = ligne;
        this.libelle = libelle;
        this.valeurError = valeurError;
    }
}
