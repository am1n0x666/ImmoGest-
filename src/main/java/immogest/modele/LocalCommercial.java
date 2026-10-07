package immogest.modele;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity
@DiscriminatorValue("LocalCommercial")
public class LocalCommercial extends Bien {
    protected LocalCommercial() { }
    public LocalCommercial(String adresse, String ville, double surfaceM2, double loyerBase) {
        super(adresse,ville,surfaceM2,loyerBase);
    }
    public LocalCommercial(String adresse, String ville, double surfaceM2) { this(adresse,ville,surfaceM2,500); }
    @Override public double calculerLoyerCharges() { return getLoyerBase() * (1 + 0.15); }
    @Override public String getType() { return "LocalCommercial"; }
}
