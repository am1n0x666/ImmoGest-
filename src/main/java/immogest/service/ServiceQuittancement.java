package immogest.service;
import immogest.modele.*;
import java.time.LocalDate;
import java.util.List;
public class ServiceQuittancement {
    public double calculerLoyer(Bien bien) {
        Validation.objet(bien,"Bien"); bien.valider();
        double total=bien.calculerLoyerCharges(); Validation.positif(total,"Loyer avec charges");
        return total;
    }
    public Quittance genererQuittance(Bail bail,LocalDate date) {
        Validation.objet(bail,"Bail"); Validation.objet(date,"Date d'émission"); bail.valider();
        if (bail.isCloture()) { throw new IllegalArgumentException("Impossible d'émettre une quittance pour un bail clôturé."); }
        double charges=calculerLoyer(bail.getBien())-bail.getBien().getLoyerBase();
        Quittance q=new Quittance(bail,date,List.of("Loyer","Charges"),List.of(bail.getLoyer(),charges));
        assert q.getMontantTotal()>0;
        return q;
    }
}
