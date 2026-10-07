package immogest.dao;
import immogest.modele.EtatDesLieux;
import org.hibernate.SessionFactory;
public class EtatDesLieuxDao extends Dao<EtatDesLieux> {
    public EtatDesLieuxDao(SessionFactory factory) { super(factory,EtatDesLieux.class); }
}
