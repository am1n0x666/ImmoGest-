package immogest.modele;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity
@DiscriminatorValue("Appartement")
public class Appartement extends Bien implements Meublable {
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="appartement_equipement", joinColumns=@JoinColumn(name="bien_id"))
    @Column(name="designation",nullable=false)
    private List<String> equipements = new ArrayList<>();
    protected Appartement() { }
    public Appartement(String adresse, String ville, double surfaceM2, double loyerBase) {
        super(adresse,ville,surfaceM2,loyerBase);
    }
    public Appartement(String adresse, String ville, double surfaceM2) { this(adresse,ville,surfaceM2,500); }
    @Override public double calculerLoyerCharges() { return getLoyerBase() * (1 + 0.10); }
    @Override public String getType() { return "Appartement"; }
    @Override public void ajouterEquipement(String designation) {
        Validation.texte(designation,"Équipement"); equipements.add(designation.trim());
    }
    @Override public boolean estMeuble() { return !equipements.isEmpty(); }
    public List<String> getEquipements() { return new ArrayList<>(equipements); }
}
