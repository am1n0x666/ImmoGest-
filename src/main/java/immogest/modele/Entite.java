package immogest.modele;
import jakarta.persistence.*;
@MappedSuperclass
public abstract class Entite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public Long getId() { return id; }
    public abstract void valider();
}
