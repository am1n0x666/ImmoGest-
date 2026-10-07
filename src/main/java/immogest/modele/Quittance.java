package immogest.modele;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name="quittance")
public class Quittance extends Entite {
    @ManyToOne(optional=false) @JoinColumn(name="bail_id",nullable=false) private Bail bail;
    @Column(nullable=false) private LocalDate dateEmission;
    @OneToMany(cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.EAGER)
    @JoinColumn(name="quittance_id",nullable=false)
    private List<LigneQuittance> lignes=new ArrayList<>();
    protected Quittance() { }
    public Quittance(Bail bail, LocalDate dateEmission, List<String> designations, List<Double> montants) {
        Validation.objet(designations,"Désignations"); Validation.objet(montants,"Montants");
        if (designations.isEmpty() || designations.size()!=montants.size()) {
            throw new IllegalArgumentException("Une quittance doit avoir au moins une ligne complète.");
        }
        this.bail=bail; this.dateEmission=dateEmission;
        for (int i=0; i<designations.size(); i++) {
            Validation.objet(montants.get(i),"Montant");
            lignes.add(new LigneQuittance(designations.get(i),montants.get(i)));
        }
        valider();
    }
    @Override public void valider() {
        Validation.objet(bail,"Bail"); Validation.objet(dateEmission,"Date d'émission"); bail.valider();
        if (lignes.isEmpty()) { throw new IllegalArgumentException("Une quittance doit avoir au moins une ligne."); }
        for (LigneQuittance ligne:lignes) { ligne.valider(); }
        Validation.positif(getMontantTotal(),"Total");
    }
    public double getMontantTotal() { double total=0; for (LigneQuittance ligne:lignes) { total+=ligne.getMontant(); } return total; }
    public Bail getBail() { return bail; }
    public LocalDate getDateEmission() { return dateEmission; }
    public List<LigneQuittance> getLignes() { return new ArrayList<>(lignes); }
    @Entity(name="LigneQuittance") @Table(name="ligne_quittance")
    public static class LigneQuittance extends Entite {
        @Column(nullable=false) private String designation;
        @Column(nullable=false) private double montant;
        protected LigneQuittance() { }
        private LigneQuittance(String designation,double montant) { this.designation=designation; this.montant=montant; valider(); }
        @Override public void valider() { Validation.texte(designation,"Désignation"); Validation.positif(montant,"Montant"); }
        public String getDesignation() { return designation; }
        public double getMontant() { return montant; }
    }
}
