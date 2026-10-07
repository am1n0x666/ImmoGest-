package immogest.modele;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
@Table(name="bail")
public class Bail extends Entite {
    @ManyToOne(optional=false) @JoinColumn(name="bien_id",nullable=false) private Bien bien;
    @ManyToOne(optional=false) @JoinColumn(name="locataire_id",nullable=false) private Locataire locataire;
    @ManyToOne(optional=false) @JoinColumn(name="gestionnaire_id",nullable=false) private Gestionnaire gestionnaire;
    @Column(nullable=false) private LocalDate dateDebut;
    @Column(nullable=false) private LocalDate dateFin;
    @Column(nullable=false) private boolean cloture;
    @Column(nullable=false) private double loyer;
    protected Bail() { }
    public Bail(Bien bien, Locataire locataire, Gestionnaire gestionnaire, LocalDate dateDebut, LocalDate dateFin) {
        Validation.objet(bien,"Bien");
        this.bien=bien; this.locataire=locataire; this.gestionnaire=gestionnaire;
        this.dateDebut=dateDebut; this.dateFin=dateFin; this.loyer=bien.getLoyerBase();
        valider();
    }
    @Override public void valider() {
        Validation.objet(bien,"Bien"); Validation.objet(locataire,"Locataire"); Validation.objet(gestionnaire,"Gestionnaire");
        Validation.objet(dateDebut,"Date de début"); Validation.objet(dateFin,"Date de fin");
        if (!dateFin.isAfter(dateDebut)) { throw new IllegalArgumentException("La fin doit être après le début."); }
        Validation.positif(loyer,"Loyer du bail");
        bien.valider(); locataire.valider(); gestionnaire.valider();
    }
    public boolean chevauche(LocalDate debut, LocalDate fin) {
        Validation.objet(debut,"Début"); Validation.objet(fin,"Fin");
        if (!fin.isAfter(debut)) { throw new IllegalArgumentException("Période invalide."); }
        return !cloture && debut.isBefore(dateFin) && fin.isAfter(dateDebut);
    }
    public void cloturer() {
        if (cloture) { throw new IllegalStateException("Ce bail est déjà clôturé."); }
        cloture=true;
    }
    public void setLoyer(double loyer) { Validation.positif(loyer,"Loyer"); this.loyer=loyer; }
    public Bien getBien() { return bien; }
    public Locataire getLocataire() { return locataire; }
    public Gestionnaire getGestionnaire() { return gestionnaire; }
    public LocalDate getDateDebut() { return dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public boolean isCloture() { return cloture; }
    public double getLoyer() { return loyer; }
    @Override public String toString() { return locataire.getNom()+" - "+bien.getAdresse()+" ("+dateDebut+" / "+dateFin+")"; }
}
