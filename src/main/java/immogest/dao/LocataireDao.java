package immogest.dao;
import immogest.modele.Locataire;
import org.hibernate.SessionFactory;
public class LocataireDao extends Dao<Locataire> {
    public LocataireDao(SessionFactory factory) { super(factory,Locataire.class); }
}
