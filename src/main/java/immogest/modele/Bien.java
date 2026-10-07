package immogest.modele;
import jakarta.persistence.*;
@Entity
@Table(name="bien")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="type_bien")
public abstract class Bien extends Entite {
    @Column(nullable=false) private String adresse;
    @Column(nullable=false) private String ville;
    @Column(nullable=false) private double surfaceM2;
    @Column(nullable=false) private double loyerBase;
    protected Bien() { }
    protected Bien(String adresse, String ville, double surfaceM2, double loyerBase) {
        this.adresse=adresse; this.ville=ville; this.surfaceM2=surfaceM2; this.loyerBase=loyerBase;
        valider();
    }
    @Override public void valider() {
        Validation.texte(adresse,"Adresse"); Validation.texte(ville,"Ville");
        Validation.positif(surfaceM2,"Surface"); Validation.positif(loyerBase,"Loyer");
    }
    public String getAdresse() { return adresse; }
    public String getVille() { return ville; }
    public double getSurfaceM2() { return surfaceM2; }
    public double getLoyerBase() { return loyerBase; }
    public abstract double calculerLoyerCharges();
    public abstract String getType();
    @Override public String toString() { return getType()+" - "+adresse+", "+ville; }
}
