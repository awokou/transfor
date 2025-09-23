package com.server.transfor.batch;

import com.server.transfor.model.CaPaiementLine;
import com.server.transfor.model.CreLine;
import org.springframework.batch.item.ItemProcessor;

public class CreLineItemProcessor implements ItemProcessor<CaPaiementLine, CreLine> {

    @Override
    public CreLine process(CaPaiementLine line) throws Exception {
        CreLine cre = new CreLine();
        cre.setCdapp("GV");
        cre.setLntypcre("GV001");
        cre.setTypeDocument(line.getTypeDocument());
        cre.setCodeEntiteTGENTITE(line.getCodeEntiteTGENTITE());
        cre.setDateDebutPeriode(line.getDateDebutPeriode());
        cre.setDateFinPeriode(line.getDateFinPeriode());
        cre.setSommeHT(line.getPrixUnitaireHT());
        cre.setDevise(line.getDevise());
        cre.setCdtypSol("A");
        cre.setCdTvaUG("0");
        cre.setCdCHMTVA("H");
        cre.setTypDom("F");
        return cre;
    }
}
