package immogest.modele;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name="etat_des_lieux")
public class EtatDesLieux extends Entite {
    @ManyToOne(optional=false) @JoinColumn(name="bail_id",nullable=false) private Bail bail;
    @Column(nullable=false) private LocalDate dateRealisation;
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="defaut",joinColumns=@JoinColumn(name="etat_id"))
    @Column(name="description",nullable=false) private List<String> listeDefauts=new ArrayList<>();
    protected EtatDesLieux() { }
    public EtatDesLieux(Bail bail, LocalDate dateRealisation, List<String> defauts) {
        Validation.objet(defauts,"Liste de défauts");
        this.bail=bail; this.dateRealisation=dateRealisation; this.listeDefauts=new ArrayList<>(defauts);
        valider();
    }
    @Override public void valider() {
        Validation.objet(bail,"Bail"); Validation.objet(dateRealisation,"Date de l'état des lieux");
        bail.valider();
        if (dateRealisation.isAfter(LocalDate.now()) || dateRealisation.isBefore(bail.getDateDebut())) {
            throw new IllegalArgumentException("L'état des lieux doit être entre le début du bail et aujourd'hui.");
        }
        for (String defaut : listeDefauts) { Validation.texte(defaut,"Défaut"); }
    }
    public Bail getBail() { return bail; }
    public Bien getBien() { return bail.getBien(); }
    public LocalDate getDateRealisation() { return dateRealisation; }
    public List<String> getListeDefauts() { return new ArrayList<>(listeDefauts); }
}
