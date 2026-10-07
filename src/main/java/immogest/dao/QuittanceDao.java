package immogest.dao;
import immogest.modele.Quittance;
import org.hibernate.SessionFactory;
public class QuittanceDao extends Dao<Quittance> {
    public QuittanceDao(SessionFactory factory) { super(factory,Quittance.class); }
}
