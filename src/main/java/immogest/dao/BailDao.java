package immogest.dao;
import immogest.modele.Bail;
import org.hibernate.SessionFactory;
public class BailDao extends Dao<Bail> {
    public BailDao(SessionFactory factory) { super(factory,Bail.class); }
}
