package immogest.modele;
import jakarta.persistence.*;
@Entity
@Table(name="gestionnaire")
public class Gestionnaire extends Entite {
    @Column(nullable=false)
    private String nom;
    @Column(nullable=false)
    private String matricule;
    protected Gestionnaire() { }
    public Gestionnaire(String nom, String matricule) {
        this.nom = nom;
        this.matricule = matricule;
        valider();
    }
    @Override public void valider() {
        Validation.texte(nom, "Nom");
        Validation.texte(matricule, "Matricule");
    }
    public String getNom() { return nom; }
    public String getMatricule() { return matricule; }
    @Override public String toString() { return nom; }
}
