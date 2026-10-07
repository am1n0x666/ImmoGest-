package immogest.dao;
import immogest.modele.Gestionnaire;
import org.hibernate.SessionFactory;
public class GestionnaireDao extends Dao<Gestionnaire> {
    public GestionnaireDao(SessionFactory factory) { super(factory,Gestionnaire.class); }
}
