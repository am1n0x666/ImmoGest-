package immogest.modele;
import jakarta.persistence.*;
@Entity
@Table(name="locataire")
public class Locataire extends Entite {
    @Column(nullable=false)
    private String nom;
    @Column(nullable=false)
    private String telephone;
    @Column(nullable=false)
    private String email;
    protected Locataire() { }
    public Locataire(String nom, String telephone, String email) {
        this.nom = nom;
        this.telephone = telephone;
        this.email = email;
        valider();
    }
    @Override public void valider() {
        Validation.texte(nom, "Nom");
        Validation.texte(telephone, "Téléphone");
        Validation.texte(email, "Email");
        if (!email.contains("@")) { throw new IllegalArgumentException("L'email doit contenir @."); }
    }
    public String getNom() { return nom; }
    public String getTelephone() { return telephone; }
    public String getEmail() { return email; }
    @Override public String toString() { return nom; }
}
